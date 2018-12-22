package com.ebxps.cadif.adpatation;

import java.util.List;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;

/**
 * Various static utility methods.
 * 
 * @author Steve Higgins - August 2016
 *
 */
public class CadiRepository {

	/**
	 * Name of the CADI module
	 */
	public static final String CADI_MODULE_NAME = "ebxps-cadi";

	/**
	 * Gets a log4j Category for the CADI module.
	 * @return a log4j Category for the calling object
	 */
	public static Category getCategory() {
		return Category.getInstance(CadiRepository.CADI_MODULE_NAME);
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


	public static Adaptation findDatasetForModule(String moduleName) {
		return searchDataspace(Repository.getDefault().getReferenceBranch(), moduleName);
	}
	
	public static Adaptation findCadiDataset() {
		return searchDataspace(Repository.getDefault().getReferenceBranch(), CADI_MODULE_NAME);
	}
	
	/**
	 * Search a given dataspace and it's descendants for a dataset that implements the module's model.
	 * @param dataspace The starting datasoace
	 */
	private static Adaptation searchDataspace(AdaptationHome dataspace, String moduleName) {
	
		if (dataspace != null && dataspace.isOpenBranch()) {
	
			// Scan the dataset module names 
			@SuppressWarnings("unchecked")
			List<Adaptation> datasets = dataspace.findAllRoots();
			for (Adaptation dataset : datasets) {
				String tmpModuleName = dataset.getSchemaLocation().getModuleName();
				if (dataset.isActivated() && !dataset.isDeleted() && tmpModuleName != null && tmpModuleName.equals(moduleName)) {
					return dataset;
				}
			}
	
		}
	
		@SuppressWarnings("unchecked")
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
	 * Private constructor. This class shouldn't be instantiated.
	 */
	private CadiRepository() {
	}
	
}
