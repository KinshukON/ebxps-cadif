package com.ebxps.cadif.tabletrigger;

import com.ebxps.cadif.adpatation.CadiRepository;
import com.onwbp.org.apache.log4j.Category;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.OperationException;


/**
 * This table trigger updates the field named in the trigger parameter with the value from the
 * session tracking information.
 * @author Craig Cox - Orchestra Networks March 2017
 *
 */

public class TrackingInfoTableTrigger extends TableTrigger {

	private Category log = CadiRepository.getCategory();
	private String cadiInfoTag = "CADI{";
	private String trackingInfoFieldPath;
	private String defaultValue;


	public String getTrackingInfoFieldPath() {
		return trackingInfoFieldPath;
	}


	public void setTrackingInfoFieldPath(String trackingInfoFieldPath) {
		this.trackingInfoFieldPath = trackingInfoFieldPath;
	}


	public String getDefaultValue() {
		return getTrackinigData(defaultValue);
	}


	public void setDefaultValue(String defaultValue) {
		this.defaultValue = cadiInfoTag + defaultValue + "}" ;
	}


	@Override
	public void setup(TriggerSetupContext aContext) {

	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext aContext) throws OperationException {

		aContext.setAllPrivileges();
		String trackngInfo = aContext.getSession().getTrackingInfo();
		if (trackngInfo == null || trackngInfo.isEmpty()){
			String trackingFieldValue = (String) aContext.getOccurrenceContext().getValue(Path.parse(trackingInfoFieldPath));
			if (trackingFieldValue == null){
				trackngInfo = defaultValue;
			}else{
				trackngInfo = cadiInfoTag + trackingFieldValue +"}";
			}
		}

		log.debug(String.format("Setting record tracking info to [%s] for %s", trackngInfo, aContext.getOccurrenceContext().toString()));
		aContext.getOccurrenceContextForUpdate().setValue(getTrackinigData(trackngInfo), Path.parse(trackingInfoFieldPath));

	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext aContext) throws OperationException {

		aContext.setAllPrivileges();
		String trackngInfo = aContext.getSession().getTrackingInfo();
		if (trackngInfo == null || trackngInfo.isEmpty()){
			ValueChange change = aContext.getChanges().getChange(Path.parse(trackingInfoFieldPath));
			if (change != null){
				if (change.getValueBefore()== null){
					if (change.getValueAfter() != null){
						trackngInfo = cadiInfoTag +(String) change.getValueAfter() +"}";
					}
				}else{
					if(change.getValueBefore().equals(change.getValueAfter())){
						trackngInfo = defaultValue;
					}else{
						trackngInfo = cadiInfoTag +(String) change.getValueAfter() +"}";
					}
				}
			}else{
				trackngInfo = defaultValue;
			}
		}
		log.debug(String.format("Setting record tracking info to [%s] for %s", trackngInfo, aContext.getOccurrenceContext().toString()));
		aContext.getOccurrenceContextForUpdate().setValue(getTrackinigData(trackngInfo), Path.parse(trackingInfoFieldPath));

	}

	private String getTrackinigData (String trackinginfo){

		String trackingData = "";
		int start = trackinginfo.indexOf(cadiInfoTag);
		if (start < 0) { return null; }
		int end = trackinginfo.indexOf("}");
		trackingData = trackinginfo.substring(start+cadiInfoTag.length(), end);

		return trackingData;
	}

}
