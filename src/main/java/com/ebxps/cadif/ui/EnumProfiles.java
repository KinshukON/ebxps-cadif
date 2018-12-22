package com.ebxps.cadif.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.base.misc.EbxRuntimeException;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.instance.ValueContextForValidation;
import com.orchestranetworks.schema.ConstraintContext;
import com.orchestranetworks.schema.ConstraintEnumeration;
import com.orchestranetworks.schema.InvalidSchemaException;
import com.orchestranetworks.service.Profile;
import com.orchestranetworks.service.Role;
import com.orchestranetworks.service.UserReference;
import com.orchestranetworks.service.directory.DirectoryDefault;

/**
 * Programmatic enumeration constraint to display a list of users and/or roles
 * taken from the default directory.
 * <p>Parameters control whether users and/or roles are included in the list.</p>
 * 
 *  @author Steve Higgins - Orchestra Networks - December 2015
 * 
 */
public class EnumProfiles implements ConstraintEnumeration
{

	private Category log = CadiRepository.getCategory();
	
	/** Parameter value indicating true. */
	private static final String BOOLEAN_TRUE = "true";

	/** The default user/role directory. */
	private DirectoryDefault directory = null;
	
	/** Flag to indicate whether roles should be displayed. */
	private boolean includeRoles = true;
	
	/** Flag to indicate whether users should be displayed. */
	private boolean includeUsers = true;

	/**
	 * Perform setup for this enumeration constraint.
	 * @param ctx The constraint context provided by EBX 
	 * @see com.orchestranetworks.schema.Constraint#setup(com.orchestranetworks.schema.ConstraintContext)
	 */
	public void setup(ConstraintContext ctx)
	{

		// Raise an error if both parameters are set to false
		if (includeRoles == false && includeUsers == false) {
			ctx.addError("Parameter Error - Component won't select any Profiles!");
			return;
		}
		
		// Locate the EBX Directory
		try
		{
			Repository repository = Repository.getDefault();
			directory = DirectoryDefault.getInstance(repository);
		}
		catch (Exception ex)
		{
			throw new EbxRuntimeException(ex);
		}
	}

	/**
	 * Return a list of profiles containing all specific roles and user references
	 * that are currently in the directory. Roles are listed first, followed by 
	 * user references.
	 * @param vContext the value context provided by EBX
	 * @see com.orchestranetworks.schema.ConstraintEnumeration#getValues(com.orchestranetworks.instance.ValueContext)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getValues(ValueContext vContext) throws InvalidSchemaException
	{
		List profList = new ArrayList();
		List tmpList = null;

		// Include roles if required
		if (includeRoles) {
			tmpList = directory.getAllSpecificRoles();
			for(Iterator i = tmpList.iterator(); i.hasNext();) {
				Profile p = (Profile) i.next();
				profList.add(p.format());
			}
		}

		// Include users if required
		if (includeUsers) {
			tmpList = directory.getAllUserReferences();
			for(Iterator i = tmpList.iterator(); i.hasNext();) {
				Profile p = (Profile) i.next();
				profList.add(p.format());
			}
		}

		log.debug(String.format("Profiles found:", profList.toString()));
		return profList;
	}

	/**
	 * Convert the value stored by EBX into something understandable for the user  
	 * @param value The value to be manipulated into a displayable String
	 * @param vContext The value context provided by EBX
	 * @param locale The user's current locale
	 * @see com.orchestranetworks.schema.ConstraintEnumeration#displayOccurrence(java.lang.Object, com.orchestranetworks.instance.ValueContext, java.util.Locale)
	 */
	public String displayOccurrence(Object value, ValueContext vContext, Locale locale) throws InvalidSchemaException
	{
		if (value == null || !(value instanceof String)) {
			return null;
		}
		
		Profile p = Profile.parse((String) value);

		String label = null;
		if (p.isUserReference()) {
			// Displays "userid (first-name last-name)"
			label = directory.displayUser((UserReference) p, Locale.getDefault());
		} else {
			// Displays "[role-description]" (square brackets indicate that this is a role, not a user)
			label = directory.displaySpecificRole((Role) p, Locale.getDefault());
		}
		
		return label;
		
	}
	
	/**
	 * Check the value that the user selected. Does nothing in this case.
	 * @param obj The occurrence object to be checked
	 * @param vCtx The value context provided by EBX   
	 * @see com.orchestranetworks.schema.Constraint#checkOccurrence(java.lang.Object, com.orchestranetworks.instance.ValueContextForValidation)
	 */
	public void checkOccurrence(Object obj, ValueContextForValidation vCtx)
		throws InvalidSchemaException
	{
		// Nothing to do here
	}

	/**
	 * @return the includeRoles
	 */
	public boolean isIncludeRoles() {
		return includeRoles;
	}

	/**
	 * @param includeRoles the includeRoles to set
	 */
	public void setIncludeRoles(String includeRoles) {
		this.includeRoles = includeRoles.equalsIgnoreCase(BOOLEAN_TRUE);
	}

	/**
	 * @return the includeUsers
	 */
	public boolean isIncludeUsers() {
		return includeUsers;
	}

	/**
	 * @param includeUsers the includeUsers to set
	 */
	public void setIncludeUsers(String includeUsers) {
		this.includeUsers = includeUsers.equalsIgnoreCase(BOOLEAN_TRUE);
	}

	/**
	 * Return a string to briefly describe this enumeration constraint.   
	 * @param locale The current user's locale
	 * @param vContext The value context provided by EBX
	 * @see com.orchestranetworks.schema.Constraint#toUserDocumentation(java.util.Locale, com.orchestranetworks.instance.ValueContext)
	 */
	public String toUserDocumentation(Locale locale, ValueContext vContext) throws InvalidSchemaException
	{
		if (includeRoles) {
			if (includeUsers) {
				return "must be a specific user or role defined to EBX";
			} else {
				return "must be a specific role defined to EBX";
			}
		} else {
			return "must be a specific user defined to EBX";
		}
	}


}
