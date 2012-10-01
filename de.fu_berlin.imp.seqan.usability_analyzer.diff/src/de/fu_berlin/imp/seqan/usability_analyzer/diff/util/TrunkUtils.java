package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

public class TrunkUtils {

	private File data;

	public TrunkUtils(File data) {
		this.data = data;
	}

	public File getTrunkFile(String relativeFile) {
		return new File(data, relativeFile);
	}

	public static File getTrunkFile(File trunkDir, String relativeFile) {
		return new File(trunkDir, relativeFile);
	}
}
