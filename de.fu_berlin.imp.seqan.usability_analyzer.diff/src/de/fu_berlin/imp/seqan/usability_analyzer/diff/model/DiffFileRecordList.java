package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataResourceList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;

public class DiffFileRecordList extends ArrayList<DiffRecord> {

	/**
	 * Creates a new {@link DiffFileRecordList} instance.
	 * 
	 * @param dataResources
	 *            that can be treated as {@link DiffDataResource}s<br>
	 *            e.g. [ "/some/dir/data/file.v1.diff",
	 *            "/some/dir/data/file.v2.diff" ]
	 * @param sourceOrigin
	 *            directory that contains the original source files<br>
	 *            e.g. /some/dir/trunk
	 * @param sourceCache
	 *            directory that contains already patched {@link DiffRecord}
	 *            files<br>
	 *            e.g. /some/dir/patches_files
	 * @param progressMonitor
	 * @return
	 */
	public static DiffFileList create(DataResourceList dataResources,
			SourceOrigin sourceOrigin, SourceCache sourceCache,
			IProgressMonitor progressMonitor) {
		DiffFileList diffFiles = new DiffFileList();

		DiffDataResource prevDiffFile = null;

		progressMonitor.beginTask(
				"Processing " + DiffDataResource.class.getSimpleName() + "s",
				dataResources.size());

		for (IData data : dataResources) { // look ahead = 1
			ID id = DiffDataResource.getId(data);
			String revision = DiffDataResource.getRevision(data);
			TimeZoneDate prevDate = prevDiffFile != null ? prevDiffFile
					.getDateRange().getEndDate() : null;
			TimeZoneDateRange dateRange = new TimeZoneDateRange(prevDate,
					DiffDataResource.getDate(data));

			DiffDataResource diffDataResource = new DiffDataResource(data,
					prevDiffFile, id, revision, dateRange, sourceOrigin,
					sourceCache, new SubProgressMonitor(progressMonitor, 1));
			diffFiles.add(diffDataResource);

			prevDiffFile = diffDataResource;
		}

		// clean up since a DiffFileList creation can temporally consume much
		// heap
		Runtime.getRuntime().gc();

		progressMonitor.done();

		return diffFiles;
	}

	private static final long serialVersionUID = 1327362495545624312L;
	private DiffDataResource diffDataResource;
	private SourceOrigin sourceOrigin;
	private SourceCache sourceCache;

	public DiffFileRecordList(DiffDataResource diffDataResource,
			SourceOrigin sourceOrigin, SourceCache sourceCache) {
		Assert.isNotNull(diffDataResource);
		Assert.isNotNull(sourceOrigin);
		Assert.isNotNull(sourceCache);
		this.diffDataResource = diffDataResource;
		this.sourceOrigin = sourceOrigin;
		this.sourceCache = sourceCache;
	}

	/**
	 * Creates a {@link DiffRecord} and add it to this
	 * {@link DiffFileRecordList}
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
		DiffFileRecordMeta meta = new DiffFileRecordMeta(metaOldLine,
				metaNewLine);

		IData originalSourceFile = sourceOrigin.getOriginSourceFile(meta
				.getToFileName());
		DiffRecord diffRecord = new DiffRecord(this.diffDataResource,
				originalSourceFile, sourceCache, commandLine, meta,
				contentStart, contentEnd);
		if (!diffRecord.isTemporary()) {
			this.add(diffRecord);
			return diffRecord;
		} else {
			return null;
		}
	}
}
