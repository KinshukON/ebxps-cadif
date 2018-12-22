package com.ebxps.cadif.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import com.orchestranetworks.schema.info.SchemaFacetTableRef;

/**
 * Programmatic enumeration constraint to display a list of fields
 * in a given table. The <code>pathToTableRecord</code> parameter specifies
 * a set of field paths that lead from the current field (ie, the field
 * that this class is attached to) to a field that contains the name
 * of the table to list.
 * 
 * @author Craig Cox - Orchestra Networks - 2015
 *
 */
public class EnumFields implements ConstraintEnumeration<String> {

	private String pathToTableRecord;
	private List<Path> pathsToTable = new ArrayList<Path>();

	@Override
	public void setup(ConstraintContext ctx) {

		if (pathToTableRecord != null) {
			for (String pathElement : pathToTableRecord.split("[ ,]")) {
				if (pathElement != null && !pathElement.trim().isEmpty()){
					pathsToTable.add(Path.parse(pathElement));
				}			
			}
		}
			
	}

	/**
	 * Return the complete list of field paths for the target table
	 */
	@Override
	public List<String> getValues(ValueContext ctx) throws InvalidSchemaException {

		SchemaNode tableNode = getTableSchemaNode(ctx);
		if (tableNode == null) { 
			return null; 
		}
		
		return getFieldList(tableNode.getNodeChildren());

	}

	/**
	 * Recursively scan the target table and list its fields. 
	 * @param schemaNodes
	 * @return
	 */
	private List<String> getFieldList(SchemaNode[] schemaNodes) {

		List<String> list = new ArrayList<String>();

		for (SchemaNode node : schemaNodes){
			String fieldKey = node.getPathInAdaptation().format();
			list.add(fieldKey);
			SchemaNode[] childNodes = node.getNodeChildren();
			if (childNodes.length > 0) { 
				list.addAll(getFieldList(childNodes));
			}
		}
		
		return list;
		
	}

	/**
	 * Return the label for an individual field path.
	 */
	@Override
	public String displayOccurrence(String value, ValueContext ctx, Locale locale) throws InvalidSchemaException {
		
		String fieldPath = (String) value;
		SchemaNode fieldNode = getFieldNode(ctx, fieldPath);
		if (fieldNode != null) {
			return String.format("%s [%s]", fieldNode.getLabel(locale), fieldPath);
		} else {
			return null;
		}
		
	}
	

	@Override
	public void checkOccurrence(String value, ValueContextForValidation ctx) throws InvalidSchemaException {
		
		SchemaNode fieldNode = getFieldNode(ctx, (String) value);
		if (fieldNode == null) {
			ctx.addError("Invalid field selection");
		}

	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext) throws InvalidSchemaException {
		return null;
	}

	/**
	 * Find a field within the target table.
	 * @param ctx
	 * @param fieldPath
	 * @return
	 */
	private SchemaNode getFieldNode(ValueContext ctx, String fieldPath) {

		SchemaNode tableNode = getTableSchemaNode(ctx);
		if (tableNode == null) { 
			return null; 
		}

		return tableNode.getNode(Path.SELF.add(fieldPath), true, true);

	}

	/**
	 * Find the table that this component should examine.
	 * @param ctx
	 * @return The table root node (so that its fields can be iterated)
	 */
	private SchemaNode getTableSchemaNode(ValueContext ctx) {
	
		// If there was no path the use the current table's root node
		if (pathsToTable.isEmpty()) {
			return getNamedTableRootNode(ctx);
		}
		
		// Follow the foreign keys to the 
		Adaptation record = null;
		for (Path pathStep : this.pathsToTable){
	
			if (record == null) {
				
				String fkValue = (String) ctx.getValue(pathStep);
				if (fkValue == null) { 
					return null; 
				}
				
				SchemaFacetTableRef fkSpec = ctx.getNode(pathStep).getFacetOnTableReference();
				record = fkSpec.getLinkedRecord(ctx);
				
			} else {
				
				String fkValue = record.getString(pathStep);
				if (fkValue == null) { 
					return null; 
				}
				
				SchemaNode fkField = record.getSchemaNode().getNode(pathStep);
				SchemaFacetTableRef fkSpec = fkField.getFacetOnTableReference();
				record = fkSpec.getLinkedRecord(record);
				
			}
			
		}
		
		return getNamedTableRootNode(record);
		
	}

	private SchemaNode getNamedTableRootNode(Adaptation record) {
		
		String tableDataspaceName = record.getString(Path.parse("./dataspace"));
		AdaptationHome tableDataspace = Repository.getDefault().lookupHome(HomeKey.forBranchName(tableDataspaceName));
		if (tableDataspace == null) { 
			return null; 
		}
	
		String tableDatasetName = record.getString(Path.parse("./dataset"));
		Adaptation tableDataset = tableDataspace.findAdaptationOrNull(AdaptationName.forName(tableDatasetName));
		if (tableDataset == null) { 
			return null; 
		}
	
		String tablePath = record.getString(Path.parse("./tableName"));
		return tableDataset.getTable(Path.parse(tablePath)).getTableOccurrenceRootNode();
		
	}
	
	private SchemaNode getNamedTableRootNode(ValueContext record) {
		String tableDataspaceName = (String) record.getValue(Path.parse("../dataspace"));
		AdaptationHome tableDataspace = Repository.getDefault().lookupHome(HomeKey.parse(tableDataspaceName));
		if (tableDataspace == null) { 
			return null; 
		}
	
		String tableDatasetName = (String) record.getValue(Path.parse("../dataset"));
		Adaptation tableDataset = tableDataspace.findAdaptationOrNull(AdaptationName.forName(tableDatasetName));
		if (tableDataset == null) { 
			return null; 
		}
	
		String tablePath = (String) record.getValue(Path.parse("../tableName"));
		return tableDataset.getTable(Path.parse(tablePath)).getTableOccurrenceRootNode();

	}
	
	public String getPathToTableRecord() {
		return pathToTableRecord;
	}

	public void setPathToTableRecord(String pathToTableRecord) {
		this.pathToTableRecord = pathToTableRecord;
	}
}
