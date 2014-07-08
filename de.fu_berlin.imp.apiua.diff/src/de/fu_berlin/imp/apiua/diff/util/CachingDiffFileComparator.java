package de.fu_berlin.imp.apiua.diff.util;

import java.util.Comparator;
import java.util.HashMap;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.data.IData;

public class CachingDiffFileComparator implements Comparator<IData> {
	private HashMap<IData, String> locationsHashs = new HashMap<IData, String>();
	private HashMap<IData, TimeZoneDate> dates = new HashMap<IData, TimeZoneDate>();

	@Override
	public int compare(IData dataResource1, IData dataResource2) {
		String locationHash1 = this.locationsHashs.get(dataResource1);
		if (locationHash1 == null) {
			locationHash1 = DiffDataUtils.getLocationHash(dataResource1);
			this.locationsHashs.put(dataResource1, locationHash1);
		}
		String locationHash2 = this.locationsHashs.get(dataResource2);
		if (locationHash2 == null) {
			locationHash2 = DiffDataUtils.getLocationHash(dataResource2);
			this.locationsHashs.put(dataResource2, locationHash2);
		}

		TimeZoneDate date1 = this.dates.get(dataResource1);
		if (date1 == null) {
			date1 = DiffDataUtils.getDate(dataResource1, null);
			this.dates.put(dataResource1, date1);
		}
		TimeZoneDate date2 = this.dates.get(dataResource2);
		if (date2 == null) {
			date2 = DiffDataUtils.getDate(dataResource2, null);
			this.dates.put(dataResource2, date2);
		}

		int x = locationHash1.compareTo(locationHash2);
		return x != 0 ? x : date1.compareTo(date2);
	}
}