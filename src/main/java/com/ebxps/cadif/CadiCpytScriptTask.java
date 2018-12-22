package com.ebxps.cadif;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.ebxps.cadif.process.ImportDefinitionManager;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.workflow.ScriptTask;
import com.orchestranetworks.workflow.ScriptTaskBean;
import com.orchestranetworks.workflow.ScriptTaskBeanContext;
import com.orchestranetworks.workflow.ScriptTaskContext;

/**
 * CADI Copy Transformation Script Task
 * <p>This class is used in workflows to process copy transformation mappings using the 
 * CADI import definition to provide structure and sequence to the transformation
 * of independent tables in the dataset.
 * </p>
 * @author Craig Cox - Orchestra Networks 2017
 *
 */
public class CadiCpytScriptTask extends ScriptTaskBean {

	private Category log = CadiRepository.getCategory();
	
	private String ImportDefCpytPrefix;
	private Integer CadiDefnId;
	private String CadiBatchId;
	private String TableBatchIdFld;
	private String TargetDataspace;
	private String SourceDataspace;
	private String SourceDataset;
	

	
	private String cpytPrefix;
	
	
	@Override
	public void executeScript(ScriptTaskBeanContext scriptTaskContext) throws OperationException {
		
		log.debug("CADI CPYT >>>> START");
		
		ImportDefinitionManager idm = new ImportDefinitionManager(scriptTaskContext.getSession());

		idm.getDefinitions(CadiDefnId);
		idm.executeCPYT(ImportDefCpytPrefix, cpytPrefix, CadiDefnId, CadiBatchId, TableBatchIdFld, TargetDataspace, SourceDataspace, SourceDataset);

		log.debug("CADI CPYT >>>> END");
	}


	public String getImportDefCpytPrefix() {
		return ImportDefCpytPrefix;
	}


	public String getCpytPrefix() {
		return cpytPrefix;
	}


	public void setCpytPrefix(String cpytPrefix) {
		this.cpytPrefix = cpytPrefix;
	}


	public void setImportDefCpytPrefix(String importDefCpytPrefix) {
		ImportDefCpytPrefix = importDefCpytPrefix;
	}


	public Integer getCadiDefnId() {
		return CadiDefnId;
	}


	public void setCadiDefnId(Integer cadiDefnId) {
		CadiDefnId = cadiDefnId;
	}


	public String getCadiBatchId() {
		return CadiBatchId;
	}


	public void setCadiBatchId(String cadiBatchId) {
		CadiBatchId = cadiBatchId;
	}


	public String getTableBatchIdFld() {
		return TableBatchIdFld;
	}


	public void setTableBatchIdFld(String tableBatchIdFld) {
		TableBatchIdFld = tableBatchIdFld;
	}


	public String getTargetDataspace() {
		return TargetDataspace;
	}


	public void setTargetDataspace(String targetDataspace) {
		TargetDataspace = targetDataspace;
	}


	public String getSourceDataspace() {
		return SourceDataspace;
	}


	public void setSourceDataspace(String sourceDataspace) {
		SourceDataspace = sourceDataspace;
	}


	public String getSourceDataset() {
		return SourceDataset;
	}


	public void setSourceDataset(String sourceDataset) {
		SourceDataset = sourceDataset;
	}

}
