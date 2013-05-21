package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordMeta;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;

public class DiffRecords implements IDiffRecords {

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
		SubMonitor monitor = SubMonitor.convert(progressMonitor,
				dataList.size());

		List<IDiff> diffs = new ArrayList<IDiff>();

		IDiff prevDiffFile = null;

		for (IData data : dataList) { // look ahead = 1
			Diff diff = new Diff(data, prevDiffFile, trunk, sourceCache,
					monitor.newChild(1));
			diffs.add(diff);

			prevDiffFile = diff;
		}

		// clean up since a Diffs creation can temporally consume much
		// heap
		Runtime.getRuntime().gc();

		monitor.done();

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
	public IDiffRecord createAndAddRecord(String commandLine,
			String metaOldLine, String metaNewLine, long contentStart,
			long contentEnd) {
		IDiffRecordMeta meta = new DiffRecordMeta(metaOldLine, metaNewLine);

		IData originalSourceFile = this.trunk.getSourceFile(meta
				.getToFileName());
		IDiffRecord diffRecord = new DiffRecord(this.diff, originalSourceFile,
				this.sourceCache, commandLine, meta, contentStart, contentEnd);
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
