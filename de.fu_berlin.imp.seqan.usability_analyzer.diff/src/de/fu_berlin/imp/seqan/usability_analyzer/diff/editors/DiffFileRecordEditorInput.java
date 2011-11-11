package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileRecordEditorInput implements IEditorInput {

	private DiffFileRecord diffFileRecord;

	public DiffFileRecordEditorInput(DiffFileRecord diffFileRecord) {
		this.diffFileRecord = diffFileRecord;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return diffFileRecord.getFilename();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Displays a person";
	}

	public DiffFileRecord getDiffFileRecord() {
		return diffFileRecord;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((diffFileRecord == null) ? 0 : diffFileRecord.hashCode());
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
		DiffFileRecordEditorInput other = (DiffFileRecordEditorInput) obj;
		if (diffFileRecord == null) {
			if (other.diffFileRecord != null)
				return false;
		} else if (!diffFileRecord.equals(other.diffFileRecord))
			return false;
		return true;
	}

}
