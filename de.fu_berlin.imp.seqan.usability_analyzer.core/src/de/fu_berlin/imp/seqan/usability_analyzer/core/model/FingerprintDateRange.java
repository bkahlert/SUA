package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FingerprintDateRange extends LocalDateRange {

	private Fingerprint fingerprint;

	public FingerprintDateRange(Fingerprint id, LocalDate startDate,
			LocalDate endDate) {
		super(startDate, endDate);
		this.fingerprint = id;
	}

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public static Map<Fingerprint, List<LocalDateRange>> group(
			List<FingerprintDateRange> dateRanges) {
		Map<Fingerprint, List<LocalDateRange>> groupedDateRanges = new HashMap<Fingerprint, List<LocalDateRange>>();
		for (FingerprintDateRange dateRange : dateRanges) {
			Fingerprint fingerprint = dateRange.getFingerprint();
			if (!groupedDateRanges.containsKey(fingerprint))
				groupedDateRanges.put(fingerprint,
						new ArrayList<LocalDateRange>());
			groupedDateRanges.get(fingerprint).add(dateRange);
		}
		return groupedDateRanges;
	}
}
