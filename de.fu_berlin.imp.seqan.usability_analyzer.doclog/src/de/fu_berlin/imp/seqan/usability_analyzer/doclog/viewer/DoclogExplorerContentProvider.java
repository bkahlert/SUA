package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class DoclogExplorerContentProvider implements
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
			return ((List<?>) inputElement).toArray();
		}
		return new Object[0];
	}
}
