package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FileList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;

public class DiffFileDirectory extends File {

	private static final long serialVersionUID = -1044615563054968853L;
	private static final Logger LOGGER = Logger
			.getLogger(DiffFileDirectory.class);

	private static Map<ID, FileList> readDiffFilesMapping(File directory) {
		Map<ID, FileList> rawFiles = new HashMap<ID, FileList>();
		for (File diffFile : directory
				.listFiles((FileFilter) new RegexFileFilter(DiffFile.PATTERN))) {
			ID id = DiffFile.getId(diffFile);
			if (!rawFiles.containsKey(id))
				rawFiles.put(id, new FileList());
			rawFiles.get(id).add(diffFile);
		}
		return rawFiles;
	}

	private static void sortDiffFiles(FileList diffFiles) {
		Collections.sort(diffFiles, new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				TimeZoneDate date1 = DiffFile.getDate(file1);
				TimeZoneDate date2 = DiffFile.getDate(file2);
				return date1.compareTo(date2);
			}
		});
	}

	private static TimeZoneDateRange calculateDateRange(FileList fileList) {
		TimeZoneDate start = null;
		TimeZoneDate end = null;
		if (fileList.size() > 0) {
			start = DiffFile.getDate(fileList.get(0));
			end = DiffFile.getDate(fileList.get(fileList.size() - 1));
		}
		return new TimeZoneDateRange(start, end);
	}

	private SourceOrigin sourceOrigin;
	private SourceCache sourceCache;
	private Map<ID, FileList> fileLists;
	private Map<ID, TimeZoneDateRange> fileDateRanges;

	/**
	 * Returns a {@link DiffFileDirectory} instance that can handle contained
	 * {@link DiffFile}s
	 * 
	 * @param dataDirectory
	 *            containing {@link DiffFile}s
	 * @param originalSourcesDirectory
	 *            containing the original source files
	 * @param cachedSourcesDirectory
	 *            that can be used to cache patched {@link DiffFile}s
	 */
	public DiffFileDirectory(File dataDirectory, File originalSourcesDirectory,
			File cachedSourcesDirectory) {
		super(dataDirectory.getAbsolutePath());

		Assert.isNotNull(originalSourcesDirectory);
		Assert.isNotNull(dataDirectory);
		this.sourceOrigin = new SourceOrigin(originalSourcesDirectory);
		this.sourceCache = new SourceCache(cachedSourcesDirectory);

		long start = System.currentTimeMillis();
		this.fileLists = readDiffFilesMapping(this);
		this.fileDateRanges = new HashMap<ID, TimeZoneDateRange>(
				this.fileLists.size());
		// ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (final ID id : this.fileLists.keySet()) {
			final FileList fileList = this.fileLists.get(id);
			//
			// executorService.execute(new Runnable() {
			// @Override
			// public void run() {
			sortDiffFiles(fileList);
			fileDateRanges.put(id, calculateDateRange(fileList));
			// }
			// });
		}
		// try {
		// executorService.awaitTermination(30, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// LOGGER.fatal(
		// "Could not complete "
		// + DiffFileDirectory.class.getSimpleName() + " scan",
		// e);
		// }
		long end = System.currentTimeMillis();
		LOGGER.info(DiffFileDirectory.class.getSimpleName() + " "
				+ dataDirectory.getName() + " scanned within " + (end - start)
				+ "ms.");
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link DiffFile}s.
	 * 
	 * @return
	 */
	public Set<ID> getIDs() {
		return this.fileLists.keySet();
	}

	/**
	 * Return the {@link TimeZoneDateRange} determined by the earliest and
	 * latest {@link DiffFile}
	 * 
	 * @param id
	 * @return
	 */
	public TimeZoneDateRange getDateRange(ID id) {
		return this.fileDateRanges.get(id);
	}

	/**
	 * Returns all {@link DiffFile}s associated with a given {@link ID}.
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 */
	public DiffFileList getDiffFiles(ID id, IProgressMonitor progressMonitor) {
		DiffFileList diffFiles = DiffFileRecordList.create(
				this.fileLists.get(id), this.sourceOrigin, this.sourceCache,
				progressMonitor);
		return diffFiles;
	}
}
