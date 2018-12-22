package com.ebxps.cadif.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.instance.ValueContextForValidation;
import com.orchestranetworks.schema.ConstraintContext;
import com.orchestranetworks.schema.ConstraintEnumeration;
import com.orchestranetworks.schema.InvalidSchemaException;

/**
 * List all user-created dataspaces in the Repository.
 * 
 * @author Craig Cox - Orchestra Networks
 *
 */
public class EnumDataspace implements ConstraintEnumeration {

	
	private Category log = CadiRepository.getCategory();
	/**
	 * Component setup - none required.
	 */
	@Override
	public void setup(ConstraintContext aContext) {
		// No setup required
	}

	/**
	 * List the dataspaces.
	 */
	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException {

		List <String> list = new ArrayList<String>();
		AdaptationHome reference = Repository.getDefault().getReferenceBranch();
		list.add(reference.getKey().getName());
		list.addAll(getChildDataspaces(reference));

		return list;

	}


	/**
	 * List all of the open, non-technical dataspaces that exist under 
	 * a given dataspace.
	 * @param dataspace The starting dataspace
	 * @return A list of names of child dataspaces
	 */
	@SuppressWarnings("unchecked")
	private List<String> getChildDataspaces(AdaptationHome dataspace){
	
		List <String> list = new ArrayList<String>();
		List<AdaptationHome> dataspaceChildVersions = dataspace.getVersionChildren();
		for (AdaptationHome childVersion : dataspaceChildVersions){
			if (childVersion.isOpen()){
				List<AdaptationHome> dataspaceChildren = childVersion.getBranchChildren();
				for (AdaptationHome childDataspace: dataspaceChildren){
					if (childDataspace.isOpen() && !childDataspace.isTechnicalBranch()) {
						list.add(childDataspace.getKey().getName());
						list.addAll(getChildDataspaces(childDataspace));
					}
				}
			}
		}
		log.debug(String.format("Found dataspaces:", list.toString()));
		return list;
	
	}

	/**
	 * Return the full name/label for a particular dataset. 
	 * @param dataspaceName A value from the list returned by <code>getValues(..)</code> above.
	 * @param vCtx a value context provided by EBX 
	 * @param locale The current user's selected locale
	 * @return
	 * @throws InvalidSchemaException
	 */
	@Override
	public String displayOccurrence(Object dataspaceName, ValueContext vCtx, Locale locale)
			throws InvalidSchemaException {
	
		HomeKey key = HomeKey.forBranchName((String) dataspaceName);
		AdaptationHome dataspace = Repository.getDefault().lookupHome(key);
		
		if (dataspace.isBranchReference()) {
			return "Reference";
		}
		
		String label =  dataspace.getLabel(locale);
		if (label == null) {
			label = dataspace.getKey().getName();
		}

		return label + "   ["+ getPath(dataspace)+"]";
		
	}

	/**
	 * Return a path describing the parentage of a given dataspace.
	 * @param dataspace The dataspace
	 * @return A path description
	 */
	private String getPath(AdaptationHome dataspace){
	
		String dataSpacePath = "";
		AdaptationHome parentDataspace = dataspace.getParent();
		if (parentDataspace != null) {
			dataSpacePath = getPath(parentDataspace);
			if (dataspace.isBranch()) {
				String dataspaceName = dataspace.getKey().getName();
				dataSpacePath = dataSpacePath + dataspaceName + " / ";
			}
		} else {
			String dataspaceName = Repository.getDefault().getReferenceBranch().getKey().getName();
			dataSpacePath = dataSpacePath + dataspaceName + " / ";
		}
		
		return dataSpacePath;
	}

	/**
	 * Check a value supplied by the user.
	 */
	@Override
	public void checkOccurrence(Object aValue, ValueContextForValidation aValidationContext)
			throws InvalidSchemaException {
		// TODO Auto-generated method stub
	
	}

	/**
	 * Contribute a value to the document for the field that this component is attached to.
	 */
	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext) throws InvalidSchemaException {
		return "All data spaces listed in the current repository";
	}
	
}
