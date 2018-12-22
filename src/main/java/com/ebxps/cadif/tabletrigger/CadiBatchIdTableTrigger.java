package com.ebxps.cadif.tabletrigger;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.ebxps.cadif.process.ImportDefinitionManager;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.PathAccessException;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.trigger.BeforeCreateOccurrenceContext;
import com.orchestranetworks.schema.trigger.BeforeModifyOccurrenceContext;
import com.orchestranetworks.schema.trigger.TableTrigger;
import com.orchestranetworks.schema.trigger.TableTriggerExecutionContext;
import com.orchestranetworks.schema.trigger.TriggerSetupContext;
import com.orchestranetworks.service.OperationException;

/**
 * This table trigger is attached to tables that will receive data from a CADI import, where
 * the table supports the use of batch id's.</br>
 * The table trigger reads the value from the tracking info an populates the field named in the parameter 
 * with the value found in the CADI{xxxxxx} tacking info pattern, where xxxxxx is the batch id.
 * 
 * @author Craig Cox - Orchestra Networks 2017
 *
 */
public class CadiBatchIdTableTrigger extends TableTrigger {

	private String tableBatchIdField;

	public String getTableBatchIdField() {
		return tableBatchIdField;
	}


	public void setTableBatchIdField(String tableBatchIdField) {
		this.tableBatchIdField = tableBatchIdField;
	}


	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext aContext) throws OperationException {
		String batchid = (String) aContext.getOccurrenceContext().getValue(Path.parse(tableBatchIdField));
		if (batchid == null || batchid.isEmpty()){
			String ti = getTrackingInfo(aContext);
			if (ti != null){
				aContext.getOccurrenceContextForUpdate().setValue(getTrackingInfo(aContext), Path.parse(tableBatchIdField));
			}
		}
	}


	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext aContext) throws OperationException {
		String ti = getTrackingInfo(aContext);
		if (ti != null){
			aContext.getOccurrenceContextForUpdate().setValue(getTrackingInfo(aContext), Path.parse(tableBatchIdField));
		}
	}

	private String getTrackingInfo(TableTriggerExecutionContext aContext){


		String ti = aContext.getSession().getTrackingInfo();
		if (ti == null){ return null; }
		int start = ti.indexOf(ImportDefinitionManager.CADI_TRACKINGINFO);
		int end = ti.indexOf("}", start);
		String cadiTranckingId = ti.substring(start + ImportDefinitionManager.CADI_TRACKINGINFO.length(), end);
		return cadiTranckingId;
	}


	@Override
	public void setup(TriggerSetupContext aContext) {
		SchemaNode node = aContext.getSchemaNode().getNode(Path.parse(tableBatchIdField));
		if (node == null){ 
			PathAccessException e = new PathAccessException(String.format("Field [%s] does not exist in table [%s]", tableBatchIdField, aContext.getSchemaNode().getTableNode().getPathInSchema().format()));
			throw e;
		}
	}


}
