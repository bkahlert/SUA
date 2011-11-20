package de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public interface IDateRangeListener {
	public void dateRangeChanged(TimeZoneDateRange oldDateRange, TimeZoneDateRange newDateRange);
}
