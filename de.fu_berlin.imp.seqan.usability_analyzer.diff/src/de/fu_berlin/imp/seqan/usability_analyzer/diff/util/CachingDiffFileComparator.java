package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;

public class CachingDiffFileComparator implements Comparator<File> {
	private HashMap<File, TimeZoneDate> map = new HashMap<File, TimeZoneDate>();

	@Override
	public int compare(File file1, File file2) {
		TimeZoneDate date1 = map.get(file1);
		if (date1 == null) {
			date1 = DiffFile.getDate(file1);
			map.put(file1, date1);
		}
		TimeZoneDate date2 = map.get(file2);
		if (date2 == null) {
			date2 = DiffFile.getDate(file2);
			map.put(file2, date2);
		}
		return date1.compareTo(date2);
	}
}