package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DoclogFileList extends ArrayList<DoclogFile> implements
		HasDateRange {

	private static final long serialVersionUID = -7428136109215033397L;

	@Override
	public TimeZoneDateRange getDateRange() {
		List<TimeZoneDateRange> dateRanges = new ArrayList<TimeZoneDateRange>();
		for (DoclogFile doclogFile : this) {
			dateRanges.add(doclogFile.getDateRange());
		}
		return TimeZoneDateRange.calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

}
