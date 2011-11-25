package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FileList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;

public class DiffFileRecordList extends ArrayList<DiffFileRecord> {

	/**
	 * Creates a new {@link DiffFileRecordList} instance.
	 * 
	 * @param files
	 *            that can be treated as {@link DiffFile}s<br>
	 *            e.g. [ "/some/dir/data/file.v1.diff",
	 *            "/some/dir/data/file.v2.diff" ]
	 * @param sourceOrigin
	 *            directory that contains the original source files<br>
	 *            e.g. /some/dir/trunk
	 * @param sourceCache
	 *            directory that contains already patched {@link DiffFileRecord}
	 *            files<br>
	 *            e.g. /some/dir/patches_files
	 * @param progressMonitor
	 * @return
	 */
	public static DiffFileList create(FileList files,
			SourceOrigin sourceOrigin, SourceCache sourceCache,
			IProgressMonitor progressMonitor) {
		DiffFileList diffFiles = new DiffFileList();

		DiffFile prevDiffFile = null;
		File file = null;
		TimeZoneDate date = null;

		progressMonitor.beginTask("Parsing " + DiffFile.class.getSimpleName()
				+ "s", files.size());

		for (File nextFile : files) { // look ahead = 1
			TimeZoneDate nextDate = DiffFile.getDate(nextFile);

			if (file != null) {
				ID id = DiffFile.getId(file);
				String revision = DiffFile.getRevision(file);
				TimeZoneDateRange dateRange = new TimeZoneDateRange(date,
						nextDate);
				DiffFile diffFile = new DiffFile(file, prevDiffFile, id,
						revision, dateRange, sourceOrigin, sourceCache,
						new SubProgressMonitor(progressMonitor, 1));
				diffFiles.add(diffFile);

				prevDiffFile = diffFile;
			}

			file = nextFile;
			date = DiffFile.getDate(nextFile);
		}

		// create last file that was ignored due to look ahead
		if (file != null) {
			TimeZoneDate nextDate = null;

			ID id = DiffFile.getId(file);
			String revision = DiffFile.getRevision(file);
			TimeZoneDateRange dateRange = new TimeZoneDateRange(date, nextDate);
			DiffFile diffFile = new DiffFile(file, prevDiffFile, id, revision,
					dateRange, sourceOrigin, sourceCache,
					new SubProgressMonitor(progressMonitor, 1));
			diffFiles.add(diffFile);
		}

		progressMonitor.done();

		return diffFiles;
	}

	private static final long serialVersionUID = 1327362495545624312L;
	private DiffFile diffFile;
	private SourceOrigin sourceOrigin;
	private SourceCache sourceCache;

	public DiffFileRecordList(DiffFile diffFile, SourceOrigin sourceOrigin,
			SourceCache sourceCache) {
		Assert.isNotNull(diffFile);
		Assert.isNotNull(sourceOrigin);
		Assert.isNotNull(sourceCache);
		this.diffFile = diffFile;
		this.sourceOrigin = sourceOrigin;
		this.sourceCache = sourceCache;
	}

	/**
	 * Creates a {@link DiffFileRecord} and add it to this
	 * {@link DiffFileRecordList}
	 * 
	 * @param commandLine
	 * @param content
	 */
	public DiffFileRecord createAndAddRecord(String commandLine,
			ArrayList<String> content) {
		DiffFileRecordMeta meta = new DiffFileRecordMeta(content.get(0),
				content.get(1));

		File originalSourceFile = sourceOrigin.getOriginSourceFile(meta
				.getToFileName());
		File cachedSourceFile = sourceCache.getCachedSourceFile(this.diffFile,
				meta.getToFileName());
		DiffFileRecord diffFileRecord = new DiffFileRecord(this.diffFile,
				originalSourceFile, cachedSourceFile, commandLine, meta,
				content);
		if (!diffFileRecord.isTemporary()) {
			this.add(diffFileRecord);
			return diffFileRecord;
		} else {
			return null;
		}
	}
}
