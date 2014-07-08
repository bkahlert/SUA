package de.fu_berlin.imp.apiua.diff.util;

import java.io.File;
import java.io.IOException;

import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;

public interface ISourceStore {

	public File getSourceFile(IIdentifier id, String revision, String filename)
			throws IOException;

	public void setSourceFile(IIdentifier id, String revision, String filename,
			File file) throws IOException;

	public void clear();

}