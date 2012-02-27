package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.util.Arrays;
import java.util.List;

import difflib.PatchFailedException;

public class DiffUtils {
	/**
	 * Patches a string by a given patch
	 * 
	 * @param original
	 *            as list of lines
	 * @param patch
	 *            as list of lines; must be in <a href=
	 *            "http://www.artima.com/weblogs/viewpost.jsp?thread=164293"
	 *            >unified diff format</a>
	 * @return
	 * @throws PatchFailedException
	 */
	public static List<String> patch(List<String> original, List<String> patch)
			throws PatchFailedException {
		if (patch == null)
			throw new PatchFailedException("Patch must no be null");

		@SuppressWarnings("unchecked")
		List<String> newSource = (List<String>) difflib.DiffUtils.patch(
				original, difflib.DiffUtils.parseUnifiedDiff(patch));
		return newSource;
	}

	/**
	 * Patches a string by a given patch
	 * 
	 * @param original
	 * @param patch
	 *            as list of lines; must be in <a href=
	 *            "http://www.artima.com/weblogs/viewpost.jsp?thread=164293"
	 *            >unified diff format</a>
	 * @return
	 * @throws PatchFailedException
	 */
	public static List<String> patch(String original, List<String> patchLines)
			throws PatchFailedException {
		return patch(Arrays.asList(original.split("\n")), patchLines);
	}
}
