package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class DoclogScreenshotsContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	// private DoclogRecord doclogRecord;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// if (newInput instanceof DoclogRecord) {
		// this.doclogRecord = (DoclogRecord) newInput;
		// } else {
		// this.doclogRecord = null;
		// }
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
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof DoclogRecord) {
			return new Object[] { ((DoclogRecord) inputElement).getScreenshot() };
		} else {
			return new Object[0];
		}
	}
}
