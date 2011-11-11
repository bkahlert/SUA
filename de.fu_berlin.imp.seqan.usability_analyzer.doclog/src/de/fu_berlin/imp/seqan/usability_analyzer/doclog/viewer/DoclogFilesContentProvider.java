package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class DoclogFilesContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {

	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof DoclogRecord) {
			return ((DoclogRecord) element).getDoclogPath();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DoclogFile) {
			return ((DoclogFile) element).getDoclogRecords().size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DoclogFile) {
			return ((DoclogFile) parentElement).getDoclogRecords().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			Object[] objects = ((List<?>) inputElement).toArray();
			/*
			 * If the list contains only one element and this element is a list
			 * return the mentioned child list. This way we save one hierarchy
			 * level (= ID level).
			 */
			if (objects.length == 1 && objects[0] instanceof DoclogFile) {
				return ((DoclogFile) objects[0]).getDoclogRecords().toArray();
			}
			return objects;
		}
		return new Object[0];
	}
}
