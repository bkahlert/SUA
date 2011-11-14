package de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders;

import java.io.FileFilter;

public interface IFileFilterListener {
	public void fileFilterAdded(FileFilter fileFilter);

	public void fileFilterRemoved(FileFilter fileFilter);
}
