package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.model.ISurveyRecordItem;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.model.SurveyRecordItem;

public class SurveyContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	@SuppressWarnings("unused")
	private Viewer viewer;

	public SurveyContentProvider() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof SurveyRecord;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof SurveyRecord) {
			return getElements(parentElement);
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			Object[] objects = ((Collection<?>) inputElement).toArray();
			/*
			 * If the list contains only one element and this element is a list
			 * return the mentioned child list. This way we save one hierarchy
			 * level (= ID level).
			 */
			if (objects.length == 1 && objects[0] instanceof SurveyRecord) {
				return getElements((SurveyRecord) objects[0]);
			}
			return objects;
		}
		if (inputElement instanceof SurveyRecord) {
			List<ISurveyRecordItem> surveyRecordItems = new ArrayList<ISurveyRecordItem>();
			SurveyRecord surveyRecord = (SurveyRecord) inputElement;
			for (String key : surveyRecord.getKeys()) {
				surveyRecordItems.add(new SurveyRecordItem(surveyRecord, key));
			}
			return surveyRecordItems.toArray();
		}
		return new Object[0];
	}
}
