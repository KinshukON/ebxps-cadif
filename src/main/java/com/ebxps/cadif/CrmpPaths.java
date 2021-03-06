package com.ebxps.cadif;

import com.orchestranetworks.schema.Path;
/**
 * Generated by EBX5 5.8.1 fix A [1067:0002], at  2017/12/15 17:15:45 [GMT].
 * WARNING: Any manual changes to this class may be overwritten by generation process.
 * DO NOT MODIFY THIS CLASS.
 * 
 * This interface defines constants related to schema [Module: ebxps-crmp, path: /WEB-INF/ebx/schemas/ebxps-crmp-datamodel.xsd].
 * 
 * Root paths in this interface: 
 * 	'/crmp'   relativeToRoot: false
 * 
 */
public interface CrmpPaths
{
	// ===============================================
	// Constants for nodes under '/crmp'.
	// Prefix:  ''.
	// Statistics:
	//		106 path constants.
	//		43 leaf nodes.
	public static final Path _Crmp = Path.parse("/crmp");

	// Table type path
	public final class _System
	{
		private static final Path _System = _Crmp.add("system");
		public static Path getPathInSchema()
		{
			return _System;
		}
		public static final Path _SystemCode = Path.parse("./systemCode");
		public static final Path _SystemName = Path.parse("./systemName");
		public static final Path _Description = Path.parse("./description");
	} 

	// Table type path
	public final class _Object
	{
		private static final Path _Object = _Crmp.add("object");
		public static Path getPathInSchema()
		{
			return _Object;
		}
		public static final Path _ObjectCode = Path.parse("./objectCode");
		public static final Path _ObjectName = Path.parse("./objectName");
		public static final Path _Description = Path.parse("./description");
		public static final Path _AssocObjectTables = Path.parse("./assocObjectTables");
	} 

	// Table type path
	public final class _ObjectTable
	{
		private static final Path _ObjectTable = _Crmp.add("objectTable");
		public static Path getPathInSchema()
		{
			return _ObjectTable;
		}
		public static final Path _TableID = Path.parse("./tableID");
		public static final Path _SystemCode = Path.parse("./systemCode");
		public static final Path _ObjectId = Path.parse("./objectId");
		public static final Path _Dataspace = Path.parse("./dataspace");
		public static final Path _Dataset = Path.parse("./dataset");
		public static final Path _TableName = Path.parse("./tableName");
		public static final Path _XpathCriteria = Path.parse("./xpathCriteria");
		public static final Path _KeyColumn = Path.parse("./keyColumn");
		public static final Path _LinkToChildren = Path.parse("./linkToChildren");
		public static final Path _ChangeToColumn = Path.parse("./changeToColumn");
		public static final Path _PublishColumns = Path.parse("./publishColumns");
		public static final Path _PublishEndpoint = Path.parse("./publishEndpoint");
	} 

	// Table type path
	public final class _IntegrationLog
	{
		private static final Path _IntegrationLog = _Crmp.add("integrationLog");
		public static Path getPathInSchema()
		{
			return _IntegrationLog;
		}
		public static final Path _LogId = Path.parse("./logId");
		public static final Path _TriggerTimestamp = Path.parse("./triggerTimestamp");
		public static final Path _PublishTimestamp = Path.parse("./publishTimestamp");
		public static final Path _ResponseTimestamp = Path.parse("./responseTimestamp");
		public static final Path _TableID = Path.parse("./tableID");
		public static final Path _SystemCode = Path.parse("./systemCode");
		public static final Path _ObjectCode = Path.parse("./objectCode");
		public static final Path _Action = Path.parse("./action");
		public static final Path _ObjectId = Path.parse("./objectId");
		public static final Path _ResponseMsg = Path.parse("./responseMsg");
		public static final Path _PublishColumns = Path.parse("./publishColumns");
		public static final Path _PublishColumns_Name = Path.parse("./publishColumns/name");
		public static final Path _PublishColumns_Value = Path.parse("./publishColumns/value");
	} 
	public static final Path _Endpoints = _Crmp.add("endpoints");

	// Table type path
	public final class _Endpoints_Target
	{
		private static final Path _Endpoints_Target = _Endpoints.add("target");
		public static Path getPathInSchema()
		{
			return _Endpoints_Target;
		}
		public static final Path _Name = Path.parse("./name");
		public static final Path _Userid = Path.parse("./userid");
		public static final Path _Password = Path.parse("./password");
		public static final Path _EndpointReferences = Path.parse("./endpointReferences");
	} 

	// Table type path
	public final class _Endpoints_Endpoint
	{
		private static final Path _Endpoints_Endpoint = _Endpoints.add("endpoint");
		public static Path getPathInSchema()
		{
			return _Endpoints_Endpoint;
		}
		public static final Path _Id = Path.parse("./id");
		public static final Path _Target = Path.parse("./target");
		public static final Path _EndpointName = Path.parse("./endpointName");
		public static final Path _Uri = Path.parse("./uri");
		public static final Path _Objects = Path.parse("./objects");
		public static final Path _HttpMethod = Path.parse("./httpMethod");
		public static final Path _Userid = Path.parse("./userid");
		public static final Path _Password = Path.parse("./password");
	} 
	// ===============================================

}
