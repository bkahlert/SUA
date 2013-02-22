package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;

public class DiffRecordList extends ArrayList<DiffRecord> {

	/**
	 * Creates a new {@link DiffRecordList} instance.
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
		List<IDiff> diffs = new ArrayList<IDiff>();

		IDiff prevDiffFile = null;

		progressMonitor.beginTask("Processing " + Diff.class.getSimpleName()
				+ "s", dataList.size());

		for (IData data : dataList) { // look ahead = 1
			Diff diff = new Diff(data, prevDiffFile, trunk, sourceCache,
					new SubProgressMonitor(progressMonitor, 1));
			diffs.add(diff);

			prevDiffFile = diff;
		}

		// clean up since a Diffs creation can temporally consume much
		// heap
		Runtime.getRuntime().gc();

		progressMonitor.done();

		return new Diffs(diffs.toArray(new IDiff[0]));
	}

	private static final long serialVersionUID = 1327362495545624312L;
	private Diff diff;
	private ITrunk trunk;
	private ISourceStore sourceCache;

	public DiffRecordList(Diff diff, ITrunk trunk, ISourceStore sourceCache) {
		Assert.isNotNull(diff);
		Assert.isNotNull(trunk);
		Assert.isNotNull(sourceCache);
		this.diff = diff;
		this.trunk = trunk;
		this.sourceCache = sourceCache;
	}

	/**
	 * Creates a {@link DiffRecord} and add it to this {@link DiffRecordList}
	 * 
	 * @param commandLine
	 * @param metaOldLine
	 * @param metaNewLine
	 * @param contentStart
	 * @param contentEnd
	 */
	public DiffRecord createAndAddRecord(String commandLine,
			String metaOldLine, String metaNewLine, long contentStart,
			long contentEnd) {
		DiffRecordMeta meta = new DiffRecordMeta(metaOldLine, metaNewLine);

		IData originalSourceFile = trunk.getSourceFile(meta.getToFileName());
		DiffRecord diffRecord = new DiffRecord(this.diff, originalSourceFile,
				sourceCache, commandLine, meta, contentStart, contentEnd);
		if (!diffRecord.isTemporary()) {
			this.add(diffRecord);
			return diffRecord;
		} else {
			return null;
		}
	}
}
