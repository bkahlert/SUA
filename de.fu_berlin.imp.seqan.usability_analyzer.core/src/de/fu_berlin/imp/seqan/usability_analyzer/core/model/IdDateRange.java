package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdDateRange extends DateRange {

	private ID id;

	public IdDateRange(ID id, Date startDate, Date endDate) {
		super(startDate, endDate);
		this.id = id;
	}

	public IdDateRange(ID id, long startDate, long endDate) {
		super(startDate, endDate);
		this.id = id;
	}

	public ID getId() {
		return id;
	}

	public static Map<ID, List<DateRange>> group(List<IdDateRange> dateRanges) {
		Map<ID, List<DateRange>> groupedDateRanges = new HashMap<ID, List<DateRange>>();
		for (IdDateRange dateRange : dateRanges) {
			ID id = dateRange.getId();
			if (!groupedDateRanges.containsKey(id))
				groupedDateRanges.put(id, new ArrayList<DateRange>());
			groupedDateRanges.get(id).add(dateRange);
		}
		return groupedDateRanges;
	}
}
