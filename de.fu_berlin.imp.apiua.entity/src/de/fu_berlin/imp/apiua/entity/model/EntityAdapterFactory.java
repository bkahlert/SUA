package de.fu_berlin.imp.apiua.entity.model;

import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.apiua.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.apiua.core.model.identifier.ID;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.model.identifier.Token;
import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyRecord;

public class EntityAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IIdentifier.class, ID.class, Fingerprint.class,
				CSVSurveyRecord.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Entity) {
			Entity entity = (Entity) adaptableObject;
			if (adapterType == IIdentifier.class) {
				return entity.getIdentifier();
			}
			if (adapterType == ID.class) {
				return entity.getId();
			}
			if (adapterType == Fingerprint.class) {
				List<Fingerprint> fingerprints = entity.getFingerprints();
				return (fingerprints.size() > 0) ? fingerprints.get(0) : null;
			}
			if (adapterType == Token.class) {
				return entity.getToken();
			}
			if (adapterType == CSVSurveyRecord.class) {
				return entity.getSurveyRecord();
			}
			return null;
		}
		return null;
	}

}
