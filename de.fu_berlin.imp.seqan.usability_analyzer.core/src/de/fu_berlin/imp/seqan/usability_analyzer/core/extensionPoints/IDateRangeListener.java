package de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;

public interface IDateRangeListener {
	public void dateRangeChanged(LocalDateRange oldDateRange, LocalDateRange newDateRange);
}
