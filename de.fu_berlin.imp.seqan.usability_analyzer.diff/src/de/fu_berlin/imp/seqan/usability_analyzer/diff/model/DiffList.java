package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.bkahlert.devel.nebula.utils.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DiffList implements Iterable<IDiff>, HasDateRange {

	private IDiff[] diffs;
	private TimeZoneDateRange dateRange = null;
	private String longestPrefix;

	public DiffList(IDiff[] diffs) {
		if (diffs == null)
			throw new IllegalArgumentException();
		this.diffs = diffs;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		if (this.dateRange == null) {
			List<TimeZoneDateRange> dateRanges = new ArrayList<TimeZoneDateRange>();
			for (IDiff diff : this) {
				dateRanges.add(diff.getDateRange());
			}
			this.dateRange = TimeZoneDateRange
					.calculateOuterDateRange(dateRanges
							.toArray(new TimeZoneDateRange[0]));
		}
		return this.dateRange;
	}

	/**
	 * Returns the longest common prefix among the files described by the
	 * {@link DiffRecord} in this {@link DiffList}.
	 * <p>
	 * e.g. considering there are the files /source/a.txt, /source/b.txt,
	 * /src/c.txt this method would return /source/.
	 * 
	 * @return
	 */
	public String getLongestCommonPrefix() {
		if (this.longestPrefix == null) {
			List<DiffRecord> diffRecords = new ArrayList<DiffRecord>();
			for (IDiff diff : this)
				diffRecords.addAll(diff.getDiffFileRecords());
			Map<String, Integer> rs = StringUtils.getLongestCommonPrefix(
					new StringUtils.IStringAdapter<DiffRecord>() {
						@Override
						public String getString(DiffRecord diffRecord) {
							return diffRecord.getFilename();
						}
					}, "C:\\".length(), diffRecords.toArray(new DiffRecord[0]));
			if (rs.values().size() == 0)
				return null;
			List<Integer> occurrences = new ArrayList<Integer>(rs.values());
			Collections.sort(occurrences);
			Integer maxOccurrences = occurrences.get(occurrences.size() - 1);
			for (String prefix : rs.keySet()) {
				if (rs.get(prefix).equals(maxOccurrences))
					return prefix;
			}
			throw new RuntimeException("Program logic error");
		}
		return this.longestPrefix;
	}

	private int getIndex(IDiff diff) {
		int i = -1;
		for (i = 0; i < this.diffs.length; i++)
			if (this.diffs[i].equals(diff))
				break;
		return i;
	}

	public IDiff getSuccessor(IDiff diff) {
		int i = getIndex(diff);

		if (i >= this.diffs.length - 1)
			return null;

		if (i + 1 < this.diffs.length)
			return this.diffs[i + 1];
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

	@Override
	public Iterator<IDiff> iterator() {
		return new Iterator<IDiff>() {
			private int pos = 0;

			public boolean hasNext() {
				return pos < diffs.length;
			}

			public IDiff next() throws NoSuchElementException {
				if (hasNext())
					return diffs[pos++];
				else
					throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Returns the i-th {@link IDiff} in this {@link DiffList}.
	 * <p>
	 * Throws an {@link IndexOutOfBoundsException} exception if i access an
	 * undefined position.
	 * 
	 * @param i
	 * @return
	 */
	public IDiff get(int i) {
		return this.diffs[i];
	}

	/**
	 * Returns the number of {@link IDiff}s in this {@link DiffList}.
	 * 
	 * @return
	 */
	public int length() {
		return this.diffs.length;
	}

	/**
	 * Returns a {@link IDiff} array representing the {@link IDiff}s of this
	 * {@link DiffList}.
	 * 
	 * @return
	 */
	public IDiff[] toArray() {
		return this.diffs;
	}

}
