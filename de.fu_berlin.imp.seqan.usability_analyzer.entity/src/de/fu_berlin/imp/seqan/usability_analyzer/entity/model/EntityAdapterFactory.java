package de.fu_berlin.imp.seqan.usability_analyzer.entity.model;

import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class EntityAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ID.class, Fingerprint.class, SurveyRecord.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Entity) {
			Entity entity = (Entity) adaptableObject;
			if (adapterType == ID.class) {
				return entity.getID();
			}
			if (adapterType == Fingerprint.class) {
				List<Fingerprint> fingerprints = entity.getFingerprints();
				return (fingerprints.size() > 0) ? fingerprints.get(0) : null;
			}
			if (adapterType == SurveyRecord.class) {
				return entity.getSurveyRecord();
			}
			return null;
		}
		return null;
	}

}
