package com.ebxps.cadif;

import com.orchestranetworks.schema.Path;
/**
 * Generated by EBX5 5.7.1 fix C [1034:0007], at  2017/03/17 17:25:19 [GMT].
 * WARNING: Any manual changes to this class may be overwritten by generation process.
 * DO NOT MODIFY THIS CLASS.
 * 
 * This interface defines constants related to schema [Module: ebxps-cadi, path: /WEB-INF/ebx/schemas/cadi2.xsd].
 * 
 * Root paths in this interface: 
 * 	'/'   relativeToRoot: true
 * 
 */
public interface Paths
{
	// ===============================================
	// Constants for nodes under '/'.
	// Prefix:  ''.
	// Statistics:
	//		219 path constants.
	//		135 leaf nodes.
	public static final Path _Root = Path.parse("./root");

	// Table type path
	public final class _Root_CADIprocesses {
		private static final Path _Root_CADIprocesses = Path.parse("./root/CADIprocesses");
		public static Path getPathInSchema()
		{
			return _Root_CADIprocesses;
		}
	public static final Path _CadiProcessId = Path.parse("./cadiProcessId");
	public static final Path _CadiProcess = Path.parse("./cadiProcess");
	public static final Path _CadiLaunchRequestTime = Path.parse("./cadiLaunchRequestTime");
	public static final Path _CadiProcessState = Path.parse("./cadiProcessState");
	public static final Path _CadiProcessStateLastModified = Path.parse("./cadiProcessStateLastModified");
	public static final Path _CadiLaunchDefinition = Path.parse("./cadiLaunchDefinition");
	public static final Path _CadiLaunchDefinitionCode = Path.parse("./cadiLaunchDefinitionCode");
	public static final Path _CadiBatchId = Path.parse("./cadiBatchId");
	} 
	public static final Path _Root_CADIdefinitions = Path.parse("./root/CADIdefinitions");

	// Table type path
	public final class _Root_CADIdefinitions_CADIdefinition {
		private static final Path _Root_CADIdefinitions_CADIdefinition = Path.parse("./root/CADIdefinitions/CADIdefinition");
		public static Path getPathInSchema()
		{
			return _Root_CADIdefinitions_CADIdefinition;
		}
	public static final Path _CadiId = Path.parse("./cadiId");
	public static final Path _CadiName = Path.parse("./cadiName");
	public static final Path _CadiDescription = Path.parse("./cadiDescription");
	public static final Path _CadiProcessSequence = Path.parse("./cadiProcessSequence");
	public static final Path _CadiOwnerProfile = Path.parse("./cadiOwnerProfile");
	public static final Path _CadiOpsRole = Path.parse("./cadiOpsRole");
	public static final Path _CadiDataSteward = Path.parse("./cadiDataSteward");
	public static final Path _CadiExecutionCode = Path.parse("./cadiExecutionCode");
	public static final Path _CadiExclusivity = Path.parse("./cadiExclusivity");
	public static final Path _CadiExceptionPolicy = Path.parse("./cadiExceptionPolicy");
	public static final Path _CadiBatchIdPolicy = Path.parse("./cadiBatchIdPolicy");
	public static final Path _CadiExecByZip = Path.parse("./cadiExecByZip");
	public static final Path _CadiExecByBatch = Path.parse("./cadiExecByBatch");
	public static final Path _CadiMonitor = Path.parse("./cadiMonitor");
	public static final Path _CadiActive = Path.parse("./cadiActive");
	public static final Path _ExecutionGroup = Path.parse("./executionGroup");
	public static final Path _ExecutionGroup_CadiLastModified = Path.parse("./executionGroup/cadiLastModified");
	public static final Path _ExecutionGroup_CadiLastUsed = Path.parse("./executionGroup/cadiLastUsed");
	public static final Path _ExecutionGroup_CadiLastSuccessful = Path.parse("./executionGroup/cadiLastSuccessful");
	public static final Path _ExecutionGroup_CadiLastFailed = Path.parse("./executionGroup/cadiLastFailed");
	public static final Path _DataspaceGroup = Path.parse("./dataspaceGroup");
	public static final Path _DataspaceGroup_CadiDataspacePolicy = Path.parse("./dataspaceGroup/cadiDataspacePolicy");
	public static final Path _DataspaceGroup_CadiDataspace = Path.parse("./dataspaceGroup/cadiDataspace");
	public static final Path _DataspaceGroup_CadiChildDataspaceName = Path.parse("./dataspaceGroup/cadiChildDataspaceName");
	public static final Path _DataspaceGroup_CadiChildDataspaceLabel = Path.parse("./dataspaceGroup/cadiChildDataspaceLabel");
	public static final Path _DataspaceGroup_CadiChildDataspaceDescription = Path.parse("./dataspaceGroup/cadiChildDataspaceDescription");
	public static final Path _DataspaceGroup_CadiChildDataspacePermissions = Path.parse("./dataspaceGroup/cadiChildDataspacePermissions");
	public static final Path _DataspaceGroup_CadiDataspaceClosure = Path.parse("./dataspaceGroup/cadiDataspaceClosure");
	public static final Path _DataspaceGroup_CadiDefaultDataset = Path.parse("./dataspaceGroup/cadiDefaultDataset");
	public static final Path _DataspaceGroup_CadiResolvedDataspaceName = Path.parse("./dataspaceGroup/cadiResolvedDataspaceName");
	public static final Path _ZipFileGroup = Path.parse("./zipFileGroup");
	public static final Path _ZipFileGroup_CadiZipFileName = Path.parse("./zipFileGroup/cadiZipFileName");
	public static final Path _ZipFileGroup_CadiZipFolderPath = Path.parse("./zipFileGroup/cadiZipFolderPath");
	public static final Path _ZipFileGroup_CadiZipBatchIdRegex = Path.parse("./zipFileGroup/cadiZipBatchIdRegex");
	public static final Path _ZipFileGroup_CadiZipWorkingFolderPath = Path.parse("./zipFileGroup/cadiZipWorkingFolderPath");
	public static final Path _ZipFileGroup_CadiZipArchiveFolderPath = Path.parse("./zipFileGroup/cadiZipArchiveFolderPath");
	public static final Path _ZipFileGroup_CadiZipKeepArchiveHours = Path.parse("./zipFileGroup/cadiZipKeepArchiveHours");
	public static final Path _ZipFileGroup_CadiZipFailedFolderPath = Path.parse("./zipFileGroup/cadiZipFailedFolderPath");
	public static final Path _ZipFileGroup_CadiZipKeepFailedHours = Path.parse("./zipFileGroup/cadiZipKeepFailedHours");
	public static final Path _ZipFileGroup_CadiCrcFileNameExtension = Path.parse("./zipFileGroup/cadiCrcFileNameExtension");
	public static final Path _ZipFileGroup_CadiZipNotUsed = Path.parse("./zipFileGroup/cadiZipNotUsed");
	public static final Path _TablesGroup = Path.parse("./tablesGroup");
	public static final Path _TablesGroup_CadiTables = Path.parse("./tablesGroup/cadiTables");
	public static final Path _WorkflowGroup = Path.parse("./workflowGroup");
	public static final Path _WorkflowGroup_CadiLaunchWorkflow = Path.parse("./workflowGroup/cadiLaunchWorkflow");
	public static final Path _WorkflowGroup_CadiWorkflowName = Path.parse("./workflowGroup/cadiWorkflowName");
	public static final Path _WorkflowGroup_WorkflowParametersGroup = Path.parse("./workflowGroup/workflowParametersGroup");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiParamProcessId = Path.parse("./workflowGroup/workflowParametersGroup/cadiParamProcessId");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiParamDataspace = Path.parse("./workflowGroup/workflowParametersGroup/cadiParamDataspace");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiParamDataset = Path.parse("./workflowGroup/workflowParametersGroup/cadiParamDataset");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam = Path.parse("./workflowGroup/workflowParametersGroup/cadiWorkflowParam");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam_CadiDataValue = Path.parse("./workflowGroup/workflowParametersGroup/cadiWorkflowParam/cadiDataValue");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam_CadiDatacontextName = Path.parse("./workflowGroup/workflowParametersGroup/cadiWorkflowParam/cadiDatacontextName");
	public static final Path _WorkflowGroup_CadiLaunchFailureWorkflow = Path.parse("./workflowGroup/cadiLaunchFailureWorkflow");
	public static final Path _WorkflowGroup_CadiFailedWorkflowNAme = Path.parse("./workflowGroup/cadiFailedWorkflowNAme");
	public static final Path _WorkflowGroup_FailedWorkflowParametersGroup = Path.parse("./workflowGroup/failedWorkflowParametersGroup");
	public static final Path _WorkflowGroup_FailedWorkflowParametersGroup_CadiParamDataspace = Path.parse("./workflowGroup/failedWorkflowParametersGroup/cadiParamDataspace");
	public static final Path _WorkflowGroup_FailedWorkflowParametersGroup_CadiParamDataset = Path.parse("./workflowGroup/failedWorkflowParametersGroup/cadiParamDataset");
	public static final Path _WorkflowGroup_FailedWorkflowParametersGroup_CadiWorkflowParam = Path.parse("./workflowGroup/failedWorkflowParametersGroup/cadiWorkflowParam");
	public static final Path _WorkflowGroup_FailedWorkflowParametersGroup_CadiWorkflowParam_CadiDataValue = Path.parse("./workflowGroup/failedWorkflowParametersGroup/cadiWorkflowParam/cadiDataValue");
	public static final Path _WorkflowGroup_FailedWorkflowParametersGroup_CadiWorkflowParam_CadiDatacontextName = Path.parse("./workflowGroup/failedWorkflowParametersGroup/cadiWorkflowParam/cadiDatacontextName");
	} 

	// Table type path
	public final class _Root_CADIdefinitions_CADItargetTables {
		private static final Path _Root_CADIdefinitions_CADItargetTables = Path.parse("./root/CADIdefinitions/CADItargetTables");
		public static Path getPathInSchema()
		{
			return _Root_CADIdefinitions_CADItargetTables;
		}
	public static final Path _CadiTableId = Path.parse("./cadiTableId");
	public static final Path _CadiDefinition = Path.parse("./cadiDefinition");
	public static final Path _CadiTableSequence = Path.parse("./cadiTableSequence");
	public static final Path _CadiReferentialDataspaceName = Path.parse("./cadiReferentialDataspaceName");
	public static final Path _CadiTargetDataspace = Path.parse("./cadiTargetDataspace");
	public static final Path _CadiTargetDataset = Path.parse("./cadiTargetDataset");
	public static final Path _CadiTargetTable = Path.parse("./cadiTargetTable");
	public static final Path _CadiTableOptional = Path.parse("./cadiTableOptional");
	public static final Path _CadiBatchIdPolicy = Path.parse("./cadiBatchIdPolicy");
	public static final Path _CadiSourceFiles = Path.parse("./cadiSourceFiles");
	} 

	// Table type path
	public final class _Root_CADIdefinitions_CADIimportFiles {
		private static final Path _Root_CADIdefinitions_CADIimportFiles = Path.parse("./root/CADIdefinitions/CADIimportFiles");
		public static Path getPathInSchema()
		{
			return _Root_CADIdefinitions_CADIimportFiles;
		}
	public static final Path _CadiId = Path.parse("./cadiId");
	public static final Path _CadiTatgetTable = Path.parse("./cadiTatgetTable");
	public static final Path _CadiFileDescription = Path.parse("./cadiFileDescription");
	public static final Path _CadifileImportMode = Path.parse("./cadiSourceFileGroup/cadiFileImportMode");
	public static final Path _CadiSourceFileGroup = Path.parse("./cadiSourceFileGroup");
	public static final Path _CadiSourceFileGroup_CadiFileName = Path.parse("./cadiSourceFileGroup/cadiFileName");
	public static final Path _CadiSourceFileGroup_CadiFileFolder = Path.parse("./cadiSourceFileGroup/cadiFileFolder");
	public static final Path _CadiSourceFileGroup_CadiFileImportMode = Path.parse("./cadiSourceFileGroup/cadiFileImportMode");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiImportMode = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiImportMode");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiFileEncoding = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiFileEncoding");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiColumnHeader = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiColumnHeader");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiFieldSeparator = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiFieldSeparator");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiFieldSeparatorChar = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiFieldSeparatorChar");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiListSeparator = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiListSeparator");
	public static final Path _CadiSourceFileGroup_CadiCsvImportAttributes_CadiListSeparatorChar = Path.parse("./cadiSourceFileGroup/cadiCsvImportAttributes/cadiListSeparatorChar");
	public static final Path _CadiSourceFileGroup_CadiXmlImportAttributes = Path.parse("./cadiSourceFileGroup/cadiXmlImportAttributes");
	public static final Path _CadiSourceFileGroup_CadiXmlImportAttributes_CadiImportMode = Path.parse("./cadiSourceFileGroup/cadiXmlImportAttributes/cadiImportMode");
	public static final Path _CadiSourceFileGroup_CadiXmlImportAttributes_CadiImportByDelta = Path.parse("./cadiSourceFileGroup/cadiXmlImportAttributes/cadiImportByDelta");
	public static final Path _CadiSourceFileGroup_CadiXmlImportAttributes_CadiSetMissingtoNull = Path.parse("./cadiSourceFileGroup/cadiXmlImportAttributes/cadiSetMissingtoNull");
	public static final Path _CadiSourceFileGroup_CadiXmlImportAttributes_CadiIgnoreExtraCols = Path.parse("./cadiSourceFileGroup/cadiXmlImportAttributes/cadiIgnoreExtraCols");
	public static final Path _CadiSourceFileGroup_CadiDexImportAttributes_DexImportType = Path.parse("./cadiSourceFileGroup/cadiDexImportAttributes/dexImportType");
	public static final Path _CadiSourceFileGroup_CadiDexImportAttributes_DexPreferenceName = Path.parse("./cadiSourceFileGroup/cadiDexImportAttributes/dexPreferenceName");
	public static final Path _CadiSourceFileGroup_CadiCommitThreshold = Path.parse("./cadiSourceFileGroup/cadiCommitThreshold");
	public static final Path _CadiSourceFileGroup_CadiArchiveFolder = Path.parse("./cadiSourceFileGroup/cadiArchiveFolder");
	public static final Path _CadiSourceFileGroup_CadiKeepArchiveHours = Path.parse("./cadiSourceFileGroup/cadiKeepArchiveHours");
	public static final Path _CadiSourceFileGroup_CadiFailedFolderPath = Path.parse("./cadiSourceFileGroup/cadiFailedFolderPath");
	public static final Path _CadiSourceFileGroup_CadiKeepFailedHours = Path.parse("./cadiSourceFileGroup/cadiKeepFailedHours");
	public static final Path _CadiSourceFileGroup_CadiBatchIdRegex = Path.parse("./cadiSourceFileGroup/cadiBatchIdRegex");
	public static final Path _CadiCrcFileName = Path.parse("./cadiCrcFileName");
	} 
	public static final Path _Root_CADImonitor = Path.parse("./root/CADImonitor");

	// Table type path
	public final class _Root_CADImonitor_CADIImportProcess {
		private static final Path _Root_CADImonitor_CADIImportProcess = Path.parse("./root/CADImonitor/CADIImportProcess");
		public static Path getPathInSchema()
		{
			return _Root_CADImonitor_CADIImportProcess;
		}
	public static final Path _CadiProcessId = Path.parse("./cadiProcessId");
	public static final Path _Cadiid = Path.parse("./cadiid");
	public static final Path _CadiProcessState = Path.parse("./cadiProcessState");
	public static final Path _CadiName = Path.parse("./cadiName");
	public static final Path _CadiTargetDataspace = Path.parse("./cadiTargetDataspace");
	public static final Path _CadiTargetDataset = Path.parse("./cadiTargetDataset");
	public static final Path _ExecutionGroup = Path.parse("./executionGroup");
	public static final Path _ExecutionGroup_CadiLastModified = Path.parse("./executionGroup/cadiLastModified");
	public static final Path _ExecutionGroup_CadiLastUsed = Path.parse("./executionGroup/cadiLastUsed");
	public static final Path _ExecutionGroup_CadiLastSuccessful = Path.parse("./executionGroup/cadiLastSuccessful");
	public static final Path _ExecutionGroup_CadiLastFailed = Path.parse("./executionGroup/cadiLastFailed");
	public static final Path _ExecutionGroup_CadiErrors = Path.parse("./executionGroup/cadiErrors");
	public static final Path _TablesGroup = Path.parse("./tablesGroup");
	public static final Path _TablesGroup_CadiTables = Path.parse("./tablesGroup/cadiTables");
	public static final Path _WorkflowGroup = Path.parse("./workflowGroup");
	public static final Path _WorkflowGroup_CadiLaunchWorkflow = Path.parse("./workflowGroup/cadiLaunchWorkflow");
	public static final Path _WorkflowGroup_CadiWorkflowName = Path.parse("./workflowGroup/cadiWorkflowName");
	public static final Path _WorkflowGroup_WorkflowParametersGroup = Path.parse("./workflowGroup/workflowParametersGroup");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiParamDataspace = Path.parse("./workflowGroup/workflowParametersGroup/cadiParamDataspace");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiParamDataset = Path.parse("./workflowGroup/workflowParametersGroup/cadiParamDataset");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam = Path.parse("./workflowGroup/workflowParametersGroup/cadiWorkflowParam");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam_CadiDataValue = Path.parse("./workflowGroup/workflowParametersGroup/cadiWorkflowParam/cadiDataValue");
	public static final Path _WorkflowGroup_WorkflowParametersGroup_CadiWorkflowParam_CadiDatacontextName = Path.parse("./workflowGroup/workflowParametersGroup/cadiWorkflowParam/cadiDatacontextName");
	} 

	// Table type path
	public final class _Root_CADImonitor_CADIImportTables {
		private static final Path _Root_CADImonitor_CADIImportTables = Path.parse("./root/CADImonitor/CADIImportTables");
		public static Path getPathInSchema()
		{
			return _Root_CADImonitor_CADIImportTables;
		}
	public static final Path _CadiDefinition = Path.parse("./cadiDefinition");
	public static final Path _CadiTableId = Path.parse("./cadiTableId");
	public static final Path _CadiProcessState = Path.parse("./cadiProcessState");
	public static final Path _CadiTableSequence = Path.parse("./cadiTableSequence");
	public static final Path _CadiTargetDataspace = Path.parse("./cadiTargetDataspace");
	public static final Path _CadiTargetDataset = Path.parse("./cadiTargetDataset");
	public static final Path _CadiTargetTable = Path.parse("./cadiTargetTable");
	public static final Path _CadiTableOptional = Path.parse("./cadiTableOptional");
	public static final Path _CadiBatchId = Path.parse("./cadiBatchId");
	public static final Path _CadiImportedFiles = Path.parse("./cadiImportedFiles");
	public static final Path _CadiErrors = Path.parse("./cadiErrors");
	} 

	// Table type path
	public final class _Root_CADImonitor_CADIImportedFiles {
		private static final Path _Root_CADImonitor_CADIImportedFiles = Path.parse("./root/CADImonitor/CADIImportedFiles");
		public static Path getPathInSchema()
		{
			return _Root_CADImonitor_CADIImportedFiles;
		}
	public static final Path _CadiTatgetTable = Path.parse("./cadiTatgetTable");
	public static final Path _CadiImportFileId = Path.parse("./cadiImportFileId");
	public static final Path _CadiProcessState = Path.parse("./cadiProcessState");
	public static final Path _CadiFileDescription = Path.parse("./cadiFileDescription");
	public static final Path _CadiSourceFileGroup = Path.parse("./cadiSourceFileGroup");
	public static final Path _CadiSourceFileGroup_CadiFileName = Path.parse("./cadiSourceFileGroup/cadiFileName");
	public static final Path _CadiSourceFileGroup_CadiFileFolder = Path.parse("./cadiSourceFileGroup/cadiFileFolder");
	public static final Path _CadiSourceFileGroup_CadiFileImportMode = Path.parse("./cadiSourceFileGroup/cadiFileImportMode");
	public static final Path _CadiRecordsCreated = Path.parse("./cadiRecordsCreated");
	public static final Path _CadiRecordsUpdated = Path.parse("./cadiRecordsUpdated");
	public static final Path _CadiRecordsDeleted = Path.parse("./cadiRecordsDeleted");
	public static final Path _CadiRecordsUnchanged = Path.parse("./cadiRecordsUnchanged");
	public static final Path _CadiErrors = Path.parse("./cadiErrors");
	} 
	// ===============================================

}
