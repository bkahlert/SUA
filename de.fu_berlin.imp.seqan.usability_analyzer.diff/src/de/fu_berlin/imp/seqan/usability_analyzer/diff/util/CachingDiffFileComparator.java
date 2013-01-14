package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.util.Comparator;
import java.util.HashMap;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;

public class CachingDiffFileComparator implements Comparator<IData> {
	private HashMap<IData, TimeZoneDate> map = new HashMap<IData, TimeZoneDate>();

	@Override
	public int compare(IData dataResource1, IData dataResource2) {
		TimeZoneDate date1 = map.get(dataResource1);
		if (date1 == null) {
			date1 = DiffDataUtils.getDate(dataResource1);
			map.put(dataResource1, date1);
		}
		TimeZoneDate date2 = map.get(dataResource2);
		if (date2 == null) {
			date2 = DiffDataUtils.getDate(dataResource2);
			map.put(dataResource2, date2);
		}
		return date1.compareTo(date2);
	}
}