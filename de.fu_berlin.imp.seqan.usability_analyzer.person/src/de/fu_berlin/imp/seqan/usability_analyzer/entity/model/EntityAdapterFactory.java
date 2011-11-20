package de.fu_berlin.imp.seqan.usability_analyzer.entity.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class EntityAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { DiffFileList.class, DoclogFile.class,
				SurveyRecord.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Entity) {
			Entity entity = (Entity) adaptableObject;
			if (adapterType == DiffFileList.class) {
				return entity.getDiffFiles();
			}
			if (adapterType == DoclogFile.class) {
				return entity.getDoclogFile();
			}
			if (adapterType == SurveyRecord.class) {
				return entity.getSurveyRecord();
			}
			return null;
		}
		return null;
	}

}
