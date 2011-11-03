package de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;

public interface IDateRangeListener {
	public void dateRangeChanged(DateRange oldDateRange, DateRange newDateRange);
}
