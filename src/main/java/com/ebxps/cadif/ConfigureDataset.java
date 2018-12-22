package com.ebxps.cadif;

import com.onwbp.adaptation.Adaptation;
import com.orchestranetworks.schema.trigger.AfterCreateInstanceContext;
import com.orchestranetworks.schema.trigger.BeforeCreateInstanceContext;
import com.orchestranetworks.schema.trigger.InstanceTrigger;
import com.orchestranetworks.schema.trigger.TriggerSetupContext;
import com.orchestranetworks.service.OperationException;

/**
 * Only allow one instance of the CRMP dataset.
 * 
 * @author Steve Higgins - December 2017
 *
 */
public class ConfigureDataset extends InstanceTrigger {

	@Override
	public void setup(TriggerSetupContext aContext) {
		// No setup required
	}

	@Override
	public void handleBeforeCreate(BeforeCreateInstanceContext aContext) throws OperationException {

		// Only allow one ACME dataset 
		Adaptation crmpDataset = Tools.findCrmpDataset();
		if (crmpDataset != null) {
			throw OperationException.createError("The CRMP dataset already exists in dataspace " + crmpDataset.getHome().getKey().getName());
		}

	}
	
	@Override
	public void handleAfterCreate(AfterCreateInstanceContext aContext) throws OperationException {

		// Add some default stuff here if required ...
		
		
		
		
		
	}
	
	
	
}
