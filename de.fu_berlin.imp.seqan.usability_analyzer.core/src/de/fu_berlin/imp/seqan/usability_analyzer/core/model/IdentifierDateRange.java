package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class IdentifierDateRange extends TimeZoneDateRange {

	private IIdentifier identifier;

	public IdentifierDateRange(IIdentifier id, TimeZoneDate startDate,
			TimeZoneDate endDate) {
		super(startDate, endDate);
		this.identifier = id;
	}

	public IIdentifier getIdentifier() {
		return this.identifier;
	}

	public static Map<IIdentifier, List<TimeZoneDateRange>> group(
			List<IdentifierDateRange> dateRanges) {
		Map<IIdentifier, List<TimeZoneDateRange>> groupedDateRanges = new HashMap<IIdentifier, List<TimeZoneDateRange>>();
		for (IdentifierDateRange dateRange : dateRanges) {
			IIdentifier identifier = dateRange.getIdentifier();
			if (!groupedDateRanges.containsKey(identifier)) {
				groupedDateRanges.put(identifier,
						new ArrayList<TimeZoneDateRange>());
			}
			groupedDateRanges.get(identifier).add(dateRange);
		}
		return groupedDateRanges;
	}
}
