package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;

public class DiffExplorerContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

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
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DiffFileList) {
			return ((DiffFileList) element).size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DiffFileList) {
			return ((DiffFileList) parentElement).toArray();
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			return ((List<?>) inputElement).toArray();
		}
		return new Object[0];
	}
}
