package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.filters;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileListsViewerFileFilter extends ViewerFilter {

	private FileFilter fileFilter;

	public DiffFileListsViewerFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof DiffFileRecord) {
			DiffFileRecord diffFileRecord = (DiffFileRecord) element;
			return this.fileFilter
					.accept(new File(diffFileRecord.getFilename()));
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileFilter == null) ? 0 : fileFilter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffFileListsViewerFileFilter other = (DiffFileListsViewerFileFilter) obj;
		if (fileFilter == null) {
			if (other.fileFilter != null)
				return false;
		} else if (!fileFilter.equals(other.fileFilter))
			return false;
		return true;
	}

}
