package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;

public class TrunkUtils {
	public static File getTrunkFile(File trunkDirectory, String relativeFile) {
		return new File(trunkDirectory.getPath() + File.separator
				+ relativeFile);
	}

	public static File getTrunkFile(String relativeFile) {
		return new File(new SUADiffPreferenceUtil().getTrunkDirectory(),
				relativeFile);
	}
}
