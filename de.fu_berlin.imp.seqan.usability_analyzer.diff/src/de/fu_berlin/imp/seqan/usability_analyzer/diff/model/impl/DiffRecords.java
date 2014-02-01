package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord.FLAGS;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordMeta;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;

public class DiffRecords implements IDiffRecords {

	private static final Logger LOGGER = Logger.getLogger(DiffRecords.class);

	/**
	 * Creates a new {@link DiffRecords} instance.
	 * 
	 * @param dataList
	 *            that can be treated as {@link Diff}s<br>
	 *            e.g. [ "/some/dir/data/file.v1.diff",
	 *            "/some/dir/data/file.v2.diff" ]
	 * @param trunk
	 *            directory that contains the original source files<br>
	 *            e.g. /some/dir/trunk
	 * @param sourceCache
	 *            directory that contains already patched {@link DiffRecord}
	 *            files<br>
	 *            e.g. /some/dir/patches_files
	 * @param progressMonitor
	 * @return
	 */
	public static IDiffs create(DataList dataList, ITrunk trunk,
			ISourceStore sourceCache, IProgressMonitor progressMonitor) {
		if (dataList == null) {
			System.err.println("ERROR");
		}
		SubMonitor monitor = SubMonitor.convert(progressMonitor,
				dataList.size());

		Map<String, ArrayList<IDiff>> clusteredDiffs = new HashMap<String, ArrayList<IDiff>>();

		for (IData data : dataList) {
			String locationHash = DiffDataUtils.getLocationHash(data);
			if (!clusteredDiffs.containsKey(locationHash)) {
				clusteredDiffs.put(locationHash, new ArrayList<IDiff>());
			}

			List<IDiff> cluster = clusteredDiffs.get(locationHash);
			IDiff prevDiff = cluster.size() > 0 ? cluster
					.get(cluster.size() - 1) : null;

			IDiff diff = new Diff(data, prevDiff, trunk, sourceCache,
					monitor.newChild(1));
			cluster.add(diff);
		}

		// clean up since a Diffs creation can temporally consume much
		// heap
		Runtime.getRuntime().gc();

		monitor.done();

		List<IDiff> diffs = new ArrayList<IDiff>();
		for (List<IDiff> cluster : clusteredDiffs.values()) {
			diffs.addAll(cluster);
		}
		return new Diffs(diffs.toArray(new IDiff[0]));
	}

	private IDiff diff;
	private ArrayList<IDiffRecord> diffRecords;
	private ITrunk trunk;
	private ISourceStore sourceCache;

	public DiffRecords(IDiff diff, ITrunk trunk, ISourceStore sourceCache) {
		Assert.isNotNull(diff);
		Assert.isNotNull(trunk);
		Assert.isNotNull(sourceCache);
		this.diff = diff;
		this.diffRecords = new ArrayList<IDiffRecord>();
		this.trunk = trunk;
		this.sourceCache = sourceCache;
	}

	@Override
	public IDiffRecord createAndAddRecord(String filename, TimeZoneDate date) {
		IData originalSourceFile = this.trunk.getSourceFile(filename);

		TimeZoneDate prevDate = null;
		IDiff pastDiff = this.diff.getPrevDiffFile();
		outer: while (pastDiff != null) {
			// if
			// (this.diff.getLocationHash().equals(pastDiff.getLocationHash()))
			// {
			for (IDiffRecord diffRecord : pastDiff.getDiffFileRecords()) {
				if (diffRecord.getFilename().equals(filename)) {
					prevDate = diffRecord.getDateRange() != null ? diffRecord
							.getDateRange().getEndDate() : null;
					break outer;
				}
			}
			// }
			pastDiff = pastDiff.getPrevDiffFile();
		}

		boolean fileRestored = false;
		if (prevDate != null && date.compareTo(prevDate) < 0) {
			TimeZoneDate correctedDate = DiffDataUtils.getDate(this.diff, null);
			LOGGER.warn("Modification date "
					+ date.toISO8601()
					+ " of "
					+ filename
					+ " is in the predecessor file's past ("
					+ prevDate
					+ ")\n"
					+ "- This can be to a previously backuped file that has been restored.\n- Taking the whole revisions creation date ("
					+ correctedDate.toISO8601()
					+ ") as this file's modification date.");
			date = correctedDate;
			fileRestored = true;
		}
		TimeZoneDateRange dateRange = new TimeZoneDateRange(prevDate, date);

		List<FLAGS> flags = new LinkedList<FLAGS>();
		if (fileRestored) {
			flags.add(FLAGS.RESTORED);
		}

		IDiffRecord diffRecord = new DiffRecord(this.diff, originalSourceFile,
				this.sourceCache, filename, dateRange, flags);
		if (!diffRecord.isTemporary()) {
			this.diffRecords.add(diffRecord);
			return diffRecord;
		} else {
			return null;
		}
	}

	@Override
	public IDiffRecord createAndAddRecord(String commandLine,
			String metaOldLine, String metaNewLine, long contentStart,
			long contentEnd) {
		IDiffRecordMeta meta = new DiffRecordMeta(metaOldLine, metaNewLine);

		IData originalSourceFile = this.trunk.getSourceFile(meta
				.getToFileName());
		IDiffRecord diffRecord = new DiffRecord(this.diff, originalSourceFile,
				this.sourceCache, commandLine, meta, contentStart, contentEnd,
				null);
		if (!diffRecord.isTemporary()) {
			this.diffRecords.add(diffRecord);
			return diffRecord;
		} else {
			return null;
		}
	}

	@Override
	public Iterator<IDiffRecord> iterator() {
		return new Iterator<IDiffRecord>() {
			private int pos = 0;

			@Override
			public boolean hasNext() {
				return this.pos < DiffRecords.this.diffRecords.size();
			}

			@Override
			public IDiffRecord next() throws NoSuchElementException {
				if (this.hasNext()) {
					return DiffRecords.this.diffRecords.get(this.pos++);
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

	@Override
	public IDiffRecord get(int i) {
		return this.diffRecords.get(i);
	}

	@Override
	public int size() {
		return this.diffRecords.size();
	}

	@Override
	public Object[] toArray() {
		return this.diffRecords.toArray();
	}
}
