package com.ebxps.cadif.tabletrigger;

import java.util.*;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

public class LastModifiedDate extends TableTrigger {

	private Category log = CadiRepository.getCategory();
	
	//PARAMETER
	/** LastModifiedDate field as Date Time. */
	private Path LastModifiedDatePath;
	
	public String getLastModifiedDateField() {
		return LastModifiedDatePath.format();
	}

	public void setLastModifiedDateField(String lastModifiedDateField) {
		LastModifiedDatePath = Path.parse(lastModifiedDateField);
	}

	//SETUP
	
	@Override
	public void setup(TriggerSetupContext aContext) {
		

		SchemaNode node = aContext.getSchemaNode().getNode(LastModifiedDatePath);
		
		if (node==null){
			aContext.addWarning(String.format("LastModifiedDate setup issue: field [%s] does not exist in table [%s]",
												LastModifiedDatePath.format(),
												aContext.getSchemaNode().getPathInSchema()));
		}
		
		
	}
	
	
	
	//ACTION
	
	@Override
		public void handleBeforeCreate(BeforeCreateOccurrenceContext aContext) throws OperationException {
			aContext.setAllPrivileges();
			Date now = new Date();
			log.debug(String.format("Setting last modified field [%s] in record %s", LastModifiedDatePath.format(), aContext.getOccurrenceContext().toString()));
			aContext.getOccurrenceContextForUpdate().setValue(now, LastModifiedDatePath);
		}
	
	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext aContext) throws OperationException {
		aContext.setAllPrivileges();
		Date now = new Date();
		log.debug(String.format("Setting last modified field [%s] in record %s", LastModifiedDatePath.format(), aContext.getOccurrenceContext().toString()));
		aContext.getOccurrenceContextForUpdate().setValue(now, LastModifiedDatePath);
	}
	 

}
