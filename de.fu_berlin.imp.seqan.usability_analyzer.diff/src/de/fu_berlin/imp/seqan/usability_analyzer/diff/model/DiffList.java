package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DiffList extends ArrayList<Diff> implements HasDateRange {

	private static final long serialVersionUID = 1327362495545624012L;

	@Override
	public TimeZoneDateRange getDateRange() {
		List<TimeZoneDateRange> dateRanges = new ArrayList<TimeZoneDateRange>();
		for (Diff diff : this) {
			dateRanges.add(diff.getDateRange());
		}
		return TimeZoneDateRange.calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

	private int getIndex(IDiff diff) {
		int i = -1;
		for (i = 0; i < this.size(); i++)
			if (this.get(i).equals(diff))
				break;
		return i;
	}

	public IDiff getSuccessor(IDiff diff) {
		int i = getIndex(diff);

		if (i >= this.size() - 1)
			return null;

		if (i + 1 < this.size())
			return this.get(i + 1);
		else
			return null;
	}

	/**
	 * Returns all {@link DiffRecord}'s describing the same file.
	 * 
	 * @param filename
	 * @return
	 */
	public DiffRecordHistory getHistory(String filename) {
		DiffRecordHistory history = new DiffRecordHistory();
		for (IDiff diff : this) {
			DiffRecordList diffFileRecords = diff.getDiffFileRecords();
			if (diffFileRecords != null)
				for (DiffRecord diffRecord : diffFileRecords) {
					if (diffRecord.getFilename().equals(filename))
						history.add(diffRecord);
				}
		}
		return history;
	}
}
