package com.ebxps.cadif;

import java.util.*;

import com.ebxps.cadif.adpatation.*;
import com.ebxps.cadif.process.*;
import com.onwbp.adaptation.*;
import com.onwbp.org.apache.log4j.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.service.*;

/**
 * This class is used in a script task to  monitor for files and launch an import process.
 * 
 * @author Craig Cox - Orchestra Networks February 2017
 *
 */


public class CadiMonitorScheduledTask extends ScheduledTask {


	Category log = CadiRepository.getCategory();


	@Override
	public void execute(ScheduledExecutionContext aContext) throws OperationException, ScheduledTaskInterruption {

		ImportDefinitionManager idm = new ImportDefinitionManager(aContext.getSession());
		SourceFiles srcFiles = new SourceFiles();

		log.debug("CADI MONITOR >>> START");

		/* Get all definitions that the current user has permission to execute
		 * in processing order and id sequence, lowest values first.  
		 * returns the number of definitions to be processed. */
		List <Adaptation> idmc = idm.getDefinitions();
		log.debug(String.format("Accessing Import Definition Manager - processing [%d] import definitions", idmc.size()));
		for (Adaptation idef : idmc){
			log.debug(String.format("Processing record [%d] %s", idef.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId),
					idef.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName)));
			//For each definition found, get the import definition.
			//Check to see if the mandatory import files exist
			if (idef == null){ break; }
			String batchIdPolicy = idef.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiBatchIdPolicy);
			log.debug(String.format("Batch policy [%s] for [%d] %s", batchIdPolicy, 
					idef.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId),
					idef.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName)));
			//2.0.16 override clear of stored batch id's
			srcFiles.clear();
			List<String> batchIds = srcFiles.filesExist(idef, batchIdPolicy, null);
			log.debug(String.format("Batch id's found for import definition - %s", (batchIds == null)? "": batchIds.toString()));
			if (batchIds != null && !batchIds.isEmpty()){
				for (String batchId : batchIds){
					//If all mandatory files exist, launch the import process.
					log.info(String.format("Requestimg import for [%d] %s for batch [%s]",idef.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId),
							idef.getString(Paths._Root_CADIdefinitions_CADIdefinition._CadiName),
							batchId));
					idm.RequestImport(idef,batchId);
				}
			}
		}
		log.debug("CADI MONITOR >>> END");
	}
}
