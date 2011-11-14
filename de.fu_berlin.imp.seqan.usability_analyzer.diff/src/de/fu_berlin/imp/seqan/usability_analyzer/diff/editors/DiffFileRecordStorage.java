package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileRecordStorage implements IStorage {

	private DiffFileRecord diffFileRecord;

	public DiffFileRecordStorage(DiffFileRecord diffFileRecord) {
		this.diffFileRecord = diffFileRecord;
	}

	public String getName() {
		return this.diffFileRecord.getFilename();
	}

	public InputStream getContents() throws CoreException {
		if (this.diffFileRecord != null
				&& this.diffFileRecord.getSource() != null)
			return new ByteArrayInputStream(this.diffFileRecord.getSource()
					.getBytes());
		else
			return null;
	}

	public IPath getFullPath() {
		return null;
	}

	public boolean isReadOnly() {
		return true;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

}
