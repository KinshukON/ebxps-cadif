package com.ebxps.cadif.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationName;
import com.onwbp.org.apache.log4j.Category;
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
 * <p>Enumerate all of the datasets in a dataspace.</p>
 * <p>The name of the dataspace to use is held in a field elsewhere in the record. The path
 * to this field is passed in the <i>dataspace</i> component parameter.
 * 
 * @author Craig Cox - Orchestra Networks 
 *
 */
public class EnumDataset implements ConstraintEnumeration {

	private Category log = CadiRepository.getCategory();
	private Path pathToDataspace = null;
	private SchemaNode dataspaceField = null;
	private AdaptationHome dataspace = null;
	
	/**
	 * Check that the parameter is valid. Raise schema warnings if it's not.
	 * @param cCtx A constraint context provided by EBX
	 */
	@Override
	public void setup(ConstraintContext cCtx) {

		if (pathToDataspace == null) {
			cCtx.addWarning("No 'dataspace' parameter has been supplied");
		}
		
		dataspaceField = cCtx.getSchemaNode().getNode(pathToDataspace, true, false);
		if (dataspaceField == null) {
			cCtx.addWarning("Dataspace parameter value ["+pathToDataspace.format()+"] is an invalid path.");
		}

	}

	/**
	 * Return a list of all of the datasets in the named dataspace.
	 * @param vCtx A value context provided by EBX
	 * @return A list of technical names of datasets in the named dataspace
	 * @throws InvalidSchemaException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getValues(ValueContext vCtx) throws InvalidSchemaException {

		// Find the dataspace
		dataspace = getDataspace(vCtx);
		if (dataspace == null) {
			log.error(String.format("Dataspace [%s] cannot be found", pathToDataspace));
			return null;
		}

		// Return a list of all datasets in the dataspace
		List<String> list = new ArrayList<String>();
		List<Adaptation> datasets = dataspace.findAllRoots();
		for (Adaptation dataset : datasets){ 
			list.add(dataset.getAdaptationName().getStringName());
			List<Adaptation> decendants = dataspace.findAllDescendants(dataset);
			for (Adaptation decendant : decendants){
				list.add(decendant.getAdaptationName().getStringName());
			}
		}
		log.debug(String.format("Found datasets:", list.toString()));
		return list;
	}

	/**
	 * Return the full name/label for a particular dataset. 
	 * @param datasetName A value from the list returned by <code>getValues(..)</code> above.
	 * @param vCtx a value context provided by EBX 
	 * @param locale The current user's selected locale
	 * @return
	 * @throws InvalidSchemaException
	 */
	@Override
	public String displayOccurrence(Object datasetName, ValueContext vCtx, Locale locale)
			throws InvalidSchemaException {
		if (dataspace==null) {
			dataspace = getDataspace(vCtx);
		}
		Adaptation dataset = dataspace.findAdaptationOrNull(AdaptationName.forName((String)datasetName));
		return dataset != null ? dataset.getLabelOrName(Locale.getDefault()) : (String) datasetName;
		
	}

	/**
	 * Check that the dataset supplied by the user exists in the target dataspace.
	 */
	@Override
	public void checkOccurrence(Object datasetName, ValueContextForValidation vCtx)
			throws InvalidSchemaException {

		AdaptationHome dataspace = getDataspace(vCtx);
		Adaptation dataset = dataspace.findAdaptationOrNull(AdaptationName.forName((String)datasetName));
		if (dataset == null) {
			vCtx.addError("Please select a dataset from the list");
		}
		
	}

	/**
	 * Contribute some text to the documentation panel for the field this component is attached to.
	 * @param locale The current user's locale
	 * @param vCtx A value context provided by EBX
	 * @return A string describing the contents of this field.
	 */
	@Override
	public String toUserDocumentation(Locale locale, ValueContext vCtx) throws InvalidSchemaException {
		return "Must be a dataset within dataspace named in field <b><i>" + dataspaceField.getLabel(locale) + "</i></b>";
	}

	/**
	 * Find the dataspace using the component parameter.
	 * @param vCtx A value context provided by EBX
	 * @return The dataspace, or null if it couldn't be found
	 */
	private AdaptationHome getDataspace(ValueContext vCtx) {
		String dataspaceName = (String) vCtx.getValue(pathToDataspace);
		if (dataspaceName != null && !dataspaceName.isEmpty()) {
			return Repository.getDefault().lookupHome(HomeKey.forBranchName(dataspaceName));
		} else {
			return null;
		}
		
	}
	
	public String getDataspace() {
		return pathToDataspace.format();
	}

	public void setDataspace(String dataspace) {
		if (dataspace != null && !dataspace.isEmpty()) {
			this.pathToDataspace = Path.parse(dataspace);
		}
	}

}
