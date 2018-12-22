package com.ebxps.cadif.adpatation;

import com.ebxps.cadif.*;
import com.onwbp.adaptation.*;

@Deprecated
public class ImportDefinition {

	
	private Integer cadiId;

	private Adaptation cadiDefinitionRecord;
	
	public ImportDefinition(Adaptation cadiDefinitionRecord) {
		
		this.cadiId = cadiDefinitionRecord.get_int(Paths._Root_CADIdefinitions_CADIdefinition._CadiId);
		this.cadiDefinitionRecord = cadiDefinitionRecord;
		
	}
	
	
	
	public Adaptation getAdaptation(){
	
		return cadiDefinitionRecord;
	}
	
	
	public Integer getCadiId(){
	
		return cadiId;
	}

	
	
}
