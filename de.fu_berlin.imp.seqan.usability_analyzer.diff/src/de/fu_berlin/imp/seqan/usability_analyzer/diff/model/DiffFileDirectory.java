package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FileList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.CachingDiffFileComparator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;

public class DiffFileDirectory extends File {

	private static final long serialVersionUID = -1044615563054968853L;
	private static final Logger LOGGER = Logger
			.getLogger(DiffFileDirectory.class);

	public static final int DIFF_CACHE_SIZE = 5;

	/**
	 * Scans through the given directory, looks for sub directories with valid
	 * names (see {@link ID#isValid(String)}) and maps all containing files its
	 * corresponding {@link ID}.
	 * 
	 * @param directory
	 * @return
	 */
	private static Map<ID, FileList> readDiffFilesMapping(File directory) {
		Map<ID, FileList> rawFiles = new HashMap<ID, FileList>();
		for (File diffFileDir : directory.listFiles()) {
			if (!diffFileDir.isDirectory())
				continue;
			if (!ID.isValid(diffFileDir.getName())) {
				LOGGER.warn("Directory with invalid "
						+ ID.class.getSimpleName() + " name detected: "
						+ diffFileDir.toString());
				continue;
			}

			for (File diffFile : diffFileDir
					.listFiles((FileFilter) new RegexFileFilter(
							DiffFile.PATTERN))) {
				ID id = DiffFile.getId(diffFile);
				if (!rawFiles.containsKey(id))
					rawFiles.put(id, new FileList());
				rawFiles.get(id).add(diffFile);
			}
		}
		return rawFiles;
	}

	private static void sortDiffFiles(FileList diffFiles,
			Comparator<File> fileComparator) {
		Collections.sort(diffFiles, fileComparator);
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

	private DiffCache diffCache;

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

		this.diffCache = new DiffCache(this, DIFF_CACHE_SIZE);
	}

	public void scan() {
		long start = System.currentTimeMillis();
		this.fileLists = readDiffFilesMapping(this);
		this.fileDateRanges = new HashMap<ID, TimeZoneDateRange>(
				this.fileLists.size());

		ExecutorService executorService = ExecutorUtil
				.newFixedMultipleOfProcessorsThreadPool(2);
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
		for (final ID id : this.fileLists.keySet()) {
			final FileList fileList = this.fileLists.get(id);
			final CachingDiffFileComparator cachingDiffFileComparator = new CachingDiffFileComparator();
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						sortDiffFiles(fileList, cachingDiffFileComparator);
						TimeZoneDateRange dateRange = calculateDateRange(fileList);
						synchronized (fileDateRanges) {
							fileDateRanges.put(id, dateRange);
						}
					} catch (Exception e) {
						LOGGER.fatal(e);
					}
					return null;
				}
			});
		}
		try {
			executorService.invokeAll(callables);
		} catch (InterruptedException e) {
			LOGGER.fatal(
					"Error parsing " + DiffFileDirectory.class.getSimpleName(),
					e);
		}
		LOGGER.info(DiffFileDirectory.class.getSimpleName() + " "
				+ this.getName() + " scanned within "
				+ (System.currentTimeMillis() - start) + "ms.");
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link DiffFile}s.
	 * 
	 * @return
	 */
	public Set<ID> getIDs() {
		scanIfNecessary();
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
		scanIfNecessary();
		return this.fileDateRanges.get(id);
	}

	/**
	 * Returns all {@link DiffFile}s associated with a given {@link ID}.
	 * <p>
	 * If the {@link DiffFileList} is already in the cached the cached version
	 * is returned. Otherwise a new {@link DiffFileList} is constructed and
	 * added to the cache.
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 * 
	 * @see DiffCache
	 */
	public DiffFileList getDiffFiles(ID id, IProgressMonitor progressMonitor) {
		return diffCache.getPayload(id, progressMonitor);
	}

	/**
	 * Returns all {@link DiffFile}s associated with a given {@link ID}.
	 * <p>
	 * In contrast to {@link #getDiffFiles(ID, IProgressMonitor)} this method
	 * always creates the objects anew and does not use any caching
	 * functionality.
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 */
	public DiffFileList createDiffFiles(ID id, IProgressMonitor progressMonitor) {
		scanIfNecessary();
		DiffFileList diffFiles = DiffFileRecordList.create(
				this.fileLists.get(id), this.sourceOrigin, this.sourceCache,
				progressMonitor);
		return diffFiles;
	}

	public void scanIfNecessary() {
		if (this.fileLists == null && this.fileDateRanges == null) {
			scan();
		} else if (this.fileLists == null && this.fileDateRanges != null) {
			LOGGER.fatal("State error in "
					+ DiffFileDirectory.class.getSimpleName() + "");
		} else if (this.fileLists != null && this.fileDateRanges == null) {
			LOGGER.fatal("State error in "
					+ DiffFileDirectory.class.getSimpleName() + "");
		} else {
			// nothing to do
		}
	}
}
