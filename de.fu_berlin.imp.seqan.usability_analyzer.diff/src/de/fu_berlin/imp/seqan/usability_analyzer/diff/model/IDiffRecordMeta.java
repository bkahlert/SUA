package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public interface IDiffRecordMeta extends HasDateRange {

	public String getFromFileName();

	public TimeZoneDate getFromFileDate();

	public String getToFileName();

	public TimeZoneDate getToFileDate();

}