package com.ebxps.cadif.adpatation;

import java.util.*;
import java.util.regex.*;



import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.onwbp.boot.*;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;

/**
 * A class comprised of static utility methods for dealing with Adaptations.  Most of the methods
 * act on a record <code>Adaptation</code> or a record <code>ValueContext</code>. 
 */
public class AdaptationUtil
{

	
	/**
	 * Function to replace ${path} occurrences in an xpath predicate for a particular instance. If the
	 * path points to an instance the formatted primary key is used.
	 * TODO: compare with {@link #computeValuedPredicate(String, ValueContextForValidation)}
	 * for overlapping functionality.
	 */
	public static String calculateLocalValues(final ValueContext vc, final String predicate)
	{
		String parsed = "";
		int lastCopied = 0;
		final int len = predicate.length();
		for (int i = 0; i < len; i++)
		{
			// Look for start of substitution
			if (predicate.charAt(i) != '$')
			{
				continue;
			}
			if (i == len || predicate.charAt(i + 1) != '{')
			{
				continue;
			}
			parsed = parsed.concat(predicate.substring(lastCopied, i));
			i = i + 2;
			final int k = i;
			for (; i < len && predicate.charAt(i) != '}';)
			{
				i++;
			}
			lastCopied = i;

			// Determine substitute
			try
			{
				final String subst = predicate.substring(k, i);
				final Path p = Path.parse(subst);
				final Object o = vc.getValue(p);
				if (o == null)
				{
					return null;
				}
				if (o instanceof Adaptation)
				{
					parsed = parsed + "'" + ((Adaptation) o).getOccurrencePrimaryKey().format()
						+ "'";
				}
				else
				{
					parsed = parsed + "'" + o.toString() + "'";
				}
			}
			catch (final Exception e)
			{
				// log error
				VM.log.kernelWarn("Exception determining contextual xPath predicate: "
					+ e.getLocalizedMessage());
				return null;
			}
		}
		return parsed;
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
		if (record == null || path == null)
		{
			return null;
		}
		final SchemaNode node = record.getSchemaNode().getNode(path);
		if (node == null)
			return null;
		if (node.isAssociationNode())
		{
			return node.getAssociationLink().getAssociationResult(record);
		}
		if (node.isSelectNode())
		{
			return node.getSelectionLink().getSelectionResult(record);
		}
		return null;
	}

	/**
	 * Deletes a list of records from a selection node or association
	 *
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @throws OperationException
	 */
	@SuppressWarnings("unchecked")
	public static void deleteLinkedRecordList(
		Adaptation record,
		Path path,
		ProcedureContext pContext) throws OperationException
	{
		List<Adaptation> records = (List<Adaptation>) getLinkedRecordList(record, path, null);
		for (Adaptation childRecord : records)
		{
			//DeleteRecordProcedure.execute(pContext, childRecord);
		}
	}

	/**
	 * Get a list of records from a selection node or association
	 *
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @return the list of records, or an empty list
	 */
	@SuppressWarnings("unchecked")
	public static List<Adaptation> getLinkedRecordList(Adaptation record, Path path)
	{
		return (List<Adaptation>) getLinkedRecordList(record, path, null);
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
	public static List<?> getLinkedRecordList(Adaptation record, Path path, Path attributePath)
	{
		List<Object> list = new ArrayList<Object>();
		RequestResult reqRes = linkedRecordLookup(record, path);
		if (reqRes == null || reqRes.isEmpty())
		{
			return list;
		}

		try
		{
			for (Adaptation next; (next = reqRes.nextAdaptation()) != null;)
			{
				if (attributePath == null)
				{
					list.add(next);
				}
				else
				{
					list.add(next.get(attributePath));
				}
			}
		}
		finally
		{
			reqRes.close();
		}

		return list;
	}

	/**
	 * Get a list of primary keys as strings from a selection node or association by following a path within that table,
	 *
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @return the list of values, or an empty list
	 */
	public static List<String> getLinkedRecordKeyList(Adaptation record, Path path)
	{
		List<String> list = new ArrayList<String>();
		RequestResult reqRes = linkedRecordLookup(record, path);
		if (reqRes == null || reqRes.isEmpty())
		{
			return list;
		}

		try
		{
			for (Adaptation next; (next = reqRes.nextAdaptation()) != null;)
			{
				list.add(next.getOccurrencePrimaryKey().format());
			}
		}
		finally
		{
			reqRes.close();
		}

		return list;
	}

	/**
	 * Determine if there are any related records to the given record related by the path representing
	 * an association or selection node.
	 * 
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @return Boolean true if the iterator of related records is empty
	 */
	public static boolean isLinkedRecordListEmpty(Adaptation record, Path path)
	{
		RequestResult reqRes = linkedRecordLookup(record, path);
		return reqRes == null || reqRes.isEmpty();
	}

	/**
	 * Get first record from a selection node or association 
	 * (useful for singletons)
	 *
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @return the first record, or null if empty
	 */
	public static Adaptation getFirstRecordFromLinkedRecordList(Adaptation record, Path path)
	{
		return (Adaptation) getFirstRecordFromLinkedRecordList(record, path, null);
	}

	/**
	 * Get first value from a selection node or association by following a path within that table,
	 * or a the record if no attribute path is specified
	 * (useful for singletons)
	 * 
	 * @param record the record
	 * @param path the path of the selection node or association
	 * @param attributePath the path of the attribute on the selection node or association table
	 * @return the value or record, or null if empty
	 */
	public static Object getFirstRecordFromLinkedRecordList(
		Adaptation record,
		Path path,
		Path attributePath)
	{
		RequestResult reqRes = linkedRecordLookup(record, path);
		if (reqRes == null || reqRes.isEmpty())
		{
			return null;
		}
		try
		{
			Adaptation adaptation = reqRes.nextAdaptation();
			if (attributePath == null)
			{
				return adaptation;
			}
			else
			{
				return adaptation.get(attributePath);
			}
		}
		finally
		{
			reqRes.close();
		}
	}

	/**
	 * Given a parameterized predicate string (where parameters are of the form ${<pathString>},
	 * and using the provided value context, replace the parameters with the values of the paths
	 * specified.
	 * 
	 * @param predicate parameterized predicate string
	 * @param vc value context
	 * @return compiled predicate string
	 */
	public static String computeValuedPredicate(
		final String predicate,
		final ValueContextForValidation vc)
	{
		final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		final Matcher matcher = pattern.matcher(predicate);
		String computedPredicate = predicate;
		while (matcher.find())
		{
			final String predicatePart = matcher.group()
				.replaceAll("\\$\\{", "")
				.replaceAll("\\}", "");
			String predItem = "";
			String[] paths = predicatePart.split("\\|");
			for (int i = 0; i < paths.length; i++)
			{
				String path = paths[i];
				final Object value = vc.getValue(Path.parse(path));
				if (value != null)
				{
					predItem += value.toString();
					if (i < paths.length - 1)
					{
						predItem += "|";
					}
				}
				i++;

				computedPredicate = computedPredicate.replaceAll("\\$\\{" + path + "\\}", predItem);

			}
			//			computedPredicate = computedPredicate.replaceAll("\\$\\{", "'")
			//					.replaceAll("\\}", "'");
		}

		return computedPredicate;
	}

	/**
	 * Given a record <code>Adaptation</code> and a path representing a foreign key field, return the 
	 * target table of the foreign key.
	 * 
	 * @param record the record
	 * @param fkPath the path of the foreign key field
	 * @return AdaptationTable the target table
	 */
	public static AdaptationTable getFKTable(final Adaptation record, final Path fkPath)
	{
		if (record == null)
		{
			return null;
		}

		return getFKTable(record.createValueContext(), fkPath);
	}

	/**
	 * Given a record <code>ValueContext</code> and a path representing a foreign key field, return the 
	 * target table of the foreign key.
	 * 
	 * @param valueContext the record context
	 * @param fkPath the path of the foreign key field
	 * @return AdaptationTable the target table
	 */
	public static AdaptationTable getFKTable(final ValueContext valueContext, final Path fkPath)
	{
		if (valueContext == null)
		{
			return null;
		}

		SchemaNode fkNode = valueContext.getNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getTable(valueContext);
	}

	/**
	 * Given a record <code>Adaptation</code> and a path representing a foreign key field, return the 
	 * target record specified by the value of the foreign key field.
	 * 
	 * @param record the record
	 * @param fkPath the path of the foreign key field
	 * @return Adaptation the related record
	 */
	public static Adaptation followFK(final Adaptation record, final Path fkPath)
	{
		if (record == null)
		{
			return null;
		}

		SchemaNode fkNode = record.getSchemaNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecord(record);
	}

	/**
	 * Given a record <code>ValueContext</code> and a path representing a foreign key field, return the 
	 * target record specified by the value of the foreign key field.
	 * 
	 * @param valueContext the record context
	 * @param fkPath the path of the foreign key field
	 * @return Adaptation the related record
	 */
	public static Adaptation followFK(final ValueContext valueContext, final Path fkPath)
	{
		SchemaNode fkNode = valueContext.getNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecord(valueContext);
	}

	/**
	 * Given a record <code>Adaptation</code>, a path representing a foreign key field, and
	 * a path representing a field on the target record, return the value of that field on 
	 * the related record.
	 * 
	 * @param record the record
	 * @param fkPath the path of the foreign key field
	 * @param fkAttributePath the path of the field on the target record
	 * @return Object the value of the field on the related record
	 */
	public static Object followFK(
		final Adaptation record,
		final Path fkPath,
		final Path fkAttributePath)
	{
		final Adaptation target = followFK(record, fkPath);
		if (target == null)
		{
			return null;
		}
		return target.get(fkAttributePath);
	}

	/**
	 * Given a record <code>ValueContext</code>, a path representing a foreign key field, and
	 * a path representing a field on the target record, return the value of that field on 
	 * the related record.
	 * 
	 * @param valueContext the record context
	 * @param fkPath the path of the foreign key field
	 * @param fkAttributePath the path of the field on the target record
	 * @return Object the value of the field on the related record
	 */
	public static Object followFK(
		final ValueContext valueContext,
		final Path fkPath,
		final Path fkAttributePath)
	{
		final Adaptation target = followFK(valueContext, fkPath);
		if (target == null)
		{
			return null;
		}
		return target.get(fkAttributePath);
	}

	// TODO: Untested
	/**
	 * Given a record <code>Adaptation</code> and a path representing a repeating foreign key field,
	 * return the list of target records specified by the value of the foreign key field.
	 * 
	 * @param record the record
	 * @param fkPath the path of the repeating foreign key field
	 * @return List<Adaptation> the related records
	 */
	@SuppressWarnings("unchecked")
	public static List<Adaptation> followFKList(final Adaptation record, final Path fkPath)
	{
		SchemaNode fkNode = record.getSchemaNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecords(record);
	}

	/**
	 * Given a record <code>ValueContext</code> and a path representing a repeating foreign key field,
	 * return the list of target records specified by the value of the foreign key field.
	 * 
	 * @param valueContext the record context
	 * @param fkPath the path of the repeating foreign key field
	 * @return List<Adaptation> the related records
	 */
	@SuppressWarnings("unchecked")
	public static List<Adaptation> followFKList(final ValueContext valueContext, final Path fkPath)
	{
		SchemaNode fkNode = valueContext.getNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecords(valueContext);
	}

	/**
	 * Given a record <code>Adaptation</code>, a path representing a repeating foreign key field, and
	 * a path representing a field on the target record, return the list of the values of that field on 
	 * the related records.
	 * 
	 * @param record the record
	 * @param fkPath the path of the repeating foreign key field
	 * @param fkAttributePath the path of the field on the target record
	 * @param includeNulls indicator of whether the resulting collection should include null values
	 * @return List<?> that values of the field on the related records
	 */
	public static List<?> followFKList(
		final Adaptation record,
		final Path fkPath,
		final Path fkAttributePath,
		final boolean includeNulls)
	{
		final List<Adaptation> targetList = followFKList(record, fkPath);
		return getValuesForFKList(targetList, fkAttributePath, includeNulls);
	}

	/**
	 * Given a record <code>ValueContext</code>, a path representing a repeating foreign key field, and
	 * a path representing a field on the target record, return the list of the values of that field on 
	 * the related records.
	 * 
	 * @param valueContext the record context
	 * @param fkPath the path of the repeating foreign key field
	 * @param fkAttributePath the path of the field on the target record
	 * @param includeNulls indicator of whether the resulting collection should include null values
	 * @return List<?> that values of the field on the related records
	 */
	public static List<?> followFKList(
		final ValueContext valueContext,
		final Path fkPath,
		final Path fkAttributePath,
		final boolean includeNulls)
	{
		final List<Adaptation> targetList = followFKList(valueContext, fkPath);
		return getValuesForFKList(targetList, fkAttributePath, includeNulls);
	}

	private static List<?> getValuesForFKList(
		List<Adaptation> targetList,
		Path fkAttributePath,
		boolean includeNulls)
	{
		final List<Object> valueList = new ArrayList<Object>();
		for (Adaptation targetRecord : targetList)
		{
			Object value = targetRecord.get(fkAttributePath);
			if (includeNulls || value != null)
			{
				valueList.add(value);
			}
		}
		return valueList;
	}

	/**
	 * Get a record based on the xpath
	 *
	 * @deprecated Use {@link #getRecord(String, Adaptation, boolean, boolean)} instead. It allows you to specify
	 *             whether the xpath contains all of the pk fields, which will result in quicker lookup time.
	 *             This method will assume <code>false</code> for backwards compatibility, but it should be a
	 *             conscious decision by the caller.
	 * @param instance the data set
	 * @param xPathPredicate the xpath
	 */
	@Deprecated
	public static Adaptation getOccurrenceFromXPathExpression(
		final Adaptation instance,
		final String xPathPredicate)
	{
		if (xPathPredicate == null || "".equals(xPathPredicate))
			return null;
		final int indexOfOpeningBracket = xPathPredicate.indexOf('[');
		if (indexOfOpeningBracket < 0)
			return null;
		final int lenghtOfXPathPredicate = xPathPredicate.length();
		if (indexOfOpeningBracket >= lenghtOfXPathPredicate)
			return null;
		final String tablePath = xPathPredicate.substring(0, indexOfOpeningBracket);
		final AdaptationTable table = instance.getTable(Path.parse(tablePath));
		if (table == null)
			return null;
		final String predicate = xPathPredicate.substring(
			indexOfOpeningBracket + 1,
			lenghtOfXPathPredicate - 1);
		final RequestResult result = table.createRequestResult(predicate);
		if (result == null)
			return null;
		try
		{
			return result.nextAdaptation();
		}
		finally
		{
			result.close();
		}
	}

	/**
	 * Given a value context, return the record for which the context holds proposed values.
	 * Note well: {@link ValueContext#getAdaptationInstance()} cannot be used since it returns the
	 * data set.
	 * 
	 * @param valueContext the value context
	 * @return the record associated with the value context
	 */
	public static Adaptation getRecordForValueContext(ValueContext valueContext)
	{
		return valueContext.getAdaptationTable().lookupAdaptationByPrimaryKey(valueContext);
	}

	/**
	 * Given a field context (e.g. on a constraint) and a path to another field on the same record,
	 * return the value of the other field, specified by its adaptation-relative path.
	 * @param fieldContext
	 * @param pathToOtherField
	 * @return Object value of the other field
	 */
	public static Object getValueOfOtherField(ValueContext fieldContext, Path pathToOtherField)
	{
		SchemaNode fieldNode = fieldContext.getNode();
		Path fieldPath = fieldNode.getPathInAdaptation();
		int parentCount = fieldPath.getSize();
		for (int i = 0; i < parentCount; i++)
		{
			pathToOtherField = Path.PARENT.add(pathToOtherField);
		}
		return fieldContext.getValue(pathToOtherField);
	}

	/**
	 * Get the same record from a different data space, if it exists.
	 *
	 * @param record the record to look for
	 * @param otherDataSpace the other data space to find it in
	 * @return the record from the other data space, or null
	 */
	public static Adaptation getRecordFromOtherDataSpace(
		Adaptation record,
		AdaptationHome otherDataSpace)
	{
		if (record == null || otherDataSpace == null)
		{
			return null;
		}
		Adaptation dataSet = record.getContainer();
		Adaptation otherDataSet = otherDataSpace.findAdaptationOrNull(dataSet.getAdaptationName());
		if (otherDataSet == null)
		{
			return null;
		}
		AdaptationTable table = record.getContainerTable();
		AdaptationTable otherTable = otherDataSet.getTable(table.getTablePath());
		if (otherTable == null)
		{
			return null;
		}
		Adaptation otherRecord = otherTable.lookupAdaptationByPrimaryKey(record.getOccurrencePrimaryKey());
		return otherRecord;
	}

	/**
	 * Get the same record from the parent data space, if it exists
	 *
	 * @param record the record to look for
	 * @return the record from the parent data space, or null
	 */
	public static Adaptation getRecordFromParentDataSpace(Adaptation record)
	{
		if (record == null)
		{
			return null;
		}
		AdaptationHome parentDataSpace = record.getHome().getParentBranch();
		if (parentDataSpace == null)
		{
			return null;
		}
		return getRecordFromOtherDataSpace(record, parentDataSpace);
	}

	/**
	 * Get the same record from the initial snapshot data space, if it exists
	 *
	 * @param record the record to look for
	 * @return the record from the initial snapshot data space, or null
	 */
	public static Adaptation getRecordFromInitialSnapshot(Adaptation record)
	{
		if (record == null)
		{
			return null;
		}
		AdaptationHome parentDataSpace = record.getHome().getParent();
		if (parentDataSpace == null)
		{
			return null;
		}
		return getRecordFromOtherDataSpace(record, parentDataSpace);
	}

	/**
	 * Get a record based on the xpath
	 *
	 * @deprecated Use {@link #getRecord(String, Adaptation, boolean, boolean)} instead. It allows you to specify
	 *             whether the xpath contains all of the pk fields, which will result in quicker lookup time.
	 *             This method will assume <code>false</code> for backwards compatibility, but it should be a
	 *             conscious decision by the caller.
	 * @param recordXpath the xpath
	 * @param dataSet the data set
	 * @param errorIfNotFound Whether to consider not found to be an error
	 * @throws OperationException if an error occurs, or if not found and <code>errorIfNotFound</code> is <code>true</code>
	 */
	@Deprecated
	public static Adaptation getRecord(
		String recordXpath,
		Adaptation dataSet,
		boolean errorIfNotFound) throws OperationException
	{
		return getRecord(recordXpath, dataSet, false, errorIfNotFound);
	}

	/**
	 * Get a record based on the xpath.
	 * @see XPathExpressionHelper#lookupFirstRecordMatchingXPath(boolean, Adaptation, String)
	 *
	 * @param recordXpath the xpath
	 * @param dataSet the data set
	 * @param checkActualPrimaryKey Whether the xpath specifies each member of the primary key, for faster lookup
	 * @param errorIfNotFound Whether to consider not found to be an error
	 * @throws OperationException if an error occurs, or if not found and <code>errorIfNotFound</code> is <code>true</code>
	 */
	public static Adaptation getRecord(
		String recordXpath,
		Adaptation dataSet,
		boolean checkActualPrimaryKey,
		boolean errorIfNotFound) throws OperationException
	{
		if (recordXpath == null || dataSet == null)
		{
			return null;
		}
		Adaptation recordAdaptation = XPathExpressionHelper.lookupFirstRecordMatchingXPath(
			checkActualPrimaryKey,
			dataSet,
			recordXpath);
		if (recordAdaptation == null && errorIfNotFound)
		{
			throw OperationException.createError("Record for '" + recordXpath
				+ "' has not been found");
		}
		return recordAdaptation;
	}

	/**
	 * Return a user message with the specified message and severity where the message is prefixed
	 * by information about the record.
	 * 
	 * @param record the record
	 * @param msg the simple message
	 * @param severity the desired severity
	 * @return a UserMessage
	 */
	public static UserMessageString createUserMessage(
		Adaptation record,
		String msg,
		Severity severity)
	{
		String msgTxt = "Record " + record.getOccurrencePrimaryKey().format() + " in the "
			+ record.getContainerTable().getTableNode().getLabel(Locale.getDefault()) + " Table, "
			+ record.getLabel(Locale.getDefault()) + ": " + msg;

		return createUserMessage(msgTxt, severity);
	}

	/**
	 * Create and return a UserMessage with the specified message and severity
	 * @param msg the simple message
	 * @param severity the desired severity
	 * @return new UserMessage
	 */
	public static UserMessageString createUserMessage(String msg, Severity severity)
	{
		if (Severity.FATAL == severity)
		{
			return UserMessage.createFatal(msg);
		}
		if (Severity.ERROR == severity)
		{
			return UserMessage.createError(msg);
		}
		if (Severity.WARNING == severity)
		{
			return UserMessage.createWarning(msg);
		}
		return UserMessage.createInfo(msg);
	}

	/**
	 * Gets all of the tables for a data set.
	 * This is equivalent of <code>getAllTables(dataSet, dataSet.getSchemaNode())</code>
	 *
	 * @param dataSet the data set
	 * @return the tables for the data set, or an empty list if none exist
	 */
	public static List<AdaptationTable> getAllTables(Adaptation dataSet)
	{
		return getAllTables(dataSet, dataSet.getSchemaNode());
	}

	/**
	 * Gets all of the tables for a data set under the given node.
	 *
	 * @param dataSet the data set
	 * @param parentNode the schema node to look under
	 * @return the tables for the data set under the given node, or an empty list if none exist
	 */
	public static List<AdaptationTable> getAllTables(Adaptation dataSet, SchemaNode parentNode)
	{
		SchemaNode[] children = parentNode.getNodeChildren();
		ArrayList<AdaptationTable> tables = new ArrayList<AdaptationTable>();
		for (SchemaNode child : children)
		{
			if (child.isTableNode())
			{
				tables.add(dataSet.getTable(child.getPathInSchema()));
			}
			else
			{
				tables.addAll(getAllTables(dataSet, child));
			}
		}
		return tables;
	}

	/**
	 * Gets the label for a field
	 *
	 * @param adaptation the record or data set (for cases where you want a data set level field)
	 * @param fieldPath the path of the field within the given adaptation
	 * @param session the user's session
	 * @param includeGroupLabels Include the labels of the parent group(s) of the field
	 * @return the label
	 */
	public static String getFieldLabel(
		Adaptation adaptation,
		Path fieldPath,
		Session session,
		boolean includeGroupLabels)
	{
		SchemaNode node = adaptation.getSchemaNode().getNode(fieldPath);
		StringBuilder bldr = new StringBuilder();
		Locale locale = session.getLocale();
		bldr.append(node.getLabel(locale));
		if (includeGroupLabels)
		{
			Path tablePath = node.getTableNode().getPathInSchema();
			// Loop through the parents until you get to a table node and for each group add its label
			for (SchemaNode parentNode = node; (parentNode = parentNode.getNode(Path.PARENT)) != null
				&& !tablePath.equals(parentNode.getPathInSchema());)
			{
				bldr.insert(0, " / ");
				bldr.insert(0, parentNode.getLabel(locale));
			}
		}
		return bldr.toString();
	}

	/**
	 * Depending on the schema type associated with the specified node, values will be quoted in predicates.
	 * For example, all the string types and date types would have their values in quotes.
	 * @param node the schema node representing a field
	 * @return <code>true</code> if values for that field would require quotes
	 */
	public static boolean isValueQuotedInPredicate(SchemaNode node)
	{
		SchemaTypeName type = node.getXsTypeName();
		return !(SchemaTypeName.XS_BOOLEAN.equals(type) || SchemaTypeName.XS_DECIMAL.equals(type)
			|| SchemaTypeName.XS_INT.equals(type) || SchemaTypeName.XS_INTEGER.equals(type));
	}

	public static DifferenceBetweenOccurrences getRecordDifferencesInChildDataSpace(
		Adaptation record,
		boolean resolvedMode) throws OperationException
	{
		AdaptationHome childDataSpace = record.getHome();
		AdaptationHome initialSnapshot = childDataSpace.getParent();
		if (initialSnapshot == null)
		{
			throw OperationException.createError("No initial snapshot found for data space "
				+ childDataSpace.getKey().getName());
		}
		Adaptation dataSet = record.getContainer();
		Adaptation initialDataSet = initialSnapshot.findAdaptationOrNull(dataSet.getAdaptationName());
		if (initialDataSet == null)
		{
			throw OperationException.createError("Data set "
				+ dataSet.getAdaptationName().getStringName() + " not found in snapshot "
				+ initialSnapshot.getKey().getName());
		}
		AdaptationTable table = record.getContainerTable();
		AdaptationTable initialTable = initialDataSet.getTable(table.getTablePath());
		if (initialTable == null)
		{
			throw OperationException.createError("Table " + table.getTablePath().format()
				+ " not found in data set " + initialDataSet.getAdaptationName().getStringName()
				+ " in snapshot " + initialSnapshot.getKey().getName());
		}
		Adaptation initialRecord = initialTable.lookupAdaptationByName(record.getAdaptationName());
		if (initialRecord == null)
		{
			throw OperationException.createError("Record " + record.toXPathExpression()
				+ " not found in snapshot " + initialSnapshot.getKey().getName());
		}
		return DifferenceHelper.compareOccurrences(record, initialRecord, resolvedMode);
	}

	/**
	 * Return the association record identified by the two records it associates and the association table
	 * @param tablePath a path within the same data set as the key1 adaptation
	 * @param key1
	 * @param key2
	 * @return Adaptation the record corresponding th the association
	 */
	public static Adaptation getAssociationRecord(Path tablePath, Adaptation key1, Adaptation key2)
	{
		AdaptationTable assocTable = key2.getContainerTable()
			.getContainerAdaptation()
			.getTable(tablePath);
		PrimaryKey key = assocTable.computePrimaryKey(new Object[] {
				key1.getOccurrencePrimaryKey().format(), key2.getOccurrencePrimaryKey().format() });
		return assocTable.lookupAdaptationByPrimaryKey(key);
	}

	/**
	 * Evaluate a 'path' expression where each of the paths except the last item represents an adaptation path
	 * against the adaptation represented by the previous path and the first path is one against the provided record.
	 * @param recordContext
	 * @param paths
	 * @return list of objects representing the union of all evaluated paths
	 */
	public static List<Adaptation> evaluatePath(ValueContext recordContext, Path[] paths)
	{
		List<Adaptation> thisLevel = evaluateSingle(null, recordContext, paths[0]);
		for (int i = 1; i < paths.length; i++)
		{
			Path path = paths[i];
			List<Adaptation> nextLevel = new ArrayList<Adaptation>();
			for (Adaptation a : thisLevel)
			{
				if (a != null)
				nextLevel.addAll(evaluateSingle(a, null, path));
			}
			if (nextLevel.isEmpty())
				return null;
			thisLevel = nextLevel;
		}
		return thisLevel;
	}

	/**
	 * Starting from either a recordRoot (Adaptation) or a contextRoot (ValueContext), find the related records,
	 * a list of Adaptations, resulting from the provided Path.  The Path should represent a fk, fk list, association,
	 * or selection.  For an fk, the collection should contain 0 or 1 Adaptations.
	 * @param recordRoot
	 * @param contextRoot
	 * @param path
	 * @return related adaptations
	 */
	@SuppressWarnings("unchecked")
	public static List<Adaptation> evaluateSingle(
		Adaptation recordRoot,
		ValueContext contextRoot,
		Path path)
	{
		SchemaNode recordNode = recordRoot != null ? recordRoot.getSchemaNode()
			: contextRoot.getNode();
		SchemaNode fkNode = recordNode.getNode(path);
		SchemaFacetTableRef tableRef = fkNode.getFacetOnTableReference();
		if (tableRef != null)
		{

			if (fkNode.getMaxOccurs() > 1)
				return recordRoot != null ? tableRef.getLinkedRecords(recordRoot)
					: tableRef.getLinkedRecords(contextRoot);
			else
				return Collections.singletonList(recordRoot != null ? tableRef.getLinkedRecord(recordRoot)
					: tableRef.getLinkedRecord(contextRoot));
		}
		else if (fkNode.isAssociationNode() || fkNode.isSelectNode())
		{
			if (recordRoot == null)
				recordRoot = getRecordForValueContext(contextRoot);
			return recordRoot != null ? getLinkedRecordList(recordRoot, path)
				: Collections.<Adaptation> emptyList();
		}
		return Collections.<Adaptation> emptyList();
	}

	/**
	 * Evaluate a 'path' expression where each of the paths except the last item represents an adaptation path
	 * against the adaptation represented by the previous path and the first path is one against the provided record.
	 * @param record
	 * @param paths
	 * @return list of objects representing the union of all evaluated paths
	 */
	public static List<Adaptation> evaluatePath(Adaptation record, Path[] paths)
	{
		List<Adaptation> thisLevel = new ArrayList<Adaptation>();
		thisLevel.add(record);
		for (int i = 0; i < paths.length; i++)
		{
			Path path = paths[i];
			List<Adaptation> nextLevel = new ArrayList<Adaptation>();
			for (Adaptation a : thisLevel)
			{
				if (a != null)
				nextLevel.addAll(evaluateSingle(a, null, path));
			}
			if (nextLevel.isEmpty())
				return null;
			thisLevel = nextLevel;
		}
		return thisLevel;
	}





	/**
	 * Answers whether the specified node represents a relationship field (foreign key, association
	 * or selection node).
	 * 
	 * @param node the node
	 * @return <code>true</code> if the node represetns a relationship
	 */
	public static boolean isRelationshipNode(SchemaNode node)
	{
		SchemaFacetTableRef tableRef = node.getFacetOnTableReference();
		if (tableRef != null)
			return true;
		return node.isAssociationNode() || node.isSelectNode();
	}

	/**
	 * For a given node, if the node is a relationship node (foreign key, association or selection node),
	 * return the target table of the relationship.
	 * 
	 * @param node the node
	 * @return the table node of the related table or null if the node is not a relationship node
	 */
	public static SchemaNode getTableNodeForRelated(SchemaNode node)
	{
		return getTableNodeForRelated(node, null);
	}

	/**
	 * For a given node, if the node is a relationship node (foreign key, association or selection node),
	 * return the target table of the relationship.
	 * 
	 * @param node the node
	 * @param dataSet the data set for the table you are starting from
	 * @return the table node of the related table or null if the node is not a relationship node
	 */
	public static SchemaNode getTableNodeForRelated(SchemaNode node, Adaptation dataSet)
	{
		SchemaFacetTableRef tableRef = node.getFacetOnTableReference();
		if (tableRef != null)
		{
			return tableRef.getTableNode();
		}
		else if (node.isAssociationNode())
		{
			AssociationLink link = node.getAssociationLink();
			if (link == null) // bad state???
				return null;
			HomeKey dataSpaceKey = link.getDataSpaceReference();
			AdaptationName dataSetKey = link.getDataSetReference();
			SchemaNode dataSetRoot = node;
			if (dataSetKey != null)
			{
				if (dataSet == null)
					throw new IllegalArgumentException(
						"If an association uses a table from another data set, an original data set is required to find the target table");
				AdaptationHome dataSpace = dataSet.getHome();
				if (dataSpaceKey != null)
					dataSpace = dataSpace.getRepository().lookupHome(dataSpaceKey);
				dataSet = dataSpace.findAdaptationOrNull(dataSetKey);
				dataSetRoot = dataSet.getSchemaNode();
			}
			if (link.isLinkTable())
			{
				AssociationLinkByLinkTable alink = (AssociationLinkByLinkTable) link;
				Path targetPath = alink.getFieldToTargetPath();
				SchemaNode fieldNode = dataSetRoot.getNode(targetPath);
				return getTableNodeForRelated(fieldNode, dataSet);
			}
			else
			{
				AssociationLinkByTableRefInverse alink = (AssociationLinkByTableRefInverse) link;
				Path sourcePath = alink.getFieldToSourcePath();
				return dataSetRoot.getNode(sourcePath).getTableNode();
			}
		}
		else if (node.isSelectNode())
		{
			SelectionLink link = node.getSelectionLink();
			return link.getTableNode();
		}
		return null;
	}



	public static enum CompareOper {
		Equals, NotEquals, LessThan, LessThanOrEqual, GreaterThan, GreaterThanOrEqual, IsNull, IsNotNull, DateEarlier, DateEqual, DateLater
	};





	public static void buildPredicate(StringBuilder bldr, Path path, String value, CompareOper oper)
	{
		if (oper == null)
			oper = CompareOper.Equals;
		switch (oper)
		{
		case Equals:
			buildBinaryOperator(bldr, path, value, " = ");
			break;
		case NotEquals:
			buildBinaryOperator(bldr, path, value, " != ");
			break;
		case LessThan:
			buildBinaryOperator(bldr, path, value, " < ");
			break;
		case LessThanOrEqual:
			buildBinaryOperator(bldr, path, value, " <= ");
			break;
		case GreaterThan:
			buildBinaryOperator(bldr, path, value, " > ");
			break;
		case GreaterThanOrEqual:
			buildBinaryOperator(bldr, path, value, " >= ");
			break;
		case IsNull:
			buildUnaryFunction(bldr, path, "ods:is-null");
			break;
		case IsNotNull:
			buildUnaryFunction(bldr, path, "ods:is-not-null");
			break;
		case DateEarlier:
			buildBinaryFunction(bldr, path, value, "date-less-than");
			break;
		case DateEqual:
			buildBinaryFunction(bldr, path, value, "date-equal");
			break;
		case DateLater:
			buildBinaryFunction(bldr, path, value, "date-greater-than");
			break;
		}
	}
	private static void buildBinaryFunction(
		StringBuilder bldr,
		Path path,
		String value,
		String function)
	{
		bldr.append(function)
			.append("(")
			.append(path.format())
			.append(",")
			.append(value)
			.append(")");
	}

	private static void buildUnaryFunction(StringBuilder bldr, Path path, String function)
	{
		bldr.append(function).append("(").append(path.format()).append(")");
	}

	private static void buildBinaryOperator(StringBuilder bldr, Path path, String value, String oper)
	{
		bldr.append(path.format()).append(oper).append(value);
	}



	public static Adaptation getOneRecordOrThrowOperationException(
		final Repository aRepository,
		final String aBranchName,
		final String aAdapationName,
		final String aXpath) throws OperationException
	{
		final AdaptationHome home = getDataSpaceOrThrowOperationException(aRepository, aBranchName);
		final Adaptation instance = getDataSetOrThrowOperationException(home, aAdapationName);
		Request request = null;
		try
		{
			request = XPathExpressionHelper.createRequestForXPath(instance, aXpath);
		}
		catch (Exception ex)
		{
			throw OperationException.createError(ex);
		}
		final RequestResult result = request.execute();
		final Adaptation record;
		try
		{
			record = result.nextAdaptation();
			if (record == null)
			{
				throw OperationException.createError("No record found for xpath '" + aXpath + "'");
			}
			if (result.nextAdaptation() != null)
			{
				throw OperationException.createError("More than one record match xpath '" + aXpath
					+ "'");
			}
		}
		finally
		{
			result.close();
		}
		return record;
	}

	public static AdaptationHome getDataSpaceOrThrowOperationException(
		final Repository aRepository,
		final String aBranchName) throws OperationException
	{
		final AdaptationHome home = aRepository.lookupHome(HomeKey.forBranchName(aBranchName));
		if (home == null)
		{
			throw OperationException.createError("Data space '" + aBranchName + "' does not exist");
		}
		return home;
	}

	public static Adaptation getDataSetOrThrowOperationException(
		final AdaptationHome dataSpaceRef,
		final String dataSetName) throws OperationException
	{
		final Adaptation dataSetRef = dataSpaceRef.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		if (dataSetRef == null)
		{
			throw OperationException.createError("Data set '" + dataSetName + "' does not exist");
		}
		return dataSetRef;
	}

	public static Adaptation getRecordByPrimayKey(
		final Repository repository,
		final String dataspaceName,
		final String datasetName,
		final Path tablePath,
		final PrimaryKey primaryKey) throws OperationException
	{
		AdaptationHome dataspace = AdaptationUtil.getDataSpaceOrThrowOperationException(
			repository,
			dataspaceName);
		Adaptation dataset = AdaptationUtil.getDataSetOrThrowOperationException(
			dataspace,
			datasetName);
		AdaptationTable table = dataset.getTable(tablePath);
		return table.lookupAdaptationByPrimaryKey(primaryKey);
	}



	public static List<Adaptation> getRecords(RequestResult rr)
	{
		List<Adaptation> result = new ArrayList<>();
		try
		{
			for (Adaptation next; (next = rr.nextAdaptation()) != null;)
			{
				result.add(next);
			}
		}
		finally
		{
			rr.close();
		}
		return result;
	}

	/**
	 * Given a record's valueContext and a list of paths, return a list of values corresponding to
	 * those paths.
	 * @param recordContext
	 * @param paths
	 * @return list of values of paths
	 */
	public static List<Object> getPathValues(ValueContext recordContext, List<Path> paths)
	{
		List<Object> result = new ArrayList<Object>();
		for (Path commonValuePath : paths)
		{
			result.add(recordContext.getValue(commonValuePath));
		}
		return result;
	}

	/**
	 * Given a value context representing a record occurrence, return the primary key string of that record.
	 * @param recordContext
	 * @return string representation of the primary key
	 */
	public static String getOccurrencePrimaryKey(ValueContext recordContext)
	{
		AdaptationTable table = recordContext.getAdaptationTable();
		SchemaNode rootNode = table.getTableNode().getTableOccurrenceRootNode();
		Path[] keySpec = table.getPrimaryKeySpec();
		Object[] keyValues = new Object[keySpec.length];
		SchemaNode[] keyNodes = new SchemaNode[keySpec.length];
		for (int i = 0; i < keySpec.length; i++)
		{
			keyValues[i] = recordContext.getValue(keySpec[i]);
			keyNodes[i] = rootNode.getNode(Path.SELF.add(keySpec[i]));
		}
		return PrimaryKey.parseObjects(keyValues, keyNodes);
	}


	/**
	 * Given the reference information (e.g. from a tableRef node), and a current data set,
	 * find the related table.
	 * @param dataSet
	 * @param dataSpaceKey
	 * @param dataSetKey
	 * @param tablePath
	 * @return AdaptationTable that is referenced
	 */
	public static AdaptationTable getTable(
		Adaptation dataSet,
		HomeKey dataSpaceKey,
		AdaptationName dataSetKey,
		Path tablePath)
	{
		AdaptationHome dataSpace = dataSet.getHome();
		if (dataSpaceKey != null)
		{
			dataSpace = dataSpace.getRepository().lookupHome(dataSpaceKey);
		}
		if (dataSetKey != null)
		{
			dataSet = dataSpace.findAdaptationOrNull(dataSetKey);
		}
		return dataSet.getTable(tablePath);
	}
}
