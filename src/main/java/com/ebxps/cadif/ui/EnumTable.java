package com.ebxps.cadif.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationName;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.instance.ValueContextForValidation;
import com.orchestranetworks.schema.ConstraintContext;
import com.orchestranetworks.schema.ConstraintEnumeration;
import com.orchestranetworks.schema.InvalidSchemaException;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;

/**
 * Programmatic enumeration constraint to display a list of tables in a 
 * given dataset.
 * 
 * @author Craig Cox - Orchestra Networks - 2015
 *
 */
public class EnumTable implements ConstraintEnumeration<String> {

	/** Map of table technical identifiers against their labels. */
	private Map<String,String> tableMap = new TreeMap<String,String>();

	@Override
	public void setup(ConstraintContext aContext) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException {
		
		List<String> list = new ArrayList<String>();
		String dataspaceName = (String)aContext.getValue(Path.parse("../dataspace"));
		AdaptationHome dataspace = Repository.getDefault().lookupHome(HomeKey.parse(dataspaceName));
		if (dataspace==null){return null;}
		String datasetName = (String)aContext.getValue(Path.parse("../dataset"));
		Adaptation dataset = dataspace.findAdaptationOrNull(AdaptationName.forName(datasetName));
		if (dataset==null){return null;}
//		String datasetLabel = dataset.getLabelOrName(Locale.getDefault());
		List<SchemaNode> tables = findTables(dataset);
		for (SchemaNode table : tables) {
			//String tableKey = String.format("%s:%s:%s", dataspaceName, dataset.toPublicReference(), table.getPathInSchema().format());
			String tableKey = String.format("%s", table.getPathInSchema().format());
			//String tableLabel = String.format("%s  [ %s / %s ]", table.getLabel(Locale.getDefault()),dataspaceName ,datasetLabel);
			String tableLabel = String.format("%s [%s]", table.getLabel(Locale.getDefault()),table.getPathInSchema().format());
			tableMap.put(tableKey, tableLabel);
			list.add(tableKey);
		}
		return list;
	}

	/**
	 * Return a list of the valid tables within a dataset.
	 * @param dataset
	 * @return
	 */
	private List<SchemaNode> findTables(Adaptation dataset) {
		List<SchemaNode> nodeList = new ArrayList<SchemaNode>();
		findTables(dataset.getSchemaNode(), nodeList);
		return nodeList;
	}
	/**
	 * Find all tables within a dataset, starting with the root node. Ignore 
	 * tables with category <b>TRANSFORM</b>.
	 * @param schemaNode
	 * @return
	 */
	private void findTables(SchemaNode node, List<SchemaNode> nodeList) {

		if (node.isTableNode()) {
			String category = node.getCategory();
			if (category == null || !category.equalsIgnoreCase("TRANSFORM")) {
				nodeList.add(node);
			}
		} else {
			for (SchemaNode childNode : node.getNodeChildren()) {
				findTables(childNode, nodeList);
			}
		}

	}

	@Override
	public String displayOccurrence(String tableKey, ValueContext aContext, Locale aLocale) throws InvalidSchemaException {
		return tableMap.get((String) tableKey);
	}

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext) throws InvalidSchemaException {
		// TODO Auto-generated method stub
	
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext) throws InvalidSchemaException {
		// TODO Auto-generated method stub
		return null;
	}
}
