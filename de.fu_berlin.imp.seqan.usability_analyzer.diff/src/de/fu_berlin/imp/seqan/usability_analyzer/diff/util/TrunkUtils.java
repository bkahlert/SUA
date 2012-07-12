package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

public class TrunkUtils {

	private File data;

	public TrunkUtils(File data) {
		this.data = data;
	}

	public static File getTrunkFile(File trunkDirectory, String relativeFile) {
		return new File(trunkDirectory.getPath() + File.separator
				+ relativeFile);
	}

	public File getTrunkFile(String relativeFile) {
		return new File(data + File.separator + "trunk" + File.separator,
				relativeFile);
	}
}
