package de.fu_berlin.imp.apiua.diff.extensionProviders;

import java.io.FileFilter;

public interface IFileFilterListener {
	public void fileFilterAdded(FileFilter fileFilter);

	public void fileFilterRemoved(FileFilter fileFilter);
}
