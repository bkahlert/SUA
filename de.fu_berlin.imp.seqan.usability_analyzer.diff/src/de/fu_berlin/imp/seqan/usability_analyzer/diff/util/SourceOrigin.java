package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

public class SourceOrigin {
	private File sourcesDirectory;

	public SourceOrigin(File sourcesDirectory) {
		super();
		this.sourcesDirectory = sourcesDirectory;
	}

	public File getOriginSourceFile(String filename) {
		return TrunkUtils.getTrunkFile(sourcesDirectory, filename);
	}
}
