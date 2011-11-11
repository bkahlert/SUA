package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DiffFileList extends ArrayList<DiffFile> implements HasDateRange {

	private static final long serialVersionUID = 1327362495545624012L;

	@Override
	public DateRange getDateRange() {
		List<DateRange> dateRanges = new ArrayList<DateRange>();
		for (DiffFile diffFile : this) {
			dateRanges.add(diffFile.getDateRange());
		}
		return DateRange.calculateOuterDateRange(dateRanges
				.toArray(new DateRange[0]));
	}

	private int getIndex(DiffFile diffFile) {
		int i = -1;
		for (i = 0; i < this.size(); i++)
			if (this.get(i).equals(diffFile))
				break;
		return i;
	}

	public DiffFile getSuccessor(DiffFile diffFile) {
		int i = getIndex(diffFile);

		if (i >= this.size() - 1)
			return null;

		if (i + 1 < this.size())
			return this.get(i + 1);
		else
			return null;
	}
}
