package de.fu_berlin.imp.seqan.usability_analyzer.person.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class PersonAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { DiffFileList.class, DoclogFile.class,
				SurveyRecord.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Person) {
			Person person = (Person) adaptableObject;
			if (adapterType == DiffFileList.class) {
				return person.getDiffFiles();
			}
			if (adapterType == DoclogFile.class) {
				return person.getDoclogFile();
			}
			if (adapterType == SurveyRecord.class) {
				return person.getSurveyRecord();
			}
			return null;
		}
		return null;
	}

}
