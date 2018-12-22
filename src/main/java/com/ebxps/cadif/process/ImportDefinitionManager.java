package com.ebxps.cadif.process;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.ebxps.cadif.*;
import com.ebxps.cadif.adpatation.*;
//import com.ebxps.cpyt.CopyTransform;
import com.onwbp.adaptation.*;
import com.onwbp.base.text.UserMessage;
import com.onwbp.org.apache.log4j.*;
import com.orchestranetworks.addon.adix.AdixFactory;
import com.orchestranetworks.addon.adix.AdixImport;
import com.orchestranetworks.addon.adix.AdixImportResult;
import com.orchestranetworks.addon.adix.AdixImportResultBean;
import com.orchestranetworks.addon.adix.AdixImportSpec;
import com.orchestranetworks.addon.adix.DataAccessSpec;
import com.orchestranetworks.addon.adix.ImportDataAccessSpec;
import com.orchestranetworks.addon.adix.ImportType;
import com.orchestranetworks.addon.dataexchange.DataExchangeDataAccessSpec;
import com.orchestranetworks.instance.HomeCreationSpec;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.scheduler.ScheduledExecutionContext;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.PathAccessException;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.ExportImportCSVSpec.Header;
import com.orchestranetworks.workflow.ProcessLauncher;
import com.orchestranetworks.workflow.PublishedProcessKey;
import com.orchestranetworks.workflow.WorkflowEngine;


/**
 * Provides access method to get and handle ImportDefinitions.
 * Will only get definitions the current user has permission to execute.
 * 
 * @author craigcox
 *
 */


public class ImportDefinitionManager {

	private Category log = CadiRepository.getCategory();

	private AdaptationHome cadiDataspace;

	private Adaptation cadiDataset;

	private List<Adaptation> cadiDefinitions = new ArrayList<Adaptation>();

	private RequestResult cadiProcessRecords;

	private Adaptation definitionRecord = null;

	private Adaptation currentTableDefinition = null;

	private Adaptation currentCadiImportRecord = null;

	private Adaptation currentFilesDefinition = null;

	private Adaptation currentImportProcessRecord = null;

	private Adaptation currentImportTableRecord = null;

	private Session session;

	private ProgrammaticService pService;

	private AdaptationHome targetDataspace = null;

	public static final Integer FATAL = 1;
	public static final Integer ERORR = 2;
	public static final Integer WARN = 3;
	public static final Integer INFO = 4;

	private Adaptation currentCadiProcess = null;
	public static final String CADI_QUEUED = "Q";
	public static final String CADI_HELD = "H";
	public static final String CADI_ACTIVE = "A";
	public static final String CADI_COMPLETE = "C";
	public static final String CADI_FAILED = "F";
	public static final String CADI_TERMINAETD = "T";

	public static final String CADI_TRACKINGINFO = "CADI{";


	private String lastCadiStatus = "";

	private boolean hasErrors = false;

	private boolean cadiProcessStateOverride = false;

	private String cadiBatchId;

	protected Adaptation currentImportFileRecord;

	private String errorText = "";





	public ImportDefinitionManager(Session aSession){

		log.debug("Import Definition Manager - requested");
		cadiDataset = CadiRepository.findCadiDataset();
		cadiDataspace = cadiDataset.getHome();
		session = aSession;
		pService = ProgrammaticService.createForSession(session, cadiDataspace);

	}

	/**
	 * Get all definitions by processing order and Id.
	 * @return 0 or >0 number of definitions identified.
	 */

	public List<Adaptation> getDefinitions(){

		log.debug("Getting import definitions list");
		//Get import definition records that are active and flagged as monitored
		String predicate = Paths._Root_CADIdefinitions_CADIdefinition._CadiActive.format() + " = true and"+
				Paths._Root_CADIdefinitions_CADIdefinition._CadiMonitor.format() + " = true";
		RequestSortCriteria sortCriteria = new RequestSortCriteria();
		// sort by Process sequence and id ascending
		sortCriteria.add(Paths._Root_CADIdefinitions_CADIdefinition._CadiProcessSequence);
		sortCriteria.add(Paths._Root_CADIdefinitions_CADIdefinition._CadiId);
		// Get table
		AdaptationTable cadiDefinitionTable = cadiDataset.getTable(Paths._Root_CADIdefinitions_CADIdefinition.getPathInSchema());
		// Get records		
		RequestResult cadiDefinitionRecords = cadiDefinitionTable.createRequestResult(predicate, sortCriteria);
		validateCadiDefinitions(cadiDefinitionRecords);
		cadiDefinitionRecords.close();
		// Return the number of definitions.
		log.debug(cadiDefinitions.toString());
		return cadiDefinitions;

	}

	/**
	 * 
	 * @param cadiId definition id to get
	 * @return 0 or 1 number of definitions identified.
	 * 
	 */


	public Integer getDefinitions(Integer cadiId){

		AdaptationTable cadiDefinitionTable = cadiDataset.getTable(Paths._Root_CADIdefinitions_CADIdefinition.getPathInSchema());
		String cadiIdRef = cadiId.toString();
		definitionRecord = cadiDefinitionTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(cadiIdRef));
		validateCadiDefinition(definitionRecord);

		return 0;

	}


	/**
	 * 
	 * Get all definitions by processing order and Id, for the definition code.
	 * @param definitionCode Definition code to use to select the definitions with
	 * @return 0 or >0 number of definitions identified.
	 */

	public Integer getDefinitions(String definitionCode){


		return 0;

	}


	private void validateCadiDefinitions(RequestResult cadiDefinitionRecords){

		cadiDefinitions.clear();
		Adaptation cadiDefinitionRecord = cadiDefinitionRecords.nextAdaptation();
		while (cadiDefinitionRecord != null){
			if (validateCadiDefinition(cadiDefinitionRecord)){
				cadiDefinitions.add(cadiDefinitionRecord);
			}
			cadiDefinitionRecord = cadiDefinitionRecords.nextAdaptation();
		}

	}

	private boolean validateCadiDefinition(Adaptation cadiDefinitionRecord){
		//FIXME  validate the definition.
		log.debug(String.format("Validating CADI definition [%s]", cadiDefinitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName)));
		/* ***************************************************************
		 * 
		 * 
		 * 
		 * 		VALIDATION CODE GOES HERE!
		 * 
		 * 		JIRA RDMCORE-975 CADI technical refactoring  RDMCORE-1122
		 * 
		 * 
		 * 
		 ****************************************************************/

		log.debug(String.format("VALID CADI definition [%s]", cadiDefinitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName)));
		return true;
	}

	/**
	 * Launch an import process, by adding a record to the CADI process table.
	 * This will run the import process inline, as a table trigger executes the process.
	 * @param importDefinitionRecord - definition to launch
	 * @return 
	 */


	public boolean RequestImport(final Adaptation importDefinitionRecord, final String batchId){


		// Create an update procedure
		Procedure WriteCadiProcessRecord = new Procedure() {
			@Override
			public void execute(final ProcedureContext pContext) throws Exception {


				// Get CADIprocess table
				AdaptationTable cadiProcessesTable = cadiDataset.getTable(Paths._Root_CADIprocesses.getPathInSchema());
				ValueContextForUpdate vcu = pContext.getContextForNewOccurrence(cadiProcessesTable);
				Integer cadiId = importDefinitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId);

				// Does the record for this import definition and batch already exist? 
				String predicate = String.format("%s = '%s' and %s = '%s'",
						Paths._Root_CADIprocesses._CadiBatchId.format(), batchId,
						Paths._Root_CADIprocesses._CadiLaunchDefinition.format(), cadiId.toString());

				Adaptation processRecord = cadiProcessesTable.lookupFirstRecordMatchingPredicate(predicate);

				if (processRecord != null){ 
					log.warn(String.format("Attempting to request import for existing definition [%s] for batch [%s]", 
							importDefinitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName), 
							batchId));
					return; 
				}

				Date now = new Date();
				vcu.setValue(now, Paths._Root_CADIprocesses._CadiLaunchRequestTime);
				vcu.setValue(now, Paths._Root_CADIprocesses._CadiProcessStateLastModified);
				vcu.setValue(cadiId.toString(),Paths._Root_CADIprocesses._CadiLaunchDefinition);
				vcu.setValue(batchId, Paths._Root_CADIprocesses._CadiBatchId);
				pContext.setAllPrivileges(true);
				pContext.doCreateOccurrence(vcu, cadiProcessesTable);
				pContext.setAllPrivileges(false);
				log.info(String.format("Import request [%s] for batch [%s] added", 
						importDefinitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName), 
						batchId));
			}
		};

		// Execute the update
		pService.execute(WriteCadiProcessRecord);


		return false;
	}

	/**
	 * Returns a list of CADI process records that are in a Queued state.
	 * @param aContext - from a scheduled task
	 * @return RequestResult - list of cadi process records.
	 */

	public RequestResult getCadiProcesses(ScheduledExecutionContext aContext){
		cadiDataset = CadiRepository.findCadiDataset();
		cadiDataspace = cadiDataset.getHome();
		session = aContext.getSession();
		pService = ProgrammaticService.createForSession(session, cadiDataspace);

		ImportDefinitionManager idm = new ImportDefinitionManager(session);


		String predicate = String.format("%s = 'Q'", Paths._Root_CADIprocesses._CadiProcessState.format());
		AdaptationTable cadiProcessTable = cadiDataset.getTable(Paths._Root_CADIprocesses.getPathInSchema());
		cadiProcessRecords = cadiProcessTable.createRequestResult(predicate);
		log.debug(String.format("Found [%d] Queued process records", cadiProcessRecords.getSize()));

		return cadiProcessRecords;
	}

	public void closeCadiProcesses(){

		log.debug("Cadi process record 'RequestResult' closed.");
		cadiProcessRecords.close();

	}


	/* ************************************************* */
	/*       IMPORT PROCESSING 							 */
	/* ************************************************* */
	/** 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param processRecord
	 */




	public void executeImport(Adaptation processRecord) {

		definitionRecord = AdaptationUtil.followFK(processRecord, Paths._Root_CADIprocesses._CadiLaunchDefinition);
		//Update  status of CADI process record
		updateCADIStatus(processRecord, CADI_ACTIVE);
		log.debug(String.format("Setting process record [%d] to %s", processRecord.get_int(Paths._Root_CADIprocesses._CadiProcessId), CADI_ACTIVE));

		//From the process record get the definition record.

		String batchIdPolicy = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiBatchIdPolicy);
		String processBatchId = processRecord.getString(Paths._Root_CADIprocesses._CadiBatchId);

		// Batch Id policy is F-file,Z-zip,C-cadi,N-none 
		// Process BatchId is a valid batch from a file or "cadi".

		//Validate all of the mandatory files still exist for the import definition
		SourceFiles srcFiles = new SourceFiles();
		List<String> batchIds = srcFiles.filesExist(definitionRecord, batchIdPolicy, processBatchId);
		if (batchIds != null){
			for (String batchId : batchIds){
				//If all mandatory files exist, launch the import process.
				log.debug(String.format("Launching Import Process for definition [%s] batch [%s}", 
						definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName), batchId));

				LaunchImportProcess(processRecord, definitionRecord, batchId);
			}
		}else{
			log.debug(String.format("Request [filesExists(definitionRecord[%s], batchIdPolicy[%s], processBatchId[%s])] returned no batchids", 
					definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName), batchIdPolicy, processBatchId));
		}

		// Reset the target data space for the next import definition.
		targetDataspace = null;
		updateCADIStatus(CADI_COMPLETE);
		log.debug(String.format("Setting process record [%d] to %s", processRecord.get_int(Paths._Root_CADIprocesses._CadiProcessId), CADI_COMPLETE));

	}


	private void LaunchImportProcess(Adaptation processRecord,Adaptation definitionRecord, String batchId) {

		String batchIdPolicy = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiBatchIdPolicy);
		// "N" no batch, "C" Cadi batch id, "F" file, "Z" zip
		boolean useFileBatchId = false;
		if (batchIdPolicy.equals("F") || batchIdPolicy.equals("Z")){
			useFileBatchId = true;
		}
		if (batchIdPolicy.equals("C")){ cadiBatchId = null; }

		// Prepare the data space for import
		targetDataspace = prepareImportDataspace(definitionRecord, batchId);
		if (targetDataspace==null){
			updateCADIStatus(CADI_FAILED);
			return;
		}

		auditDefinitionRecord(processRecord, null, null);

		String executeByZip = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiExecByZip);
		String workingFolder = null;
		List<File> zipfiles = null;

		if ("Y".equals(executeByZip)){
			workingFolder = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipWorkingFolderPath);
			if (workingFolder == null){ 
				raiseException(ERORR, "No working folder defined to unzip file to", definitionRecord);
				updateCADIStatus(CADI_FAILED);
				return;
			}
			SourceFiles sourceFiles = new SourceFiles();
			//Expand the zip file before processing the definitions.
			zipfiles = sourceFiles.findZipFiles(definitionRecord, batchId, batchIdPolicy);
			for (File file :zipfiles){
				log.debug(String.format("Processing ZIP file [%s]", file.getName()));
				if (file.isFile() && file.canRead() && !file.isHidden()){
					CadiZipFile zipfile = new CadiZipFile();
					try {
						zipfile.extract(file, workingFolder);
					} catch (Exception e) {
						e.printStackTrace();
						raiseException(ERORR, "Unzip failed see log", definitionRecord);
						updateCADIStatus(CADI_FAILED);
						return;
					}
				}
			}
		}


		// Get the list of tables to import 
		RequestResult targetTableDefinitions = getTableDefnListBySeq(definitionRecord, Paths._Root_CADIdefinitions_CADIdefinition._TablesGroup_CadiTables);
		Adaptation targetTableDefn = targetTableDefinitions.nextAdaptation();
		while ( targetTableDefn != null){
			//Get target data set.
			String targetDatasetName = targetTableDefn.getString(Paths._Root_CADIdefinitions_CADItargetTables._CadiTargetDataset); 
			Adaptation targetDataset = null;
			if (targetDatasetName != null && !targetDatasetName.isEmpty()){
				targetDataset = targetDataspace.findAdaptationOrNull(AdaptationName.forName(targetDatasetName));
			}
			AdaptationTable targetTable;
			if (targetDataset == null){
				//If there is no target data set, the definition is faulty.
				raiseException(ERORR, "Dataset has not been defined or does not exist", targetTableDefn);
				return;
			}else{
				// Check the table exists in the target data set.
				String targetTableName = targetTableDefn.getString(Paths._Root_CADIdefinitions_CADItargetTables._CadiTargetTable);
				if (targetTableName == null || targetTableName.isEmpty()) { 
					raiseException(ERORR,"Table has not been defined",targetTableDefn);
					return;
				}
				Path targetTablePath = Path.parse(targetTableName);
				try{
					targetTable = targetDataset.getTable(targetTablePath);
				} catch (PathAccessException pae){
					raiseException(ERORR, "Target table path error"+pae.getMessage(), targetTableDefn);
					return;
				}
			}
			// ****** INFORMATION CHECK *******
			// targetTableDefn - contains the import table definition.
			// targetTable - is the target table for the import.
			// batchId - is the batch to process.

			if (batchIdPolicy.equals("N")){ batchId = null; }
			if (batchIdPolicy.equals("C")){ batchId = getCadiBatchId(); }

			auditTargetTableDefinition(targetTableDefn, batchId);

			processFiles(targetTableDefn,targetTable,batchId,workingFolder);

			targetTableDefn = targetTableDefinitions.nextAdaptation();
		}

		// ******* ONCE FILE HAVE BEEN PROCESSED *********
		// definitionRecord - contains the import definition

		if ("Y".equals(executeByZip) && zipfiles != null){
			String folderName = null;
			if (hasErrors){
				folderName = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipFailedFolderPath); 
			}else{
				folderName = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipArchiveFolderPath); 				
			}
			for (File file :zipfiles){
				if (file.isFile() && file.canRead() && !file.isHidden() && folderName != null){
					try {
						moveFileToFolder(folderName, file, null);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		launchWorkflow(definitionRecord, batchId);


	}



	private void processFiles(Adaptation targetTableDefn, AdaptationTable targetTable,  String batchId, String workingfolder) {

		//BatchId might be from file or CADI generated value
		log.debug(String.format("Processing files for target table [%s] for batch [%s]", targetTable.getTablePath().format(), batchId));
		//Process the file import definitions for this table and batch
		SourceFiles sourceFiles = new SourceFiles();

		List<Adaptation> filesDefinitions = AdaptationUtil.getLinkedRecordList(targetTableDefn, Paths._Root_CADIdefinitions_CADItargetTables._CadiSourceFiles);		
		for (Adaptation filesDefinition : filesDefinitions){
			log.debug(String.format("Processing table definition [%d] looking for [%s]", targetTableDefn.get_int(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableId),
					targetTableDefn.getString(Paths._Root_CADIdefinitions_CADItargetTables._CadiTargetTable)));
			List<File> files = sourceFiles.findFiles(filesDefinition, batchId, workingfolder, definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiBatchIdPolicy));
			for (File file :files){
				if (file.isFile() && file.canRead() && !file.isHidden()){
					importFile(filesDefinition, targetTable, file, batchId, workingfolder);
				}
			}

		}


	}


	private void importFile(final Adaptation filesDefinition, AdaptationTable targetTable, final File file, String batchId, String workingFolder ){

		String cadiImportType = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadifileImportMode);
		AdixImportSpec adixiSpec = null;
		ImportSpec ispec = null;

		if (cadiImportType.equals("DEX")){
			adixiSpec = getAdixImportSpec(filesDefinition,targetTable);
			adixiSpec.setImportedFile(file);
		}else {
			ispec = getImportSpec(filesDefinition);
			ispec.setTargetAdaptation(targetTable.getContainerAdaptation());
			ispec.setTargetAdaptationTable(targetTable);
			ispec.setSourceFile(file);
		}

		final ImportSpec importSpec = ispec;
		final AdixImportSpec adixImportSpec = adixiSpec;

		final int commitThreshold = filesDefinition.get_int(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCommitThreshold);

		/*
		 * Procedure for importing via Core Import 
		 */
		final Procedure proc = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				pContext.setAllPrivileges(true);
				pContext.setCommitThreshold(commitThreshold);
				pContext.setHistoryActivation(false);
				log.debug(String.format("Importing %s file [%s]", importSpec.getImportMode().format(), importSpec.getSourceFile().getName()));
				ImportResult importResult = null;
				String newError = null;
				try{
					importResult = pContext.doImport(importSpec);
				} catch (OperationException oe){
					log.error(oe.getMessage());
					newError = oe.getMessage();
				}
				pContext.setAllPrivileges(false);
				auditSourceFile(filesDefinition, file, importResult, newError);

			}

		};
		/*
		 * Procedure for importing via Data Exchange 
		 */
		final Procedure dexProc = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				pContext.setAllPrivileges(true);
				pContext.setCommitThreshold(commitThreshold);
				pContext.setHistoryActivation(false);
				log.debug(String.format("Importing %s file [%s]", adixImportSpec.getImportType().toString(), adixImportSpec.getImportedFile().getName()));
				AdixImportResult importResult = null;
				String newError = null;
				AdixImport adixImport = AdixFactory.getAdixImport();
				importResult = adixImport.execute(adixImportSpec);
				importResult.getImportResults().isEmpty();
				pContext.setAllPrivileges(false);
				auditSourceFile(filesDefinition, file, importResult, newError);

			}

		};

		// File Import execution
		ProgrammaticService iService = ProgrammaticService.createForSession(session, targetDataspace);
		String trackingInfo = session.getTrackingInfo();
		trackingInfo = String.format(CADI_TRACKINGINFO + "%s}", batchId);
		log.debug(String.format("Tracking Information [%s]", trackingInfo));
		iService.setSessionTrackingInfo(trackingInfo);
		ProcedureResult result;
		if (cadiImportType.equals("DEX")){
			log.debug(String.format("Importing %s file [%s]", adixImportSpec.getImportType().toString(), adixImportSpec.getImportedFile().getName()));
			AdixImportResult importResult = null;
			String newError = null;
			AdixImport adixImport = AdixFactory.getAdixImport();
			importResult = adixImport.execute(adixImportSpec);
			 List<AdixImportResultBean> importResultBeans = importResult.getImportResults();
			 if (importResultBeans.size() < 1) {
				 log.info("Data exchange has not processed the import");
			 }
			 AdixImportResultBean importResultBean = importResultBeans.get(0);
			 log.debug(String.format("Data Exchange import result\n"
			 		+ "Deleted=%d, Inserted=%d, Unchnaged=%d, Updated=%d, Error rows=%d", 
					 importResultBean.getRowsDeleted(), 
					 importResultBean.getRowsInserted(), 
					 importResultBean.getRowsUnchanged(), 
					 importResultBean.getRowsUpdated(), 
					 importResultBean.getTotalErrorRow()));
			//Throws a stack trace if a CSV is used for an XLS definition.
		}else {
			result = iService.execute(proc);
			final OperationException resultException = result.getException();
			if (result.hasFailed())	{
				try {
					moveFileToFolder(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiFailedFolderPath), file, workingFolder);
				} catch (PathAccessException e) {
					// The data model has changed cannot get failed import folder.
					log.error("Techincal Exception: the data model does not match the CadiPaths entry for 'Cadi_FailedImportFolder' ACCESS PATH EXCEPTION.");
					e.printStackTrace();
					updateCADIStatus(CADI_FAILED);
					return;
				} catch (IOException e) {
					// The move of the file to the failed folder has failed, refer to log output for details.
					log.error("Move File To Failed Folder failed: ["+e.getMessage()+"]");
					e.printStackTrace();
					updateCADIStatus(CADI_FAILED);
					return;
				}
				String message = String.format("File Import execution failed for file [%s] %s",file.getName(),result.getException().getMessage());
				log.error(message);
				//FIXME
				//auditSourceFile(filesDefinition, file, null, result.getExceptionFullMessage(Locale.getDefault()));
				updateCADIStatus(CADI_FAILED);

				return;
			}
		}
		try {
			moveFileToFolder(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiArchiveFolder), file, workingFolder);
		} catch (PathAccessException e) {
			// The data model has changed cannot get failed import folder.
			log.error("Techincal Exception: the data model does not match the CadiPaths entry for 'Cadi_SuccessImportFolder' ACCESS PATH EXCEPTION.");
			e.printStackTrace();
			updateCADIStatus(CADI_FAILED);
			return;
		} catch (IOException e) {
			// The move of the file to the failed folder has failed, refer to log output for details.
			log.error("Move File To Success Folder failed: ["+e.getMessage()+"]");
			e.printStackTrace();
			updateCADIStatus(CADI_FAILED);
			return;
		}
		return;
	}


	private ImportSpec getImportSpec(Adaptation filesDefinition){


		String fileType = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiFileImportMode);
		if (fileType.equals("XML")){
			String importMode = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiXmlImportAttributes_CadiImportMode);
			boolean importMissingValuesAsNull = filesDefinition.get_boolean(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiXmlImportAttributes_CadiSetMissingtoNull);
			boolean importIgnoreExtraFields = filesDefinition.get_boolean(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiXmlImportAttributes_CadiIgnoreExtraCols);
			final ImportSpec importSpec = new ImportSpec();
			importSpec.setImportMode(getImportMode(importMode));
			importSpec.setByDelta(filesDefinition.get_boolean(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiXmlImportAttributes_CadiImportByDelta));
			return importSpec;
		}
		String importMode = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiImportMode);
		String importfileEncoding = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiFileEncoding);
		String importColumnHeader = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiColumnHeader);
		String importFieldSeperator = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiFieldSeparator);
		String importFieldSeperatorChar = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiFieldSeparatorChar);
		String importListSeperator = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiListSeparator);
		String importListSeperatorChar = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiCsvImportAttributes_CadiListSeparatorChar);
		final ImportSpec importSpec = new ImportSpec();
		importSpec.setImportMode(getImportMode(importMode));
		ExportImportCSVSpec csvSpec = new ExportImportCSVSpec();
		csvSpec.setEncoding(importfileEncoding);
		csvSpec.setFieldSeparator(getSeparator(importFieldSeperator,importFieldSeperatorChar));
		csvSpec.setListSeparator(getListSeparator(importListSeperator, importListSeperatorChar));
		csvSpec.setHeader(getHeaderType(importColumnHeader));
		importSpec.setCSVSpec(csvSpec);
		return importSpec;
	}

	private AdixImportSpec getAdixImportSpec(Adaptation filesDefinition, AdaptationTable targetTable) {

		AdixImportSpec iSpec = new AdixImportSpec();
		//For single table
		Adaptation targetDataset = targetTable.getContainerAdaptation();
		String importPreference = filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiDexImportAttributes_DexPreferenceName);
		ImportType importType = toAdixImportType(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiDexImportAttributes_DexImportType));

		ImportDataAccessSpec dataAccessSpec;
		if (importType.isExcelForMultipleTables() || importType.isXMLForMultipleTables()) {
			dataAccessSpec = new ImportDataAccessSpec(targetDataset, Locale.getDefault(), session);
		}else {
			dataAccessSpec = new ImportDataAccessSpec(targetDataset, targetTable, Locale.getDefault(), session);
		}
		iSpec.setDataAccessSpec(dataAccessSpec);
		iSpec.setImportPreference(importPreference);
		iSpec.setImportType(importType);

		return iSpec;
	}

	private ImportType toAdixImportType(String importTypeString) {

		//Implemented types CSV, EXCEL_FOR_SINGLE_TABLE, UNKNOWN (assumed to be XML for single tables)
		if (importTypeString.equals("CSV")) {return ImportType.CSV; }
		if (importTypeString.equals("EXCEL_FOR_SINGLE_TABLE")) {return ImportType.EXCEL_FOR_SINGLE_TABLE; }
		if (importTypeString.equals("EXCEL_FOR_MULTIPLE_TABLES")) {return ImportType.EXCEL_FOR_MULTIPLE_TABLES; }//not defined in model
		if (importTypeString.equals("XML_FOR_MULTIPLE_TABLES")) {return ImportType.XML_FOR_MULTIPLE_TABLES; }//not defined in model
	
		return ImportType.UNKNOWN;
	}

	private Header getHeaderType(String importColumnHeader) {
		if(importColumnHeader.equals("N")){return Header.NONE;
		}else if(importColumnHeader.equals("L")){return Header.LABEL;}
		return Header.PATH_IN_TABLE;
	}

	private char getSeparator(String importFieldSeperator, String importFieldSeperatorChar) {
		if (importFieldSeperator.equals("C")){return ',';
		}else if (importFieldSeperator.equals("S")){return ';';
		}else if (importFieldSeperator.equals("B")){return ' ';
		}else if (importFieldSeperator.equals("T")){return '\t';
		}return importFieldSeperatorChar.charAt(0);
	}

	private String getListSeparator(String importListSeperator, String importListSeperatorChar) {

		if (importListSeperator.equals("L")){
			return "\n";
		}
		return String.valueOf(getSeparator(importListSeperator, importListSeperatorChar));
	}

	private ImportSpecMode getImportMode(String importMode) {
		if (importMode == null){importMode = "U";}
		if (importMode.equals("U")){return ImportSpecMode.UPDATE_OR_INSERT;
		}else if(importMode.equals("I")){return ImportSpecMode.INSERT;
		}else if(importMode.equals("M")){return ImportSpecMode.UPDATE;
		}else{return ImportSpecMode.REPLACE;}
	}

	private void moveFileToFolder(String folderName, File file, String workingFolder) throws IOException {

		if (workingFolder==null){
			log.debug(String.format("Moving file [%s] to folder[%s]", file.getName(), folderName));
			File targetFolder = new File(folderName);
			// Windows locked file fix attempt wait 3 seconds before trying to move the file (5 times)
			boolean fileMoved = false;
			int tryCount = 0;
			while (!fileMoved && tryCount < 5){
				System.gc();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fileMoved = true;
				try{
					FileUtils.moveFileToDirectory(file, targetFolder, true);
				}catch (IOException ioe){
					fileMoved = false;

					tryCount++;
					if (tryCount == 5){
						log.error(String.format("FAILED TO MOVE FILE %s", file.getName()));

					}
				}
			}
		}
	}


	private AdaptationHome prepareImportDataspace(Adaptation definitionRecord, String batchId) {

		// Target data space is set once and once only for the import definition.
		if (targetDataspace !=null){
			log.debug(String.format("Prepare target dataspace: target already set to [%s]", targetDataspace.getKey().format()));
			return targetDataspace;
		}

		AdaptationHome dataspace = Repository.getDefault().lookupHome(HomeKey.parse("B"+definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiDataspace)));

		if(!definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiDataspacePolicy).equals("N")){
			final HomeCreationSpec homeCreationSpec = new HomeCreationSpec();
			String childDataSpaceKey = getSubstitutedText(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiChildDataspaceName),definitionRecord,batchId);
			homeCreationSpec.setKey(HomeKey.forBranchName(childDataSpaceKey));
			String dataspaceOwnerRole = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiOpsRole);
			Profile dataspaceOwner = session.getUserReference();
			if (dataspaceOwnerRole != null && !dataspaceOwnerRole.isEmpty()){ dataspaceOwner = Profile.parse(dataspaceOwnerRole); }
			homeCreationSpec.setOwner(dataspaceOwner);
			homeCreationSpec.setParent(dataspace);
			String permissionsParentName = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiChildDataspacePermissions);
			if (permissionsParentName !=null){
				AdaptationHome permissionsParentDataspace = Repository.getDefault().lookupHome(HomeKey.parse("B"+permissionsParentName));
				if (permissionsParentDataspace != null){
					homeCreationSpec.setHomeToCopyPermissionsFrom(permissionsParentDataspace);
				}
			}
			UserMessage dataSpaceLabel = UserMessage.createInfo(getSubstitutedText(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiChildDataspaceLabel),definitionRecord,batchId));
			if (dataSpaceLabel !=null){
				homeCreationSpec.setLabel((dataSpaceLabel));
			}
			UserMessage dataSpaceDescription = UserMessage.createInfo(getSubstitutedText(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiChildDataspaceDescription),definitionRecord,batchId));
			if (dataSpaceDescription !=null){
				homeCreationSpec.setDescription(dataSpaceDescription);
			}
			log.debug(String.format("Creating child dataspace [%s]", homeCreationSpec.toString()));
			try {
				final AdaptationHome childDataSpace = Repository.getDefault().createHome(homeCreationSpec,session);
				dataspace = childDataSpace;
			} catch (OperationException e) {
				// Failed to create new child data space
				log.error("getTargetDataSpace: failed to create a new child data space, ["+e.getMessage()+"]");
				e.printStackTrace();
				updateCADIStatus(CADI_FAILED);
			}
		}
		targetDataspace = dataspace;
		return dataspace;
	}


	private String getCadiBatchId(){
		if (cadiBatchId == null || cadiBatchId.isEmpty()){
			Date now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.sss");
			cadiBatchId = String.format("cadi%s", dateFormat.format(now));
		}
		return cadiBatchId;

	}

	private void launchWorkflow(Adaptation definitionRecord, String batchId){

		String workflowName = null;
		Map <String,String> wfParameters = new HashMap<String,String>();

		// Collate the workflow launch settings, depending on the error status of the import
		if (hasErrors ){
			String launchWorkflow = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_CadiLaunchFailureWorkflow);
			if (launchWorkflow.equals("N")){ return; }
			workflowName = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_CadiFailedWorkflowNAme);
			wfParameters.put(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_FailedWorkflowParametersGroup_CadiParamDataspace), targetDataspace.getKey().getName());
			wfParameters.put(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_FailedWorkflowParametersGroup_CadiParamDataset), 
					definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiDefaultDataset));
			List<CadiWorkflowParameter> wfPlist = definitionRecord.getList(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_FailedWorkflowParametersGroup_CadiWorkflowParam);
			if (wfPlist != null) { addWfParameters (wfParameters, wfPlist, definitionRecord, batchId); }
		}else{
			String launchWorkflow = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_CadiLaunchWorkflow);
			if (launchWorkflow.equals("N")){ return; }
			workflowName = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_CadiWorkflowName);
			wfParameters.put(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_WorkflowParametersGroup_CadiParamDataspace), targetDataspace.getKey().getName());	
			wfParameters.put(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_WorkflowParametersGroup_CadiParamDataset), 
					definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiDefaultDataset));
			List<CadiWorkflowParameter> wfPlist = definitionRecord.getList(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam);
			if (wfPlist != null) { addWfParameters (wfParameters, wfPlist, definitionRecord, batchId); }		
		}

		Integer cadiProcessId = currentCadiProcess.get_int(Paths._Root_CADIprocesses._CadiProcessId);
		wfParameters.put(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._WorkflowGroup_WorkflowParametersGroup_CadiParamProcessId), cadiProcessId.toString());

		// LAUNCH WORKFLOW

		WorkflowEngine workflowEngine = WorkflowEngine.getFromRepository(Repository.getDefault(), session);
		PublishedProcessKey aKey = PublishedProcessKey.forName(workflowName);
		// TODO:  What if there is no published work flow with this name?
		ProcessLauncher processLauncher = workflowEngine.getProcessLauncher(aKey);
		// TODO: Check launchable

		// Set the work flow label same as data space label
		String dataspaceLabel = getSubstitutedText(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiChildDataspaceLabel), definitionRecord, batchId);
		if (dataspaceLabel !=null ) {
			processLauncher.setLabel(UserMessage.createInfo(dataspaceLabel));
		}

		// Set the work flow description same as data space description
		String dataspaceDescription = getSubstitutedText(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiDescription), definitionRecord, batchId);;
		if (dataspaceDescription !=null){
			processLauncher.setDescription(UserMessage.createInfo(dataspaceDescription));
		}

		for (Entry<String, String> wfParam : wfParameters.entrySet()){
			processLauncher.setInputParameter(wfParam.getKey(),wfParam.getValue());
		}

		try {
			processLauncher.launchProcessWithResult();
		} catch (OperationException e) {
			log.error(String.format("Failed to launch workflow [%s] - %s", workflowName, e.getMessage()));
			e.printStackTrace();
			raiseException(ERORR,String.format("Failed to launch workflow [%s] - %s", workflowName, e.getMessage()) , null);
			return;		// TODO: Should this fail silently?
		}

	}

	private void addWfParameters (Map <String,String> wfParameters, List<CadiWorkflowParameter> wfPlist, Adaptation definitionRecord, String batchId){

		for (CadiWorkflowParameter wfParam : wfPlist){
			wfParameters.put(wfParam.getCadiDatacontextName(), getSubstitutedText(wfParam.getCadiDataValue(), definitionRecord, batchId));
		}

	}


	private String getSubstitutedText(String string, Adaptation definitionRecord, String batchId) {

		log.debug(String.format("Resolve variables in string {%s]", string));
		String id = String.valueOf(definitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId));
		String cadiName = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName);
		String cadiDescription = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiDescription);
		if (cadiDescription == null){cadiDescription = "";}
		String cadiDataset = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiDefaultDataset);
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String dateValue = dateFormat.format(now);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss-SSS");
		String timeValue = timeFormat.format(now);
		Integer cadiOrder = definitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiProcessSequence);
		String owner = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiOwnerProfile);
		String operations = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiOpsRole);
		String datasteward = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiDataSteward);
		String execCode = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiExecutionCode);
		string = string.replaceAll("\\Q${name}\\E", valueOrEmpty(cadiName));
		string = string.replaceAll("\\Q${dataset}\\E", valueOrEmpty(cadiDataset));
		string = string.replaceAll("\\Q${id}\\E", valueOrEmpty(id));
		string = string.replaceAll("\\Q${date}\\E", valueOrEmpty(dateValue));
		string = string.replaceAll("\\Q${time}\\E", valueOrEmpty(timeValue));
		string = string.replaceAll("\\Q${description}\\E", valueOrEmpty(cadiDescription));
		string = string.replaceAll("\\Q${order}\\E", valueOrEmpty(cadiOrder.toString()));
		string = string.replaceAll("\\Q${owner}\\E", valueOrEmpty(owner));
		string = string.replaceAll("\\Q${operations}\\E", valueOrEmpty(operations));
		string = string.replaceAll("\\Q${datasteward}\\E", valueOrEmpty(datasteward));
		string = string.replaceAll("\\Q${code}\\E", valueOrEmpty(execCode));
		string = string.replaceAll("\\Q${batchid}\\E", valueOrEmpty(batchId));
		string = string.replaceAll("\\Q${errors}\\E", errorText);
		log.debug(String.format("Resolved string {%s]", string));
		return string;
	}

	private String valueOrEmpty(String value){
		if (value != null) { return value; }
		return "";
	}

	private void updateCADIStatus(Adaptation record, String status){

		currentCadiProcess = record;
		lastCadiStatus = record.getString(Paths._Root_CADIprocesses._CadiProcessState);
		updateCADIStatus(status);
	}

	private void updateCADIStatus(final String status){

		log.debug(String.format("Override[%s] - Change status from [%s] to [%s]", (cadiProcessStateOverride)?"Yes":"No", lastCadiStatus, status));

		if (cadiProcessStateOverride == false){
			// Do not update status if the state change is invalid
			if (status.equals("Q")) { return; }
			// Cannot be set to 'Queued'
			if (status.equals("H")) { return; }
			// Cannot be set to 'Held'
			if (status.equals("R")) { return; }
			// Cannot be set to 'Released'
			if (status.equals("A") && !lastCadiStatus.equals("Q")) { return; }
			// Cannot be set to 'Active' unless currently 'Queued'
			if (status.equals("C") && !lastCadiStatus.equals("A")) { return; }
			// Cannot be set to 'Completed' unless currently 'Active'
			if (status.equals("T") && !lastCadiStatus.equals("A")) { return; }
			// Cannot be set to 'Terminated' unless currently 'Active'
			//if (status.equals("F") && !lastCadiStatus.equals("A")) { return; }
			// Cannot be set to 'Failed' unless currently 'Active'
		}
		final Procedure proc = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				ValueContextForUpdate vcu = pContext.getContext(currentCadiProcess.getAdaptationName());
				vcu.setValue(status, Paths._Root_CADIprocesses._CadiProcessState);
				Date now = new Date();
				vcu.setValue(now, Paths._Root_CADIprocesses._CadiProcessStateLastModified);
				pContext.setAllPrivileges(true);
				pContext.doModifyContent(currentCadiProcess, vcu);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(proc);
		final OperationException resultException = result.getException();

		// If the import definition sets the import definition to inactive if there are errors
		if (status.equals("F") || status.equals("T")){
			String cadiExceptionPolicy = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiExceptionPolicy);
			// LOG-ONLY, LOG-DEACTIVATE, LOG-DEACTIVATE-MANDATORY
			boolean deactivate = false;
			if(cadiExceptionPolicy.equals("LOG-DEACTIVATE")){
				deactivate = true;
			}
			if(cadiExceptionPolicy.equals("LOG-DEACTIVATE-MANDATORY")){
				boolean tableOptional = currentTableDefinition.get_boolean(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableOptional);
				if (!tableOptional){
					deactivate = true;
				}
			}
			if (deactivate){
				try {
					deactivateDefinition(definitionRecord);
				} catch (OperationException e) {
					log.error(String.format("CANNOT DEACTIVATE Import definition [%d] %s", definitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId),
							definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName)));
					e.printStackTrace();
				}
			}
		}

		lastCadiStatus = status;
		if (lastCadiStatus.equals("C") || lastCadiStatus.equals("F") || lastCadiStatus.equals("T")){
			currentCadiProcess=null;
		}
		//auditDefinitionRecord(currentCadiProcess, status, null);
	}


	/**
	 * Log an exception
	 * @param state - FATAL, ERROR, WARNING, INFORMATION
	 * @param message - Message text
	 * @param record - definition record being processed at the time of the exception.
	 */

	private void raiseException(Integer state, String message, Adaptation record) {

		log.debug(String.format("Raising Exception [%d] %s for %s",state,message, record.getAdaptationName().getStringName()));

		String statusText = "";
		// Update the passed record cadiErrorsList with the new message 
		//Path errorsField = Path.parse("./cadiErrors");
		//List<String> errors = record.getList(errorsField);
		switch(state){
		case 1: 	message = "[FATAL] " + message;
		statusText = "F";
		log.fatal(message);
		break;
		case 2:		message = "[ERROR] " + message;
		statusText = "E";
		log.error(message);
		break;
		case 3:		message = "[WARNING] " + message;
		statusText = "W";
		log.warn(message);
		break;
		case 4:		message = "[INFORMATION] " + message;
		statusText = "I";
		log.info(message);
		break;
		}
		//errors.add(message);
		//Update definition record.
		if (state < 3){
			updateCADIStatus(CADI_FAILED);
			auditDefinitionRecord(currentCadiProcess, statusText, message);
			errorText = message;
			hasErrors = true;
		}

	}


	public boolean executeCPYT(final String importDefCpytPrefix, 
			final String cpytPrefix, 
			final Integer cadiDefnId, 
			final String cadiBatchId, 
			final String tableBatchIdFld, 
			final String targetDataspaceName, 
			final String sourceDataspaceName, 
			final String sourceDatasetName){

		hasErrors=false;

		final Procedure proc = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{

				log.debug(">>>> CADI starting CPYT process ");
				AdaptationHome targetDataspace = Repository.getDefault().lookupHome(HomeKey.parse("B"+targetDataspaceName));
				if (targetDataspace == null){
					String message = "Target dataspace is null, CPYT definitions must use current or named dataspace";
					log.warn(message);

				}
				AdaptationHome sourceDataspace = Repository.getDefault().lookupHome(HomeKey.parse("B"+sourceDataspaceName));
				if (sourceDataspace == null){
					String message = String.format("Attempt to find source dataspace [%s] failed.", sourceDataspaceName);
					log.error(message);
					updateCADIStatus(CADI_FAILED);
					hasErrors = true;
					return;
				}
				if(sourceDatasetName==null){
					log.error("Source data set name is [null]");
					updateCADIStatus(CADI_FAILED);
					hasErrors = true;
					return;
				}
				Adaptation sourceDataset = sourceDataspace.findAdaptationOrNull(AdaptationName.forName(sourceDatasetName));
				if (sourceDataset == null){
					String message = String.format("Attempt to find source dataset [%s] failed.", sourceDatasetName);
					log.error(message);
					updateCADIStatus(CADI_FAILED);
					hasErrors = true;
					return;					
				}
				// Get the list of tables to import 
				RequestResult sourceTableDefinitions = getTableDefnListBySeq(definitionRecord, Paths._Root_CADIdefinitions_CADIdefinition._TablesGroup_CadiTables);
				Adaptation sourceTableDefn = sourceTableDefinitions.nextAdaptation();
				while (sourceTableDefn != null){
					log.debug(String.format("Processing table [%s]", sourceTableDefn.getString(Paths._Root_CADIdefinitions_CADItargetTables._CadiTargetTable)));
					//Get target data set.
					String sourceTableName = sourceTableDefn.getString(Paths._Root_CADIdefinitions_CADItargetTables._CadiTargetTable);
					if (sourceTableName == null || sourceTableName.isEmpty()) { 
						log.error(String.format("Table not defined for definition id [%d]", sourceTableDefn.get_int(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableId)));
						raiseException(ERORR,"Table has not been defined",sourceTableDefn);
						return;
					}
					log.debug(String.format("Preparing table [%s]", sourceTableName));
					Path sourceTablePath = Path.parse(sourceTableName);
					AdaptationTable sourceTable = sourceDataset.getTable(sourceTablePath);
					if (sourceTable == null){
						log.error(String.format("Table [%s] not found for definition id [%d]", sourceTablePath, sourceTableDefn.get_int(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableId)));
						raiseException(ERORR,"Defined table has not been found",sourceTableDefn);
						return;
					}
					// Have the table, now compose the transformation name

					String transformationName = importDefCpytPrefix + cpytPrefix + sourceTablePath.getLastStep().format();

					log.debug(String.format("CPYT start: Transformation [%s], Target dataspace [%s], Source table [%s], Batch Id [%s], Table batch id field [%s] ",
							transformationName, targetDataspace.getKey().format(), sourceTable.getTablePath().format(), cadiBatchId, tableBatchIdFld));

				//	CopyTransform cpyt = new CopyTransform(transformationName, targetDataspace);

					RequestResult records = getChangedRecords(sourceTable, cadiBatchId, tableBatchIdFld);
					log.debug(String.format("Found [%d] records to process", records.getSize()));
					Adaptation record = records.nextAdaptation();
					while (record != null){
				//		cpyt.processMappings(record, pContext);
						record = records.nextAdaptation();
					}
					sourceTableDefn = sourceTableDefinitions.nextAdaptation();
				}
				log.debug(">>>> CADI completed CPYT process ");

			}
		};

		pService.execute(proc);

		return hasErrors;
	}

	private RequestResult getChangedRecords(AdaptationTable sourceTable, String cadiBatchId,
			String tableBatchIdFld) {

		String predicate = String.format("./%s = '%s'", tableBatchIdFld, cadiBatchId);
		RequestResult rr = sourceTable.createRequestResult(predicate);

		return rr;
	}

	private RequestResult getTableDefnListBySeq(Adaptation definitionRecord, Path associationNode){

		String predicate = String.format("%s = '%d'", Paths._Root_CADIdefinitions_CADItargetTables._CadiDefinition.format(), definitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId));
		AdaptationTable targetTable = cadiDataset.getTable(Paths._Root_CADIdefinitions_CADItargetTables.getPathInSchema());
		RequestSortCriteria sortCriteria = new RequestSortCriteria();
		sortCriteria.add(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableSequence);
		RequestResult rr = targetTable.createRequestResult(predicate, sortCriteria);

		log.debug(String.format("Request Result [%s] contains [%d] table definitions using predicate [%s]", rr.toString(), rr.getSize(), predicate));

		return rr;
	}

	public void updateStatus(String cadiProcessId, String statusValue) {

		cadiProcessStateOverride  = true;
		AdaptationTable processTable = cadiDataset.getTable(Paths._Root_CADIprocesses.getPathInSchema());
		currentCadiProcess = processTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(cadiProcessId));
		updateCADIStatus(statusValue);
		cadiProcessStateOverride = false;
	}


	public void deactivateDefinition(final Adaptation definitionRecord) throws OperationException{

		final Procedure definitionUpdate = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				ValueContextForUpdate vcu = pContext.getContext(definitionRecord.getAdaptationName());
				vcu.setValue(false, Paths._Root_CADIdefinitions_CADIdefinition._CadiActive);
				pContext.setAllPrivileges(true);
				pContext.doModifyContent(definitionRecord, vcu);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(definitionUpdate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

	//*******************************************
	//   A U D I T   M E T H O D S
	//*******************************************

	/**
	 * Create or update an audit record for the import definition
	 * @param process Record - executing process record.
	 * @param definitionRecord - original import definition record
	 * @param action - 'create': create a new definition audit record,  'status': aligns the status with the process record.
	 */

	private void auditDefinitionRecord(Adaptation processRecord,  String processState, String newError) {

		currentCadiImportRecord = processRecord;

		if (definitionRecord == null){
			log.error("CANNOT WRITE TO CADI Definition Import process table as there is no currenr 'definitionRecord'");
			return;
		}
		log.debug(" >>> WRITE AUDIT FOR IMPORT DEFINITON <<<");

		//Using the process record, find the definition record.
		Adaptation definitionRecord = AdaptationUtil.followFK(processRecord, Paths._Root_CADIprocesses._CadiLaunchDefinition);
		//Using the definition record id and process id, see if there is a record already.
		Integer definitionId = definitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId);
		Integer processId = processRecord.get_int(Paths._Root_CADIprocesses._CadiProcessId);
		String pkString = String.format("%d|%d", processId, definitionId);
		AdaptationTable importProcessTable = definitionRecord.getContainer().getTable(Paths._Root_CADImonitor_CADIImportProcess.getPathInSchema());
		currentImportProcessRecord = importProcessTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(pkString));


		//If a record does not already exist, create it 
		if (currentImportProcessRecord == null){
			try {
				auditCreateImportProcessRecord(definitionRecord, processId, processState, importProcessTable);
			} catch (OperationException e) {
				log.error(String.format("Failed to create Import process record: [%s]", e.getMessage()));
				e.printStackTrace();
			}
		}
		if (currentImportProcessRecord == null){
			log.error("Expecting Import process record which is not available");
		}
		try {
			auditUpdateImportProcessRecord(processState, newError);
		} catch (OperationException e) {
			log.error(String.format("Failed to update Import process record: [%s]", e.getMessage()));
			e.printStackTrace();
		}
	}


	private void auditCreateImportProcessRecord( final Adaptation definitionRecord ,final Integer processId, final String processState, final AdaptationTable importProcessTable) throws OperationException{

		final Procedure definitionCreate = new Procedure()
		{


			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				ValueContextForUpdate vcu = pContext.getContextForNewOccurrence(importProcessTable);

				vcu.setValue(definitionRecord.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportProcess._Cadiid);
				vcu.setValue(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName), Paths._Root_CADImonitor_CADIImportProcess._CadiName);
				vcu.setValue(Integer.toString(processId), Paths._Root_CADImonitor_CADIImportProcess._CadiProcessId);
				vcu.setValue(processState, Paths._Root_CADImonitor_CADIImportProcess._CadiProcessState);
				vcu.setValue(definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._DataspaceGroup_CadiDefaultDataset), Paths._Root_CADImonitor_CADIImportProcess._CadiTargetDataset);
				vcu.setValue(targetDataspace.getKey().format(), Paths._Root_CADImonitor_CADIImportProcess._CadiTargetDataspace);
				pContext.setAllPrivileges(true);
				currentImportProcessRecord = pContext.doCreateOccurrence(vcu, importProcessTable);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(definitionCreate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

	private void auditUpdateImportProcessRecord(final String processState, final String newError) throws OperationException{

		final Procedure definitionUpdate = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				//Find existing import process record

				ValueContextForUpdate vcu = pContext.getContext(currentImportProcessRecord.getAdaptationName());
				if (newError!=null){
					@SuppressWarnings("unchecked")
					List<String> errors = vcu.getAdaptationInstance().getList(Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
					errors.add(newError);
					vcu.setValue(errors, Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
				}

				if (processState!=null){
					vcu.setValue(processState, Paths._Root_CADImonitor_CADIImportProcess._CadiProcessState);
				}


				pContext.setAllPrivileges(true);
				pContext.doModifyContent(currentImportProcessRecord, vcu);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(definitionUpdate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

	private void auditTargetTableDefinition(Adaptation targetTableDefn, String processBatchId) {

		currentTableDefinition = targetTableDefn;
		if (currentTableDefinition == null || currentImportProcessRecord ==null){
			log.error("CANNOT WRITE TO CADI Definition Import Target Table table as there is no current 'currentTableDefinition'");
			return;
		}
		log.debug(">>> WRITE AUDIT FOR IMPORT TABLE DEFINITON <<<");
		//Using the process record, find the definition record.
		Adaptation definitionRecord = AdaptationUtil.followFK(targetTableDefn, Paths._Root_CADIdefinitions_CADItargetTables._CadiDefinition);

		//Using the definition record id and process id, see if there is a record already.
		String processRecordPk = currentImportProcessRecord.getOccurrencePrimaryKey().format();
		String targetTableDefnPk = targetTableDefn.getOccurrencePrimaryKey().format();
		String pkString = String.format("%s|%s", processRecordPk, targetTableDefnPk);
		AdaptationTable importTableTable = definitionRecord.getContainer().getTable(Paths._Root_CADImonitor_CADIImportTables.getPathInSchema());
		Adaptation importTableRecord = importTableTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(pkString));

		//If a record does not already exist, create it 
		if (importTableRecord == null){
			try {
				auditCreateImportTableRecord(targetTableDefn, null, importTableTable, processBatchId);
			} catch (OperationException e) {
				log.error(String.format("Failed to create Import process record: [%s]", e.getMessage()));
				e.printStackTrace();
			}
		}
		if (importTableRecord == null){
			log.error("Expecting Import process record which is not available");
		}

	}


	private void auditCreateImportTableRecord(final Adaptation targetTableDefn, final String processState, final AdaptationTable importProcessTable, final String processBatchId) throws OperationException{

		final Procedure importTableCreate = new Procedure()
		{


			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				ValueContextForUpdate vcu = pContext.getContextForNewOccurrence(importProcessTable);

				vcu.setValue(currentTableDefinition.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportTables._CadiTableId);
				vcu.setValue(processBatchId, Paths._Root_CADImonitor_CADIImportTables._CadiBatchId);
				vcu.setValue(currentImportProcessRecord.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportTables._CadiDefinition);
				vcu.setValue(processState, Paths._Root_CADImonitor_CADIImportTables._CadiProcessState);
				vcu.setValue(currentTableDefinition.get_boolean(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableOptional), Paths._Root_CADImonitor_CADIImportTables._CadiTableOptional);
				vcu.setValue(currentTableDefinition.get_int(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableSequence), Paths._Root_CADImonitor_CADIImportTables._CadiTableSequence);
				vcu.setValue(targetDataspace.getKey().format(), Paths._Root_CADImonitor_CADIImportTables._CadiTargetDataspace);
				vcu.setValue(currentTableDefinition.getString(Paths._Root_CADImonitor_CADIImportProcess._CadiTargetDataset), Paths._Root_CADImonitor_CADIImportTables._CadiTargetDataset);
				vcu.setValue(targetTableDefn.getString(Paths._Root_CADIdefinitions_CADItargetTables._CadiTargetTable), Paths._Root_CADImonitor_CADIImportTables._CadiTargetTable);


				pContext.setAllPrivileges(true);
				currentImportTableRecord = pContext.doCreateOccurrence(vcu, importProcessTable);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(importTableCreate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

	private void auditUpdateImportTableRecord(final String newError) throws OperationException{

		final Procedure definitionUpdate = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				//Find existing import process record

				ValueContextForUpdate vcu = pContext.getContext(currentTableDefinition.getAdaptationName());
				if (newError!=null){
					@SuppressWarnings("unchecked")
					List<String> errors = vcu.getAdaptationInstance().getList(Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
					errors.add(newError);
					vcu.setValue(errors, Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
				}


				pContext.setAllPrivileges(true);
				pContext.doModifyContent(currentTableDefinition, vcu);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(definitionUpdate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}


	private void auditSourceFile(Adaptation filesDefinition, File file, AdixImportResult importResult, String newError ) {

		currentFilesDefinition = filesDefinition;

		if (currentFilesDefinition == null){
			log.error("CANNOT WRITE TO CADI Definition Import File table as there is no current 'currentFilesDefinition'");
			return;
		}
		log.debug(">>> WRITE AUDIT FOR IMPORT FILE DEFINITON <<<");
		//Using the process record, find the definition record.

		//Using the definition record id and process id, see if there is a record already.
		if (currentImportTableRecord == null){
			log.error("ISSUE finding current process record");
			return;
		}
		String importTableRecordPk = currentImportTableRecord.getOccurrencePrimaryKey().format();
		String fileDefinitionPk = filesDefinition.getOccurrencePrimaryKey().format();
		String pkString = String.format("%s|%s", importTableRecordPk, fileDefinitionPk);
		AdaptationTable importFilesTable = currentTableDefinition.getContainer().getTable(Paths._Root_CADImonitor_CADIImportedFiles.getPathInSchema());
		Adaptation importFileRecord = importFilesTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(pkString));

		//If a record does not already exist, create it 
		if (importFileRecord == null){
			try {
				auditCreateImportFileRecord(filesDefinition, importFilesTable, file, importResult, newError);
			} catch (OperationException e) {
				log.error(String.format("Failed to create Import file record: [%s]", e.getMessage()));
				e.printStackTrace();
			}
		}else{
			if (newError != null){
				try {
					auditUpdateImportFileRecord(importFileRecord,newError);
				} catch (OperationException e) {
					log.error(String.format("Failed to update ImportFileRecord [%s]", e.getMessage()));
					e.printStackTrace();
				}
			}
		}
	}


	private void auditSourceFile(Adaptation filesDefinition, File file, ImportResult importResult, String newError ) {

		currentFilesDefinition = filesDefinition;

		if (currentFilesDefinition == null){
			log.error("CANNOT WRITE TO CADI Definition Import File table as there is no current 'currentFilesDefinition'");
			return;
		}
		log.debug(">>> WRITE AUDIT FOR IMPORT FILE DEFINITON <<<");
		//Using the process record, find the definition record.

		//Using the definition record id and process id, see if there is a record already.
		if (currentImportTableRecord == null){
			log.error("ISSUE finding current process record");
			return;
		}
		String importTableRecordPk = currentImportTableRecord.getOccurrencePrimaryKey().format();
		String fileDefinitionPk = filesDefinition.getOccurrencePrimaryKey().format();
		String pkString = String.format("%s|%s", importTableRecordPk, fileDefinitionPk);
		AdaptationTable importFilesTable = currentTableDefinition.getContainer().getTable(Paths._Root_CADImonitor_CADIImportedFiles.getPathInSchema());
		Adaptation importFileRecord = importFilesTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(pkString));

		//If a record does not already exist, create it 
		if (importFileRecord == null){
			try {
				auditCreateImportFileRecord(filesDefinition, importFilesTable, file, importResult, newError);
			} catch (OperationException e) {
				log.error(String.format("Failed to create Import file record: [%s]", e.getMessage()));
				e.printStackTrace();
			}
		}else{
			if (newError != null){
				try {
					auditUpdateImportFileRecord(importFileRecord,newError);
				} catch (OperationException e) {
					log.error(String.format("Failed to update ImportFileRecord [%s]", e.getMessage()));
					e.printStackTrace();
				}
			}
		}


	}

	private void auditCreateImportFileRecord(final Adaptation filesDefinition, 
			final AdaptationTable importFilesTable, final File file, final ImportResult importResult, final String newError) throws OperationException {


		final Procedure importFileCreate = new Procedure()
		{


			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				ValueContextForUpdate vcu = pContext.getContextForNewOccurrence(importFilesTable);

				vcu.setValue(currentImportTableRecord.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportedFiles._CadiTatgetTable);
				vcu.setValue(filesDefinition.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportedFiles._CadiImportFileId);
				vcu.setValue(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiFileDescription), Paths._Root_CADImonitor_CADIImportedFiles._CadiFileDescription);
				vcu.setValue(file.getPath(), Paths._Root_CADImonitor_CADIImportedFiles._CadiSourceFileGroup_CadiFileFolder);
				vcu.setValue(file.getName(), Paths._Root_CADImonitor_CADIImportedFiles._CadiSourceFileGroup_CadiFileName);
				vcu.setValue(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiFileImportMode), Paths._Root_CADImonitor_CADIImportedFiles._CadiSourceFileGroup_CadiFileImportMode);
				if (importResult != null){
					vcu.setValue(importResult.getCreationQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsCreated);
					vcu.setValue(importResult.getUpdateQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsUpdated);
					vcu.setValue(importResult.getDeletionQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsDeleted);
					vcu.setValue(importResult.getUnchangeQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsUnchanged);
				}

				if (newError!=null){
					@SuppressWarnings("unchecked")
					List<String> errors = null;   /// = vcu.getAdaptationInstance().getList(Paths._Root_CADImonitor_CADIImportedFiles._CadiErrors);
					errors.add(newError);
					vcu.setValue(errors, Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
					vcu.setValue("F", Paths._Root_CADImonitor_CADIImportedFiles._CadiProcessState);
				}else{
					vcu.setValue("C", Paths._Root_CADImonitor_CADIImportedFiles._CadiProcessState);
				}

				pContext.setAllPrivileges(true);
				currentImportFileRecord = pContext.doCreateOccurrence(vcu, importFilesTable);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(importFileCreate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

	private void auditCreateImportFileRecord(final Adaptation filesDefinition, 
			final AdaptationTable importFilesTable, final File file, final AdixImportResult importResult, final String newError) throws OperationException {


		final Procedure importFileCreate = new Procedure()
		{


			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				ValueContextForUpdate vcu = pContext.getContextForNewOccurrence(importFilesTable);

				vcu.setValue(currentImportTableRecord.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportedFiles._CadiTatgetTable);
				vcu.setValue(filesDefinition.getOccurrencePrimaryKey().format(), Paths._Root_CADImonitor_CADIImportedFiles._CadiImportFileId);
				vcu.setValue(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiFileDescription), Paths._Root_CADImonitor_CADIImportedFiles._CadiFileDescription);
				vcu.setValue(file.getPath(), Paths._Root_CADImonitor_CADIImportedFiles._CadiSourceFileGroup_CadiFileFolder);
				vcu.setValue(file.getName(), Paths._Root_CADImonitor_CADIImportedFiles._CadiSourceFileGroup_CadiFileName);
				vcu.setValue(filesDefinition.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiFileImportMode), Paths._Root_CADImonitor_CADIImportedFiles._CadiSourceFileGroup_CadiFileImportMode);
				if (importResult != null){
					//					vcu.setValue(importResult.getCreationQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsCreated);
					//					vcu.setValue(importResult.getUpdateQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsUpdated);
					//					vcu.setValue(importResult.getDeletionQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsDeleted);
					//					vcu.setValue(importResult.getUnchangeQty(), Paths._Root_CADImonitor_CADIImportedFiles._CadiRecordsUnchanged);
				}

				if (newError!=null){
					@SuppressWarnings("unchecked")
					List<String> errors = null;   /// = vcu.getAdaptationInstance().getList(Paths._Root_CADImonitor_CADIImportedFiles._CadiErrors);
					errors.add(newError);
					vcu.setValue(errors, Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
					vcu.setValue("F", Paths._Root_CADImonitor_CADIImportedFiles._CadiProcessState);
				}else{
					vcu.setValue("C", Paths._Root_CADImonitor_CADIImportedFiles._CadiProcessState);
				}

				pContext.setAllPrivileges(true);
				currentImportFileRecord = pContext.doCreateOccurrence(vcu, importFilesTable);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(importFileCreate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

	private void auditUpdateImportFileRecord(final Adaptation importFileRecord, final String newError) throws OperationException {

		final Procedure fileRecordUpdate = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				//Find existing import process record

				ValueContextForUpdate vcu = pContext.getContext(importFileRecord.getAdaptationName());
				if (newError!=null){
					@SuppressWarnings("unchecked")
					List<String> errors = vcu.getAdaptationInstance().getList(Paths._Root_CADImonitor_CADIImportedFiles._CadiErrors);
					errors.add(newError);
					vcu.setValue(errors, Paths._Root_CADImonitor_CADIImportProcess._ExecutionGroup_CadiErrors);
					vcu.setValue("F", Paths._Root_CADImonitor_CADIImportedFiles._CadiProcessState);
				}


				pContext.setAllPrivileges(true);
				pContext.doModifyContent(importFileRecord, vcu);
				pContext.setAllPrivileges(false);
			}

		};
		// File Import execution
		final ProcedureResult result = pService.execute(fileRecordUpdate);
		if (result.hasFailed()){
			final OperationException resultException = result.getException();
			throw resultException;
		}

	}

}

