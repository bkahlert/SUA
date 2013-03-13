package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.IOException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public interface ISourceStore {

	public File getSourceFile(IIdentifier id, long revision, String filename)
			throws IOException;

	public void setSourceFile(IIdentifier id, long revision, String filename, File file)
			throws IOException;

	public void clear();

}