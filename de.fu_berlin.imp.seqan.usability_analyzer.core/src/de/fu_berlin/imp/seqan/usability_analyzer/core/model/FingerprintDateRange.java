package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FingerprintDateRange extends DateRange {

	private Fingerprint fingerprint;

	public FingerprintDateRange(Fingerprint id, Date startDate, Date endDate) {
		super(startDate, endDate);
		this.fingerprint = id;
	}

	public FingerprintDateRange(Fingerprint id, long startDate, long endDate) {
		super(startDate, endDate);
		this.fingerprint = id;
	}

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public static Map<Fingerprint, List<DateRange>> group(
			List<FingerprintDateRange> dateRanges) {
		Map<Fingerprint, List<DateRange>> groupedDateRanges = new HashMap<Fingerprint, List<DateRange>>();
		for (FingerprintDateRange dateRange : dateRanges) {
			Fingerprint fingerprint = dateRange.getFingerprint();
			if (!groupedDateRanges.containsKey(fingerprint))
				groupedDateRanges.put(fingerprint, new ArrayList<DateRange>());
			groupedDateRanges.get(fingerprint).add(dateRange);
		}
		return groupedDateRanges;
	}
}
