package com.ebxps.cadif;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.ebxps.cadif.process.ImportDefinitionManager;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.workflow.ScriptTaskBean;
import com.orchestranetworks.workflow.ScriptTaskBeanContext;

/**
 * <b>ProcessStatusScriptTask</b> allows the CADI Process record status to be updated from within a workflow
 * @param cadiProcessId - The workflow data context variable name that holds the CADI process that launched this workflow
 * @param statusValue - Any value from [Q, A, C, F, T] meaning [Queued, Active, Complete, Failed, Terminated]
 * @author Craig Cox - Orchestra Networks 2017

 */
public class CadiProcessStatusScriptTask extends ScriptTaskBean {

	private Category log = CadiRepository.getCategory();
	
	private String cadiProcessId;
	private String statusValue;
	
	
	@Override
	public void executeScript(ScriptTaskBeanContext scriptTaskContext) throws OperationException {
		
		ImportDefinitionManager idm = new ImportDefinitionManager(scriptTaskContext.getSession());
		
		log.debug(String.format("Accessing Import Definition Manager to update processs using [%s] to set value [%s]", cadiProcessId, statusValue));

		idm.updateStatus(cadiProcessId, statusValue);
			
	}

	public String getCadiProcessId() {
		return cadiProcessId;
	}


	public void setCadiProcessId(String cadiProcessId) {
		this.cadiProcessId = cadiProcessId;
	}


	public String getStatusValue() {
		return statusValue;
	}


	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}
	
}
