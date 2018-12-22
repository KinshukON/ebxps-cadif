package com.ebxps.cadif;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ebxps.cadif.model.NameValueBean;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.adaptation.RequestResult;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.trigger.AfterCreateOccurrenceContext;
import com.orchestranetworks.schema.trigger.AfterModifyOccurrenceContext;
import com.orchestranetworks.schema.trigger.TableTrigger;
import com.orchestranetworks.schema.trigger.TriggerSetupContext;
import com.orchestranetworks.schema.trigger.ValueChanges;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Procedure;
import com.orchestranetworks.service.ProcedureContext;
import com.orchestranetworks.service.ProcedureResult;
import com.orchestranetworks.service.ProgrammaticService;
import com.orchestranetworks.service.Session;
import com.orchestranetworks.service.ValueContextForUpdate;

/**
 * This trigger is attached to tables that cause records to be published via CRMP.
 * 
 * @author Steve Higgins - Orchestra Networks - October 2017
 *
 */
public class IntegrationTrigger extends TableTrigger {

	private String deleteTriggerReocrd;
	private boolean shouldDeleteTriggerRecord;

	private enum Action { CREATE, MODIFY; }

	private Adaptation crmpDataset = null;

	/**
	 * Trigger setup - none required.
	 * @see com.orchestranetworks.schema.trigger.TableTrigger#setup(com.orchestranetworks.schema.trigger.TriggerSetupContext)
	 */
	@Override
	public void setup(TriggerSetupContext ctx) {

		crmpDataset = Tools.findCrmpDataset();
		if (crmpDataset == null) {
			ctx.addWarning("Can't find CRMP dataset");
		}

	}


	/**
	 * A new record has been created. Publish according to the integration configuration.
	 */
	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext ctx) throws OperationException {

		Adaptation newRecord = ctx.getAdaptationOccurrence();
		for (Adaptation publishSpec : getPublishSpecs(newRecord)) {
			publish(newRecord, publishSpec, ctx.getSession(), null, Action.CREATE, ctx.getProcedureContext());
		}

	}

	/**
	 * A record has been modified. Publish according to the integration configuration.
	 */
	@Override
	public void handleAfterModify(AfterModifyOccurrenceContext ctx) throws OperationException {

		Adaptation amendedRecord = ctx.getAdaptationOccurrence();
		for (Adaptation publishSpec : getPublishSpecs(amendedRecord)) {
			publish(amendedRecord, publishSpec, ctx.getSession(), ctx.getChanges(), Action.MODIFY, ctx.getProcedureContext());
		}

	}



	/**
	 * Check the publishing criteria and publish if the new/amended record meet them
	 * @param record The new or amended record
	 * @param publishSpec A record from the 'Object Tables' table holding publishing criteria for the record
	 * @param session The current user's session
	 * @param changes A container for the changes that have been made to a record. May be null for a new record
	 * or a child record. 
	 * @param action Whether the record is being created or modified
	 */
	private void publish(Adaptation record, Adaptation publishSpec, Session session, ValueChanges changes, Action action, ProcedureContext pctx) throws OperationException{

		// The record must conform to the Xpath if one is specified
		String xpathCriteria = publishSpec.getString(CrmpPaths._ObjectTable._XpathCriteria);
		if (xpathCriteria != null && !record.matches(xpathCriteria)) {
			return;		// Don't publish
		}

		// If there are changes but none of the important fields were changed then don't publish
		if (changes != null && !hasImportantChanges(changes, publishSpec)) {
			return;		// Don't publish
		}

		// Check whether we need to descend to child records 
		String linkNodePath = publishSpec.getString(CrmpPaths._ObjectTable._LinkToChildren);
		if (linkNodePath == null) {

			// The publish-spec doesn't include a link so publish the current record 
			writeToLog(record, publishSpec, session, action);
			if (shouldDeleteTriggerRecord) { pctx.doDelete(record.getAdaptationName(), false); }

		} else {

			// Check that the link exists and points to an association
			SchemaNode linkNode = record.getSchemaNode().getNode(Path.SELF.add(linkNodePath));
			if (linkNode == null) {
				String msg = String.format("Link node [%s] not found in table [%s]", 
						linkNodePath, record.getContainerTable().getTablePath().format());
				throw OperationException.createError(msg);
			}
			if (!linkNode.isAssociationNode()) {
				String msg = String.format("Link node [%s] in table [%s] is not an association", 
						linkNodePath, record.getContainerTable().getTablePath().format());
				throw OperationException.createError(msg);
			}


			// Process each child record obtained through the association link
			RequestResult childRecords = linkNode.getAssociationLink().getResult(record, session);
			try {

				Adaptation childObjectSpec = null;
				Adaptation childRecord = childRecords.nextAdaptation();
				while (childRecord != null) {

					// First time through, look up the publish spec for the child table 
					if (childObjectSpec == null) {
						childObjectSpec = getChildRecordPublishSpec(childRecord, publishSpec);		// TODO: Check whether this is useful outside of Nordic Choice
					}

					// Recursively invoke the publish to perform all checks against each child record
					publish(childRecord, childObjectSpec, session, null, action, pctx);

					// Fetch the next child
					childRecord = childRecords.nextAdaptation();

				}

			} finally {
				childRecords.close();
			}

		}

	}


	/**
	 * Check whether the set of changes that has been applied to a record affected 
	 * any of the 'important' fields named on the publishing specification.
	 * @param changes List of changes applied to the record
	 * @param publishSpec The current publish specification, containing a list of the important fields
	 * @return true if an important field has been changed (or the change list was empty), false otherwise
	 */
	private boolean hasImportantChanges(ValueChanges changes, Adaptation publishSpec) {

		List<String> importantFieldList = publishSpec.getList(CrmpPaths._ObjectTable._ChangeToColumn);

		// If no 'important' field listed then they're all important!
		if (importantFieldList == null || importantFieldList.isEmpty()) {
			return true;
		}

		// Check whether any important field has changed
		for (String importantFieldName : importantFieldList) {
			Path importantFieldPath = Path.SELF.add(importantFieldName);
			if (changes.getChange(importantFieldPath) != null) {
				return true;	// There's an important change
			}
		}

		// No important changes
		return false;

	}


	/**
	 * Use the dataspace, dataset and table path of an existing record to find
	 * all of the Object Tables records associated to it.
	 * @param record An existing record in the database  
	 * @return a list of Object Table records - the publish specifications
	 */
	private List<Adaptation> getPublishSpecs(Adaptation record) {

		String predicate = String.format("%s='%s' and %s='%s' and %s='%s'",
				CrmpPaths._ObjectTable._Dataspace.format(), record.getHome().getKey().format(),
				CrmpPaths._ObjectTable._Dataset.format(), record.getContainer().getAdaptationName().getStringName(),
				CrmpPaths._ObjectTable._TableName.format(), record.getContainerTable().getTablePath().format());

		List<Adaptation> objectSpecs = new ArrayList<Adaptation>();
		AdaptationTable objectTablesTable = crmpDataset.getTable(CrmpPaths._ObjectTable.getPathInSchema());
		RequestResult resultSet = objectTablesTable.createRequestResult(predicate);
		try {
			Adaptation result = resultSet.nextAdaptation();
			while(result != null) {
				objectSpecs.add(result);
				result = resultSet.nextAdaptation();
			}
		} finally {
			resultSet.close();
		}

		return objectSpecs;

	}

	/**
	 * Get the publishing specification for a child record.
	 * @param childRecord A child record
	 * @param objectSpec The publishing specification of the parent record
	 * @return The publishing specification of the parent record
	 */
	private Adaptation getChildRecordPublishSpec(Adaptation childRecord, Adaptation objectSpec) {

		String predicate = String.format("%s='%s' and %s='%s' and %s='%s' and %s='%s' and %s='%s'",
				CrmpPaths._ObjectTable._SystemCode.format(), objectSpec.getString(CrmpPaths._ObjectTable._SystemCode),
				CrmpPaths._ObjectTable._ObjectId.format(), objectSpec.getString(CrmpPaths._ObjectTable._ObjectId),
				CrmpPaths._ObjectTable._Dataspace.format(), childRecord.getHome().getKey().getName(),
				CrmpPaths._ObjectTable._Dataset.format(), childRecord.getContainer().getAdaptationName().getStringName(),
				CrmpPaths._ObjectTable._TableName.format(), childRecord.getContainerTable().getTablePath().format());

		return objectSpec.getContainerTable().lookupFirstRecordMatchingPredicate(predicate);

	}

	/**
	 * Maintain the Integration Log table.
	 * @param record The source record to publish
	 * @param publishSpec The publishing specification for the source record
	 * @param session The current user's session
	 * @param action Whether this is a CREATE or MODIFY operation
	 */
	private void writeToLog(Adaptation record, Adaptation publishSpec, Session session, Action action) throws OperationException {

		String systemCode = publishSpec.getString(CrmpPaths._ObjectTable._SystemCode);
		String objectCode = publishSpec.getString(CrmpPaths._ObjectTable._ObjectId);

		String keyField = publishSpec.getString(CrmpPaths._ObjectTable._KeyColumn);
		Path keyFieldPath = Path.SELF.add(keyField);
		String objectIdValue = record.getString(keyFieldPath);
		String objectId;
		if (objectIdValue == null) { objectId = ""; } else { objectId = objectIdValue.toString(); }

		// If a record already exists with a null 'published' timestamp then exit
		String predicate = String.format("osd:is-null(%s) and %s='%s' and %s='%s' and %s='%s'",
				CrmpPaths._IntegrationLog._PublishTimestamp.format(), 
				CrmpPaths._IntegrationLog._SystemCode.format(), systemCode,	// eg "SuperOffice" or "Navision"
				CrmpPaths._IntegrationLog._ObjectCode.format(), objectCode,	// eg "Customer" or "Vendor"
				CrmpPaths._IntegrationLog._ObjectId.format(), objectId);		// an actual ID

		AdaptationTable logTable = crmpDataset.getTable(CrmpPaths._IntegrationLog.getPathInSchema());
		Adaptation existingRecord = logTable.lookupFirstRecordMatchingPredicate(predicate);


		Procedure createProc = new Procedure() {

			@Override
			public void execute(ProcedureContext pctx) throws Exception {

				// Elevate privileges
				boolean isAllPrivs = pctx.isAllPrivileges();
				pctx.setAllPrivileges(true);

				ValueContextForUpdate newRecord = pctx.getContextForNewOccurrence(logTable);
				if(existingRecord == null) {
					// Create a new record
					newRecord.setValue(new Date(), CrmpPaths._IntegrationLog._TriggerTimestamp);		// Current date
					newRecord.setValue(null, CrmpPaths._IntegrationLog._PublishTimestamp);
					newRecord.setValue(null, CrmpPaths._IntegrationLog._ResponseTimestamp);
					newRecord.setValue(null, CrmpPaths._IntegrationLog._ResponseMsg);
					newRecord.setValue(publishSpec.getOccurrencePrimaryKey().format(), CrmpPaths._IntegrationLog._TableID);	// FK
					newRecord.setValue(systemCode, CrmpPaths._IntegrationLog._SystemCode);
					newRecord.setValue(objectCode, CrmpPaths._IntegrationLog._ObjectCode);
					newRecord.setValue(action.toString(), CrmpPaths._IntegrationLog._Action);		// CREATE or MODIFY
					newRecord.setValue(objectId, CrmpPaths._IntegrationLog._ObjectId);
				}
				// If the log entry exists update the field value list.
				newRecord.setValue(getPublishList(record, publishSpec), CrmpPaths._IntegrationLog._PublishColumns);
				
				// Create or Update the Record.
				if(existingRecord == null) {
					pctx.doCreateOccurrence(newRecord, logTable);
				}else {
					pctx.doModifyContent(existingRecord, newRecord);
				}
				// Restore privileges
				pctx.setAllPrivileges(isAllPrivs);

			}

		};

		ProgrammaticService svc = ProgrammaticService.createForSession(session, logTable.getContainerAdaptation().getHome());
		ProcedureResult result = svc.execute(createProc);
		if (result.hasFailed()) {
			throw result.getException();
		}

	}

	/**
	 * Extract the extra publish columns into a list of name/value pairs
	 * @param record The record we're publishing from
	 * @param publishSpec The publishing spec
	 * @return
	 */
	private List<NameValueBean> getPublishList(Adaptation record, Adaptation publishSpec) {

		List<NameValueBean> values = new ArrayList<NameValueBean>();

		List<String> publishColumns = publishSpec.getList(CrmpPaths._ObjectTable._PublishColumns);
		for (String publishColumn : publishColumns) {
			Object value = record.get(Path.parse(publishColumn));
			String outValue;
			if (value == null) { outValue = ""; } else {outValue = value.toString(); }
			NameValueBean bean = new NameValueBean();
			bean.setName(publishColumn);
			bean.setValue(outValue);
			values.add(bean);
		}

		return values.isEmpty() ? null : values;

	}


	public String getDeleteTriggerReocrd() {
		return deleteTriggerReocrd;
	}


	public void setDeleteTriggerReocrd(String deleteTriggerReocrd) {
		this.deleteTriggerReocrd = deleteTriggerReocrd;
		if (deleteTriggerReocrd != null && deleteTriggerReocrd.toLowerCase().startsWith("y")) { shouldDeleteTriggerRecord = true; }else {shouldDeleteTriggerRecord = false;}
	}


}
