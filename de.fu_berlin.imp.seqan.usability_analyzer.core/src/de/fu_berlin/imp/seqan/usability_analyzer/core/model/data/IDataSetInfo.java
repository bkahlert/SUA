package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data;

import java.util.Map;

import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public interface IDataSetInfo extends HasDateRange {

	public String getName();

	public Map<String, String> getUnknownProperties();

}