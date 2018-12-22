package com.ebxps.cadif.ui;

import java.util.Locale;

import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.schema.InvalidSchemaException;
import com.orchestranetworks.schema.types.dataspace.DataspaceSetFilter;
import com.orchestranetworks.schema.types.dataspace.DataspaceSetFilterContext;
import com.orchestranetworks.schema.types.dataspace.DataspaceSetFilterSetupContext;

public class DataspaceFilter implements DataspaceSetFilter {

	@Override
	public boolean accept(HomeKey homeKey, DataspaceSetFilterContext ctx) {

		return false;
	}

	@Override
	public void setup(DataspaceSetFilterSetupContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext ctx) throws InvalidSchemaException {
		return null;
	}

}
