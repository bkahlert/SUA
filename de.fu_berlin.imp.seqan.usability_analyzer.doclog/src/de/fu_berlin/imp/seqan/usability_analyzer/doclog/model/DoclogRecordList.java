package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DoclogRecordList extends ArrayList<DoclogRecord> implements
		HasDateRange {

	private static final long serialVersionUID = -7428136109215033396L;

	private int getDoclogRecordIndex(DoclogRecord doclogRecord) {
		int i = -1;
		for (i = 0; i < this.size(); i++)
			if (this.get(i).equals(doclogRecord))
				break;
		return i;
	}

	/**
	 * Returns the {@link DoclogRecord} that represents the immediate preceding
	 * action.
	 * 
	 * @param doclogRecord
	 * @return
	 */
	@SuppressWarnings("unused")
	public DoclogRecord getPredecessor(DoclogRecord doclogRecord) {
		int i = getDoclogRecordIndex(doclogRecord);

		if (i == 0)
			return null;

		for (int j = i - 1; j >= 0; j--) {
			DoclogRecord predecessor = this.get(j);
			return predecessor;
		}

		return null;
	}

	/**
	 * Returns the {@link DoclogRecord} that represents the immediate succeeding
	 * action.
	 * 
	 * @param doclogRecord
	 * @return
	 */
	@SuppressWarnings("unused")
	public DoclogRecord getSuccessor(DoclogRecord doclogRecord) {
		int i = getDoclogRecordIndex(doclogRecord);

		if (i == this.size() - 1)
			return null;

		for (int j = i + 1; j < this.size(); j++) {
			DoclogRecord successor = this.get(j);
			return successor;
		}

		return null;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		List<TimeZoneDateRange> dateRanges = new ArrayList<TimeZoneDateRange>();
		for (DoclogRecord doclogRecord : this) {
			dateRanges.add(doclogRecord.getDateRange());
		}
		return TimeZoneDateRange.calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

}
