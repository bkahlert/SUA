package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdDateRange extends TimeZoneDateRange {

	private ID id;

	public IdDateRange(ID id, TimeZoneDate startDate, TimeZoneDate endDate) {
		super(startDate, endDate);
		this.id = id;
	}

	public ID getId() {
		return id;
	}

	public static Map<ID, List<TimeZoneDateRange>> group(
			List<IdDateRange> dateRanges) {
		Map<ID, List<TimeZoneDateRange>> groupedDateRanges = new HashMap<ID, List<TimeZoneDateRange>>();
		for (IdDateRange dateRange : dateRanges) {
			ID id = dateRange.getId();
			if (!groupedDateRanges.containsKey(id))
				groupedDateRanges.put(id, new ArrayList<TimeZoneDateRange>());
			groupedDateRanges.get(id).add(dateRange);
		}
		return groupedDateRanges;
	}
}
