package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FingerprintDateRange extends TimeZoneDateRange {

	private Fingerprint fingerprint;

	public FingerprintDateRange(Fingerprint id, TimeZoneDate startDate,
			TimeZoneDate endDate) {
		super(startDate, endDate);
		this.fingerprint = id;
	}

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public static Map<Fingerprint, List<TimeZoneDateRange>> group(
			List<FingerprintDateRange> dateRanges) {
		Map<Fingerprint, List<TimeZoneDateRange>> groupedDateRanges = new HashMap<Fingerprint, List<TimeZoneDateRange>>();
		for (FingerprintDateRange dateRange : dateRanges) {
			Fingerprint fingerprint = dateRange.getFingerprint();
			if (!groupedDateRanges.containsKey(fingerprint))
				groupedDateRanges.put(fingerprint,
						new ArrayList<TimeZoneDateRange>());
			groupedDateRanges.get(fingerprint).add(dateRange);
		}
		return groupedDateRanges;
	}
}
