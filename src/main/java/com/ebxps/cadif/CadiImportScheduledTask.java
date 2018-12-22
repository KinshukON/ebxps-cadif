package com.ebxps.cadif;

import com.ebxps.cadif.adpatation.*;
import com.ebxps.cadif.process.*;
import com.onwbp.adaptation.*;
import com.onwbp.org.apache.log4j.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.service.*;


/**
 * This class is used in a script task to execute queued import processes.
 * 
 * @author Craig Cox - Orchestra Networks February 2017
 *
 */
public class CadiImportScheduledTask extends ScheduledTask {

	private Category log = CadiRepository.getCategory();

	@Override
	public void execute(ScheduledExecutionContext aContext) throws OperationException, ScheduledTaskInterruption {

		log.debug("CADI IMPORT >>> START");
		// Find all outstanding unprocessed CADI process records and execute the import
		ImportDefinitionManager idm = new ImportDefinitionManager(aContext.getSession());
		RequestResult cadiProcesses = idm.getCadiProcesses(aContext);
		log.debug(String.format("Processing %d requests", cadiProcesses.getSize()));
		Adaptation processRecord = cadiProcesses.nextAdaptation();
		while (processRecord != null){
			log.info(String.format("Starting process [%d] for batch %s", processRecord.get_int(Paths._Root_CADIprocesses._CadiProcessId), 
																		processRecord.getString(Paths._Root_CADIprocesses._CadiBatchId)));
			idm.executeImport(processRecord);
			log.debug(String.format("Completed process [%d] for batch %s", processRecord.get_int(Paths._Root_CADIprocesses._CadiProcessId), 
					processRecord.getString(Paths._Root_CADIprocesses._CadiBatchId)));
			processRecord = cadiProcesses.nextAdaptation();
		}
		idm.closeCadiProcesses();
		log.debug("CADI IMPORT >>> END");
	}

}


