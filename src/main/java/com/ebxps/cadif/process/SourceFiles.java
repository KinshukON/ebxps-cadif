package com.ebxps.cadif.process;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.regex.*;

import org.apache.commons.io.filefilter.*;

import com.ebxps.cadif.Paths;
import com.ebxps.cadif.adpatation.*;
import com.onwbp.adaptation.*;
import com.onwbp.org.apache.log4j.*;

/**
 * <b>SourceFiles</b> using a passed import definition adaptation will scan for the necessary files, making sure they exist 
 * @author Craig Cox  Orchestra Networks	February 2017
 *
 */


public class SourceFiles {

	private Category log = CadiRepository.getCategory();

	private List<String>errors = new ArrayList<String>();

	/** String contains batch id, Integer contains instance count*/
	private ConcurrentHashMap<String,Integer> batchIdList = new ConcurrentHashMap<String, Integer>();

	/** String contains batch id, Integer contains instance count*/
	private List<String> filesBatchIdList = new ArrayList<String>();

	private Integer tableCounter;

	/**
	 * Scan for the files required to launch/start the import for the passed definition 
	 * @param definitionRecord either an import definition record.
	 * @return batch id's: Returns a list of batch Id's for which all of the necessary files exist.
	 */


	public List<String> filesExist(Adaptation definitionRecord, String batchIdPolicy, String processBatchId){

		//definition record, policy = F or Z no processID
		//definition record, policy = F or Z with processID
		//definition record, policy = C or N ignore processID

		// If there is a processBatchId look specifically for the files that match
		// If the batchIDPolicy is Z or F use batch number else just files that exist.
		log.debug(String.format("Searching for files using Defiition [%s], Batch Id Policy [%s] and Process Batch Id [%s]", 
				definitionRecord.toString(), batchIdPolicy, processBatchId));

		
		boolean  useFileBatchId = false;
		if (batchIdPolicy.equals("F") || batchIdPolicy.equals("Z")){
			useFileBatchId = true;
		}

		boolean executeByBatch = definitionRecord.get_boolean(Paths._Root_CADIdefinitions_CADIdefinition._CadiExecByBatch);
		String executeByZip = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiExecByZip);
		List<String> batchIds = new ArrayList<String>();
		log.debug(String.format("Process is using Execute By Batch [%s], Execute By Zip [%s]",(executeByBatch)?"Yes":"No",  executeByZip));
		filesBatchIdList.clear();
		// Is this import definition to use a ZIP file as the primary and only file source? 
		if ("Y".equals(executeByZip)){
			log.debug("Processing ZIP files >> Start");
			//filesBatchIdList.clear();
			boolean fileFound = false;

			List<File> fileList = findZipFiles(definitionRecord);

			if (fileList.isEmpty()) { return null; }

			if (useFileBatchId){
				// If execute by batch and batch id policy is to use zip batch id
				if (executeByBatch && batchIdPolicy.equals("Z")){ 
					updateFilesBatchId(fileList, 
							definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipBatchIdRegex),
							batchIdPolicy);
				}else{
					if(!fileList.isEmpty()){
						updateFilesBatchId(fileList);
					}
				}
			}else{
				//Batch ids are not in use and there is a matching Zip file
				log.debug("Not using batch Id's use value [cadi]");
				batchIds.add("cadi");
				return batchIds;
			}

			updateBatchList(true);
			if(!fileFound && batchIdList.isEmpty()) { return batchIds; }

			log.debug("Processing ZIP files >> Complete");

		}else{
			// Import uses loose files not a zip file
			//for each table that is mandatory 
			log.debug("Processing loose files >> Start");

			List<Adaptation> tableRecords = AdaptationUtil.getLinkedRecordList(definitionRecord, 
					Paths._Root_CADIdefinitions_CADIdefinition._TablesGroup_CadiTables);
			tableCounter = 0;

			for (Adaptation tableRecord : tableRecords){
				if(!tableRecord.get_boolean(Paths._Root_CADIdefinitions_CADItargetTables._CadiTableOptional)){
					//Import to this table is mandatory
					tableCounter ++;
					List<Adaptation> fileRecords = AdaptationUtil.getLinkedRecordList(tableRecord,
							Paths._Root_CADIdefinitions_CADItargetTables._CadiSourceFiles);
					boolean fileFound = false;
					filesBatchIdList.clear();
					for (Adaptation fileRecord : fileRecords){
						List<File> fileList = findFileList(fileRecord, null);
						if (useFileBatchId){
							if (executeByBatch){ 
								updateFilesBatchId(fileList, 
										fileRecord.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiBatchIdRegex),
										batchIdPolicy);
							}else{
								if(!findFileList(fileRecord, null).isEmpty()){
									updateFilesBatchId(fileList);
								}
							}
						}else{
							//Batch ids are not in use and there is a matching Zip file
							log.debug("Not using batch Id's use value [cadi]");
							batchIds.add("cadi");
							return batchIds;
						}
					}
					updateBatchList(false);
					if(!fileFound && batchIdList.isEmpty()) { return batchIds; }
				}
			}
			log.debug("Processing loose files >> Complete");
		}

		// Process the results and return the batch id's

		for (Entry<String,Integer> batchListId : batchIdList.entrySet()){
			batchIds.add(batchListId.getKey());
		}

		return batchIds;

	}




	/**
	 * Get a list of batch id's for which there is a file for all mandatory tables in the import definition.
	 * @return list of batch id's
	 */
	public List<String> getBatchIds(){

		List<String> batchList = new ArrayList<String>();
		for (Entry<String,Integer> batch : batchIdList.entrySet()){
			batchList.add((String) batch.getKey());
		}
		return batchList;

	}



	public List<File> findFiles(Adaptation fileDefinitionRecord, String batchId, String workingfolder, String batchIdPolicy){


		//If batch id is null, then get any files that match the file pattern.

		List<File> selectedFileList = new ArrayList<File>();

		List<File> fileList = findFileList(fileDefinitionRecord, workingfolder);
		log.debug(String.format("Found [%d] files", fileList.size()));
		for (File file : fileList){
			String fileBatchId="";
			if (batchId != null && !batchId.startsWith("cadi")){
				String tableName = fileDefinitionRecord.getContainerTable().getTablePath().getLastStep().format();
				if (tableName.equals("CADIimportFiles")){
					if (batchIdPolicy.equals("F")){
						fileBatchId = getFileBatch(file, fileDefinitionRecord.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiBatchIdRegex));
					}else{
						fileBatchId = batchId;
					}
				}else if (tableName.equals("CADIdefinition")){
					if (batchIdPolicy.equals("Z")){					
						fileBatchId = getFileBatch(file, fileDefinitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipBatchIdRegex));
					}else{
						fileBatchId = batchId;
					}
				}
				if (fileBatchId.equals(batchId)){ 
					selectedFileList.add(file);
				}
			}else{
				selectedFileList.add(file);
			}
		}

		return selectedFileList;
	}

	/**
	 * Return a list of files to import for a fileDefinition record where the files meet the 
	 * batch id passed.
	 * @param fileDefinitionRecord - the file search arguments
	 * @param batchId - a batch id or null.
	 * @return list of File objects identifying the files to import.
	 */

	public List<File> findFiles(Adaptation fileDefinitionRecord,  String batchId, String batchIdPolicy){

		return findFiles(fileDefinitionRecord, batchId, null, batchIdPolicy);

	}



	private List<File> findFileList(Adaptation fileRecord, String workingFolder) {

		// Working folder is set when the files have been extracted from a zip file.
		if (workingFolder == null){
			workingFolder = fileRecord.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiFileFolder);
		}

		// Using the file pattern get the files that match. 
		List<File> fileList = new ArrayList<File>();
		FileFilter fileFilter = new WildcardFileFilter(
				fileRecord.getString(Paths._Root_CADIdefinitions_CADIimportFiles._CadiSourceFileGroup_CadiFileName));
		log.debug(String.format("Looking for files [%s] in folder [%s]", fileFilter.toString() ,workingFolder));
		File folder = new File(workingFolder);
		File[] files = folder.listFiles(fileFilter);
		if (files == null) {
			log.warn(String.format("Cannot find file system object %s. [%s]",fileFilter,fileRecord.toString()));
			errors.add(String.format("Cannot find file system object %s. [%s]",fileFilter,fileRecord.toString()));
		} else {
			for (File file : files) {
				if (file.isFile() && file.canRead() && !file.isHidden()) {
					fileList.add(file);
				} else {
					log.warn(String.format("Skipped unreadable file %s. [%s]",file, fileRecord.toString()));
				}
			}
		}
		return fileList;
	}

	/**
	 * Used to find any zip files.
	 * 
	 */

	public List<File> findZipFiles(Adaptation definitionRecord) {

		String batchIdPolicy = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiBatchIdPolicy);
		
		return findZipFiles(definitionRecord, null, batchIdPolicy);

	}

	/**
	 * Find the zip files to process
	 * @param definitionRecord - import definition
	 * @param batchId - Batch id for a specific zip file.
	 * @param useFileBatchId - true, only include files with matching batch id.  false, include any matching zip file
	 * @return List of <code>File</code> objects 1 per ZIP file.
	 */

	public List<File> findZipFiles(Adaptation definitionRecord, String batchId, String batchIdPolicy) {

		// definition record, null, true = list any zip files that match the file pattern, (batch ids?)
		// definition record, #id, true = find specific zip file
		// definition record, *, false = find any zip files that match the file pattern.

		// Using the file pattern get the files that match. 
		List<File> fileList = new ArrayList<File>();
		FileFilter fileFilter = new WildcardFileFilter(
				definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipFileName));
		File folder = new File(
				definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipFolderPath));
		File[] files = folder.listFiles(fileFilter);

		if (files == null) {
			log.warn(String.format("Cannot find file system object %s. [%s]",fileFilter,definitionRecord.toString()));
			errors.add(String.format("Cannot find file system object %s. [%s]",fileFilter,definitionRecord.toString()));
		} else {
			// Have a list of files to process.
			for (File file : files) {
				if (file.isFile() && file.canRead() && !file.isHidden()) {
					//file can be processed
					if (batchIdPolicy.equals("Z")){
						// No batch id was passed or batch id's are not in use
						String batchRegex = definitionRecord.getString(Paths._Root_CADIdefinitions_CADIdefinition._ZipFileGroup_CadiZipBatchIdRegex);
						String fileBatchId = getFileBatch(file, batchRegex);
						if (batchId != null){
							if (batchId.equals(fileBatchId)){
								// Batch Id provided and file matches id so keep
								filesBatchIdList.add(fileBatchId);
								fileList.add(file);
							}						
						}else{
							if (fileBatchId !=null && !fileBatchId.isEmpty()){
								// No batch Id provided store batch id and file.
								filesBatchIdList.add(fileBatchId);
								fileList.add(file);
							}
						}
					}else{
						//Not using file batch id keep the file.
						fileList.add(file);
					}
				} else {
					log.warn(String.format("Skipped unreadable file %s. [%s]",file, definitionRecord.toString()));
				}
			}
		}
		log.debug(String.format("Found Zip files: %s", fileList.toString()));
		return fileList;
	}



	private void updateFilesBatchId(List<File> fileList){

		for (File file : fileList){
			//Get file name
			String fileName = file.getName();
			//String fileBatchId = ImportDefinitionManager.FILE_ID+fileName;  //TODO how to deal with files not in batch?
			String fileBatchId = "";
			// Record distinct list of batch Ids
			if (!filesBatchIdList.contains(fileBatchId)){
				log.debug(String.format("Adding file batch id [%s] to filesBatchList", fileBatchId));
				filesBatchIdList.add(fileBatchId);
			}
		}

	}

	/**
	 * Update the collection of batch Id's from files list
	 * @param fileList
	 * @param batchIdRegex
	 */
	private void updateFilesBatchId(List<File> fileList, String batchIdRegex, String batchIdPolicy){

		log.debug(String.format("Checking files batch id using pattern [%s] and policy [%s]", batchIdRegex, batchIdPolicy));
		for (File file : fileList){
			//Get file name
			String fileName = file.getName();

			// Extract batch Id
			String fileBatchId = null;
			if (batchIdPolicy.equals("F") || batchIdPolicy.equals("Z")){
				Pattern regexPattern = Pattern.compile(batchIdRegex);
				Matcher regexMatcher = regexPattern.matcher(fileName);
				regexMatcher.find();
				fileBatchId = regexMatcher.group();
				log.debug(String.format("Checking file [%s] found batch id [%s]", fileName, fileBatchId));
			}
			if (batchIdPolicy.equals("C")){
				fileBatchId = "cadi";
				log.debug(String.format("Checking file [%s] found batch id [%s]", fileName, fileBatchId));
			}
			// Record distinct list of batch Ids
			if (!fileBatchId.isEmpty() && !filesBatchIdList.contains(fileBatchId)){
				filesBatchIdList.add(fileBatchId);
			}
		}

	}

	/**
	 * <b>updateBatchList</b> Update/cleanse the batch list 
	 * @param zip - files in batch list are zip files 
	 */
	private void updateBatchList(boolean zip) {

		log.debug(String.format("Cleansing batch is list %s", (zip)?"for ZIP files":"for loose files"));
		// update the batchIdList for every batch id found using all file search descriptors
		for (int i=0; i < filesBatchIdList.size(); i++ ){
			String batchId = filesBatchIdList.get(i);
			if(!batchIdList.containsKey(batchId)){
				batchIdList.put(batchId, 1);
			}else{
				batchIdList.replace(batchId, batchIdList.get(batchId)+1);
			}
		}

		if (!zip){
			// only keep batch id's in the list where there is an 
			// entry for each table processed so far.
			for (Entry<String,Integer> batch : batchIdList.entrySet()){
				Integer batchCount = (int) batch.getValue();
				if (batchCount != tableCounter){
					batchIdList.remove(batch.getKey());
				}			
			}
		}
	}

	private String getFileBatch(File file, String batchIdRegex){
		if (batchIdRegex == null || batchIdRegex.isEmpty()) { return ""; }
		//Get file name
		String fileName = file.getName();
		// Extract batch Id
		Pattern regexPattern = Pattern.compile(batchIdRegex);
		Matcher regexMatcher = regexPattern.matcher(fileName);
		regexMatcher.find();
		String fileBatchId = regexMatcher.group();

		return fileBatchId;

	}

	public void clear(){
		batchIdList.clear();
		filesBatchIdList.clear();
	}
}
