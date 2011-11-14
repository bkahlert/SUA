package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecordList;

public class DiffFileListsContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {

	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof DiffFile) {
			return null;
		}
		if (element instanceof DiffFileRecord) {
			return ((DiffFileRecord) element).getDiffFile();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DiffFileList) {
			return ((DiffFileList) element).size() > 0;
		}
		if (element instanceof DiffFile) {
			DiffFileRecordList diffFileRecords = ((DiffFile) element)
					.getDiffFileRecords();
			return diffFileRecords != null && diffFileRecords.size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DiffFileList) {
			return ((DiffFileList) parentElement).toArray();
		}
		if (parentElement instanceof DiffFile) {
			DiffFileRecordList diffFileRecords = ((DiffFile) parentElement)
					.getDiffFileRecords();
			return diffFileRecords != null ? diffFileRecords.toArray()
					: new Object[0];
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
			if (objects.length == 1 && objects[0] instanceof DiffFileList) {
				return ((DiffFileList) objects[0]).toArray();
			}
			return objects;
		}
		return new Object[0];
	}
}
