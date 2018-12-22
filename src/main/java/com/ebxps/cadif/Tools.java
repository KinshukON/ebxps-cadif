package com.ebxps.cadif;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.RequestResult;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.info.SchemaNodeInformation;

/**
 * Various static utility methods.
 * 
 * @author Steve Higgins/Craig Cox - September 2016/October 2017
 *
 */
public class Tools {

	/**
	 * Logger.
	 */
	private static Category LOG = getCategory();

	/**
	 * Name of the CRMP module
	 */
	public static final String CRMP_MODULE_NAME = "ebxps-crmp";
	
    /** Starting delimiter for a field substitution. */
    private static final String START_DELIMITER = "${";
    
    /** Ending delimiter for a field substitution. */
    private static final String END_DELIMITER = "}";
    

	/**
	 * Gets a log4j Category for the ACME module.
	 * @return a log4j Category for the calling object
	 */
//	public static Category getCategory() {
//		return Category.getInstance(CRMP_MODULE_NAME);
//	}
	
	/**
	 * Gets a log4j Category for the calling object.
	 * @return a log4j Category for the calling object
	 */
	public static Category getCategory() {
		final StackTraceElement element = new Throwable().fillInStackTrace().getStackTrace()[1];
		return Category.getInstance(element.getClassName());
	}


	/**
	 * Dynamically load a named Java class, and check that it's of a particular type.
	 * <p> Typical invocation will be of this form: 
	 * <code>SuperClass p = TransformTools.getInstance("com.mycompany.SubClass", SuperClass.class)</code></p>
	 * <p>That will attempt to load <b>com.mycompany.SubClass</b>, check that it's castable to 
	 * <b>SuperClass</b>, and return a instance of the class, cast to <b>SuperClass</b>.</p>
	 * 
	 * @param <T> The class type of loaded class or one of its super types
	 * @param loader The classloader to use
	 * @param className The name of the class
	 * @param superClass The class type to check.
	 * @return An instance of the desired class, or null if it couldn't be loaded
	 */
	protected static <T> T getInstance(ClassLoader loader, String className, Class<T> superClass) {

		try {
			Class<?> loadedClass = loader.loadClass(className);
			if (superClass.isAssignableFrom(loadedClass)) {
				Object classInstance = loadedClass.newInstance();
				return superClass.cast(classInstance);
			} else {
				LOG.error(String.format("Class [%s] isn't of type [%s]", loadedClass.getCanonicalName(), superClass.getCanonicalName()));
			}
		} catch (final ClassNotFoundException e) {
			LOG.error(String.format("Couldn't find class [%s]", className));
		} catch (final Exception e) {
			LOG.error(String.format("Couldn't instantiate class [%s] - %s", className, e.getMessage()));
		}

		return null;
	}

	/**
	 * Search for the CRMP configuration dataset.
	 * @return The CRMP configuration dataset, or null if it couldn't be found
	 */
	public static Adaptation findCrmpDataset() {
		return searchDataspace(Repository.getDefault().getReferenceBranch(), CRMP_MODULE_NAME);
	}
		
	/**
	 * Search a given dataspace and it's descendants for a dataset that
	 * implements the named module's model.
	 * @param dataspace The starting dataspace
	 * @param moduleName The name of the module
	 * @return The dataset the implements the module's data model, or null if it
	 *         couldn't be found
	 */
	private static Adaptation searchDataspace(AdaptationHome dataspace, String moduleName) {

		if (dataspace != null && dataspace.isOpenBranch() && !dataspace.isTechnicalBranch()) {

			// Scan the dataset module names
			List<Adaptation> datasets = dataspace.findAllRoots();
			for (Adaptation dataset : datasets) {
				String tmpModuleName = dataset.getSchemaLocation().getModuleName();
				if (dataset.isActivated() && !dataset.isDeleted() && tmpModuleName != null
						&& tmpModuleName.equals(moduleName)) {
					return dataset;
				}
			}

		}

		List<AdaptationHome> children = dataspace.isBranch() ? dataspace.getVersionChildren() : dataspace.getBranchChildren();
		for (AdaptationHome child : children) {
			Adaptation dataset = searchDataspace(child, moduleName);
			if (dataset != null) {
				return dataset;
			}
		}

		// Not found in this dataspace or descendants
		return null;
	}

	/**
	 * Retrieve the parent of a given record by following a given foreign key field.
	 * @param record The child record
	 * @param fkPath Path to a field in the child record that's a foreign key
	 * @return The parent record, according to the foreign key
	 */
	public static Adaptation getParentViaForeignKey(Adaptation record, Path fkPath) {
		SchemaNode fkNode = record.getSchemaNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecord(record);
	}

	
	/** 
	 * Extract the contents of the "information" attribute of a given node.  
	 * @param node the node to examine
	 * @return The contents of the information attribute. This may be null.  
	 */
	public static String getNodeInfo(SchemaNode node) {

		if (node == null) {
			return null;
		}

		SchemaNodeInformation nodeInfo = node.getInformation();
		if (nodeInfo == null) {
			return null;
		}

		String spec = nodeInfo.getInformation();
		if (spec == null || spec.trim().isEmpty()) {
			return null;
		}

		return spec;
	}

	/**
	 * Return a label for a trigger record based on the format defined in the match definition.
	 * Values from the trigger record are substituted wherever the ${fieldName} notation is found. 
	 * Named fields that aren't in the record will not be substituted. If the format string is
	 * null or blank then the record's default label will be returned.
	 * @param triggerRecord The trigger record
	 * @param format The label format string from the match definition
	 * @return A label for the record
	 */
	public static String getRecordLabel(final Adaptation triggerRecord, final String format) {

		// If the format string is empty then return the default label 
		if (format == null || format.trim().isEmpty()) {
			return triggerRecord.getLabel(Locale.getDefault());
		}
		
		// Create a record label from the given format, substituting values as required
		String label = format;
		int startIndex = 0;
		int endIndex = 0;
		while ((startIndex = label.indexOf(START_DELIMITER, endIndex)) >= 0
				&& (endIndex = label.indexOf(END_DELIMITER, startIndex)) >= 0) {

			// Extract the name of the field and try to find it in the record.
			// If the raw field name lookup fails try prefixing "./".
			String fieldName = label.substring(startIndex + START_DELIMITER.length(), endIndex);
			SchemaNode fieldNode = triggerRecord.getSchemaNode().getNode(Path.parse(fieldName));
			if (fieldNode == null) {
				fieldNode = triggerRecord.getSchemaNode().getNode(Path.SELF.add(fieldName));
			}

			// If we found the node then extract the value and insert it into the label
			if (fieldNode != null) {
				String fieldValue = triggerRecord.get(fieldNode).toString();		// TODO: Check this is OK with INT & STRING	
				if (fieldValue == null) { 
					fieldValue = "' '"; 
				}
				label = label.replace(START_DELIMITER + fieldName + END_DELIMITER, fieldValue);
			}

		}

		return label;

	}

	/**
	 * Get a list of values from a selection node or association by following a path within that table,
	 * or a list of records if no attribute path is specified
	 *
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @param attributePath the path of the attribute on the selection node or association table
	 * @return the list of values or records, or an empty list
	 */
	public static List<Adaptation> getLinkedRecordList(Adaptation record, Path path)
	{
		List<Adaptation> list = new ArrayList<Adaptation>();
		RequestResult reqRes = linkedRecordLookup(record, path);
		if (reqRes == null || reqRes.isEmpty()) {
			return list;
		}

		try {
			for (Adaptation next; (next = reqRes.nextAdaptation()) != null;)  {
				list.add(next);
			}
		} finally {
			reqRes.close();
		}

		return list;
	}
	/**
	 * Given a record <code>Adaptation</code> and a path representing an association/select node field
	 * of that record, return the <code>RequestResult</code> that can be used to iterate over the
	 * related records.
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @return RequestResult iterator for the related records
	 */
	public static RequestResult linkedRecordLookup(final Adaptation record, final Path path)
	{
		if (record == null || path == null) {
			return null;
		}
		
		final SchemaNode node = record.getSchemaNode().getNode(path);
		if (node == null) {
			return null;
		}
		
		if (node.isAssociationNode()) {
			return node.getAssociationLink().getAssociationResult(record);
		}
		
		if (node.isSelectNode()) {
			return node.getSelectionLink().getSelectionResult(record);
		}
		
		return null;
	}


}
