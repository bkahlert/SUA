package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DiffFileList extends ArrayList<DiffFile> implements HasDateRange {

	private static final long serialVersionUID = 1327362495545624012L;

	@Override
	public LocalDateRange getDateRange() {
		List<LocalDateRange> dateRanges = new ArrayList<LocalDateRange>();
		for (DiffFile diffFile : this) {
			dateRanges.add(diffFile.getDateRange());
		}
		return LocalDateRange.calculateOuterDateRange(dateRanges
				.toArray(new LocalDateRange[0]));
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

	/**
	 * Returns all {@link DiffFileRecord}'s describing the same file.
	 * 
	 * @param filename
	 * @return
	 */
	public DiffFileRecordHistory getHistory(String filename) {
		DiffFileRecordHistory history = new DiffFileRecordHistory();
		for (DiffFile diffFile : this) {
			DiffFileRecordList diffFileRecords = diffFile.getDiffFileRecords();
			if (diffFileRecords != null)
				for (DiffFileRecord diffFileRecord : diffFileRecords) {
					if (diffFileRecord.getFilename().equals(filename))
						history.add(diffFileRecord);
				}
		}
		return history;
	}
}
