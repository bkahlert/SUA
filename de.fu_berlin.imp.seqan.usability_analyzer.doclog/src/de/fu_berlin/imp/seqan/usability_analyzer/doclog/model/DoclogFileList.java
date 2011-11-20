package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DoclogFileList extends ArrayList<DoclogFile> implements
		HasDateRange {

	private static final long serialVersionUID = -7428136109215033397L;

	@Override
	public LocalDateRange getDateRange() {
		List<LocalDateRange> dateRanges = new ArrayList<LocalDateRange>();
		for (DoclogFile doclogFile : this) {
			dateRanges.add(doclogFile.getDateRange());
		}
		return LocalDateRange.calculateOuterDateRange(dateRanges
				.toArray(new LocalDateRange[0]));
	}

}
