package de.fu_berlin.imp.apiua.core.extensionPoints;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;

public interface IDateRangeListener {
	public void dateRangeChanged(TimeZoneDateRange oldDateRange, TimeZoneDateRange newDateRange);
}
