package de.fu_berlin.imp.apiua.diff.model;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;

public interface IDiffRecordMeta extends HasDateRange {

	public String getFromFileName();

	public TimeZoneDate getFromFileDate();

	public String getToFileName();

	public TimeZoneDate getToFileDate();

}