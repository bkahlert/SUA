package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdDateRange extends LocalDateRange {

	private ID id;

	public IdDateRange(ID id, LocalDate startDate, LocalDate endDate) {
		super(startDate, endDate);
		this.id = id;
	}

	public ID getId() {
		return id;
	}

	public static Map<ID, List<LocalDateRange>> group(
			List<IdDateRange> dateRanges) {
		Map<ID, List<LocalDateRange>> groupedDateRanges = new HashMap<ID, List<LocalDateRange>>();
		for (IdDateRange dateRange : dateRanges) {
			ID id = dateRange.getId();
			if (!groupedDateRanges.containsKey(id))
				groupedDateRanges.put(id, new ArrayList<LocalDateRange>());
			groupedDateRanges.get(id).add(dateRange);
		}
		return groupedDateRanges;
	}
}
