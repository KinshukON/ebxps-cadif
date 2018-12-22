package com.ebxps.cadif.tabletrigger;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.PathAccessException;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.SchemaTypeName;
import com.orchestranetworks.schema.trigger.BeforeCreateOccurrenceContext;
import com.orchestranetworks.schema.trigger.BeforeModifyOccurrenceContext;
import com.orchestranetworks.schema.trigger.TableTrigger;
import com.orchestranetworks.schema.trigger.TriggerSetupContext;
import com.orchestranetworks.service.OperationException;

/**
 * This class will set a named string field to a value depending on if the record is new or updated.
 * @param statusFieldName - Contains the field path of the field to update, from the root of the table this trigger is attached to.
 * @param newRecordValue - Contains the value to write to the field if the record operation is to create a new record
 * @param updateRecordValue - Contains the value to write to the field if the record operation is to update an existing record
 * @author Craig Cox - Orchestra Networks 2017
 *
 */

public class CadiNewUpdateStatusTableTrigger extends TableTrigger {

	private Category log = CadiRepository.getCategory();
	
	/**
	 * Contains the field path of the field to update, from the root of the table this
	 * trigger is attached to.
	 */
	private String statusFieldName;
	/**
	 * Contains the value to write to the field if the record operation is to create a new record
	 */
	private String newRecordValue;
	/**
	 * Contains the value to write to the field if the record operation is to update an existing record
	 */
	private String updateRecordValue;
	
	
	@Override
	public void setup(TriggerSetupContext aContext) {
		SchemaNode node = aContext.getSchemaNode().getNode(Path.parse(statusFieldName));
		if (node == null){ 
			PathAccessException e = new PathAccessException(String.format("Field [%s] does not exist in table [%s]", statusFieldName, aContext.getSchemaNode().getTableNode().getPathInSchema().format()));
			throw e;
		}
		SchemaTypeName xsTypeNAme = node.getXsTypeName();
		if (!xsTypeNAme.equals(SchemaTypeName.XS_STRING)){
			PathAccessException oe = new PathAccessException(String.format("Field [%s] is not a String field type", statusFieldName));
			throw oe;
		}
	}
	
	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext aContext) throws OperationException {
		aContext.getOccurrenceContextForUpdate().setValue(newRecordValue, Path.parse(statusFieldName));
	}
	
	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext aContext) throws OperationException {
		aContext.getOccurrenceContextForUpdate().setValue(updateRecordValue, Path.parse(statusFieldName));
	}

	public String getStatusFieldName() {
		return statusFieldName;
	}

	public void setStatusFieldName(String statusFieldName) {
		this.statusFieldName = statusFieldName;
	}

	public String getNewRecordValue() {
		return newRecordValue;
	}

	public void setNewRecordValue(String newRecordValue) {
		this.newRecordValue = newRecordValue;
	}

	public String getUpdateRecordValue() {
		return updateRecordValue;
	}

	public void setUpdateRecordValue(String updateRecordValue) {
		this.updateRecordValue = updateRecordValue;
	}

}
