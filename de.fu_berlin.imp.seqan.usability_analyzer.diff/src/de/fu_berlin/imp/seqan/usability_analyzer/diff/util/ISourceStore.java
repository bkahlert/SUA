package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.IOException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public interface ISourceStore {

	public File getSourceFile(ID id, long revision, String filename)
			throws IOException;

	public void setSourceFile(ID id, long revision, String filename, File file)
			throws IOException;

	public void clear();

}