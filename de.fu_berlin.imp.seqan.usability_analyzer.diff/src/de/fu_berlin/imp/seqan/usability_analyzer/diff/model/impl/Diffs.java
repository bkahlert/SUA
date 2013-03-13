package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.bkahlert.devel.nebula.utils.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;

public class Diffs implements IDiffs {

	private IDiff[] diffs;
	private IIdentifier identifier;
	private TimeZoneDateRange dateRange = null;
	private String longestPrefix;

	public Diffs(IDiff[] diffs) {
		if (diffs == null) {
			throw new IllegalArgumentException();
		}
		if (diffs.length == 0) {
			throw new IllegalArgumentException("You must provide at least one "
					+ IDiff.class.getSimpleName());
		}
		this.identifier = diffs[0].getIdentifier();
		for (int i = 1; i < diffs.length; i++) {
			if (!this.identifier.equals(diffs[i].getIdentifier())) {
				throw new IllegalArgumentException("All "
						+ IDiff.class.getSimpleName()
						+ " must provide the same "
						+ IIdentifier.class.getSimpleName() + ". "
						+ diffs[i].getIdentifier() + " does not equal "
						+ this.identifier + ".");
			}
		}
		this.diffs = diffs;
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#getDateRange
	 * ()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#
	 * getLongestCommonPrefix()
	 */
	@Override
	public String getLongestCommonPrefix() {
		if (this.longestPrefix == null) {
			List<IDiffRecord> diffRecords = new ArrayList<IDiffRecord>();
			for (IDiff diff : this) {
				for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
					diffRecords.add(diffRecord);
				}
			}
			Map<String, Integer> rs = StringUtils.getLongestCommonPrefix(
					new StringUtils.IStringAdapter<IDiffRecord>() {
						@Override
						public String getString(IDiffRecord diffRecord) {
							return diffRecord.getFilename();
						}
					}, "C:\\".length(), diffRecords.toArray(new DiffRecord[0]));
			if (rs.values().size() == 0) {
				return null;
			}
			List<Integer> occurrences = new ArrayList<Integer>(rs.values());
			Collections.sort(occurrences);
			Integer maxOccurrences = occurrences.get(occurrences.size() - 1);
			for (String prefix : rs.keySet()) {
				if (rs.get(prefix).equals(maxOccurrences)) {
					return prefix;
				}
			}
			throw new RuntimeException("Program logic error");
		}
		return this.longestPrefix;
	}

	private int getIndex(IDiff diff) {
		int i = -1;
		for (i = 0; i < this.diffs.length; i++) {
			if (this.diffs[i].equals(diff)) {
				break;
			}
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#getSuccessor
	 * (de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff)
	 */
	@Override
	public IDiff getSuccessor(IDiff diff) {
		int i = this.getIndex(diff);

		if (i >= this.diffs.length - 1) {
			return null;
		}

		if (i + 1 < this.diffs.length) {
			return this.diffs[i + 1];
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#getHistory
	 * (java.lang.String)
	 */
	@Override
	public DiffRecordHistory getHistory(String filename) {
		DiffRecordHistory history = new DiffRecordHistory();
		for (IDiff diff : this) {
			IDiffRecords diffFileRecords = diff.getDiffFileRecords();
			if (diffFileRecords != null) {
				for (IDiffRecord diffRecord : diffFileRecords) {
					if (diffRecord.getFilename().equals(filename)) {
						history.add(diffRecord);
					}
				}
			}
		}
		return history;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#iterator()
	 */
	@Override
	public Iterator<IDiff> iterator() {
		return new Iterator<IDiff>() {
			private int pos = 0;

			@Override
			public boolean hasNext() {
				return this.pos < Diffs.this.diffs.length;
			}

			@Override
			public IDiff next() throws NoSuchElementException {
				if (this.hasNext()) {
					return Diffs.this.diffs[this.pos++];
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#get(int)
	 */
	@Override
	public IDiff get(int i) {
		return this.diffs[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#length()
	 */
	@Override
	public int length() {
		return this.diffs.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs#toArray()
	 */
	@Override
	public IDiff[] toArray() {
		return this.diffs;
	}

}
