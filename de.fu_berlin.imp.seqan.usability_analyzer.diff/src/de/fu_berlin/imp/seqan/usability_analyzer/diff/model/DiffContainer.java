package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataResourceList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.AggregatedBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil.ParametrizedCallable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.CachingDiffFileComparator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.Trunk;

public class DiffContainer extends AggregatedBaseDataContainer {

	private static final Logger LOGGER = Logger
			.getLogger(DiffContainer.class);

	public static final int DIFF_CACHE_SIZE = 5;

	private static final ExecutorService LOADER_POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

	/**
	 * Scans through the given directory, looks for sub directories with valid
	 * names (see {@link ID#isValid(String)}) and maps all containing files its
	 * corresponding {@link ID}.
	 * 
	 * @param diffContainer
	 * @return
	 */
	private static Map<ID, DataResourceList> readDiffFilesMapping(
			DiffContainer diffContainer) {
		Map<ID, DataResourceList> rawFiles = new HashMap<ID, DataResourceList>();
		for (IDataContainer diffFileDir : diffContainer
				.getDiffFileDirectory().getSubContainers()) {
			if (!ID.isValid(diffFileDir.getName())) {
				LOGGER.warn("Directory with invalid "
						+ ID.class.getSimpleName() + " name detected: "
						+ diffFileDir.toString());
				continue;
			}

			for (IData diffFile : diffFileDir.getResources()) {
				if (!Diff.PATTERN.matcher(diffFile.getName())
						.matches())
					continue;
				ID id = DiffDataUtils.getId(diffFile);
				if (!rawFiles.containsKey(id))
					rawFiles.put(id, new DataResourceList());
				rawFiles.get(id).add(diffFile);
			}
		}
		return rawFiles;
	}

	private static void sortDiffFiles(DataResourceList diffFiles,
			Comparator<IData> fileComparator) {
		Collections.sort(diffFiles, fileComparator);
	}

	private static TimeZoneDateRange calculateDateRange(
			DataResourceList dataResourceList) {
		TimeZoneDate start = null;
		TimeZoneDate end = null;
		if (dataResourceList.size() > 0) {
			start = DiffDataUtils.getDate(dataResourceList.get(0));
			end = DiffDataUtils.getDate(dataResourceList
					.get(dataResourceList.size() - 1));
		}
		return new TimeZoneDateRange(start, end);
	}

	private IDataContainer diffContainer;
	private ITrunk trunk;
	private ISourceStore sourceCache;
	private Map<ID, DataResourceList> dataResourceLists;
	private Map<ID, TimeZoneDateRange> fileDateRanges;

	private DiffCache diffCache;

	/**
	 * Returns a {@link DiffContainer} instance that can handle contained
	 * {@link Diff}s
	 * 
	 * @param baseDataContainers
	 *            containing {@link Diff}s
	 * @param originalSourcesDirectory
	 *            containing the original source files
	 * @param cachedSourcesDirectory
	 *            that can be used to cache patched {@link Diff}s
	 */
	public DiffContainer(
			List<? extends IBaseDataContainer> baseDataContainers) {
		super(baseDataContainers);
		this.diffContainer = this.getSubContainer("diff");
		this.trunk = new Trunk(this.getSubContainer("trunk"));
		this.sourceCache = new SourceCache(this);

		this.diffCache = new DiffCache(this, DIFF_CACHE_SIZE);
	}

	public DiffContainer(IBaseDataContainer baseDataContainer) {
		this(Arrays.asList(baseDataContainer));
	}

	public void scan(SubMonitor monitor) {
		monitor = SubMonitor.convert(monitor);
		this.dataResourceLists = readDiffFilesMapping(this);
		this.fileDateRanges = new HashMap<ID, TimeZoneDateRange>(
				this.dataResourceLists.size());

		long size = 0;
		final HashMap<ID, Long> sizes = new HashMap<ID, Long>();
		for (ID id : this.dataResourceLists.keySet()) {
			long fileListSize = 0;
			for (IData file : this.dataResourceLists.get(id))
				fileListSize += file.getLength();
			sizes.put(id, fileListSize);
			size += fileListSize;
		}

		monitor.beginTask("Loading " + this, (int) (size / 1000l));
		List<Future<Integer>> futures = ExecutorUtil.nonUIAsyncExec(
				LOADER_POOL, this.dataResourceLists.keySet(),
				new ParametrizedCallable<ID, Integer>() {
					@Override
					public Integer call(ID id) throws Exception {
						final DataResourceList dataResourceList = dataResourceLists
								.get(id);

						final CachingDiffFileComparator cachingDiffFileComparator = new CachingDiffFileComparator();

						sortDiffFiles(dataResourceList,
								cachingDiffFileComparator);
						TimeZoneDateRange dateRange = calculateDateRange(dataResourceList);
						synchronized (fileDateRanges) {
							fileDateRanges.put(id, dateRange);
						}

						return (int) (sizes.get(id) / 1000l);
					}
				});
		for (Future<Integer> future : futures) {
			try {
				int worked = future.get();
				monitor.worked(worked);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			} catch (ExecutionException e) {
				LOGGER.error(e);
			}
		}
		monitor.done();
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link Diff}s.
	 * 
	 * @return
	 */
	public Set<ID> getIDs() {
		scanIfNecessary(null);
		return this.dataResourceLists.keySet();
	}

	/**
	 * Return the {@link TimeZoneDateRange} determined by the earliest and
	 * latest {@link Diff}
	 * 
	 * @param id
	 * @return
	 */
	public TimeZoneDateRange getDateRange(ID id) {
		scanIfNecessary(null);
		return this.fileDateRanges.get(id);
	}

	public IDataContainer getDiffFileDirectory() {
		return this.diffContainer;
	}

	/**
	 * Returns all {@link Diff}s associated with a given {@link ID}.
	 * <p>
	 * If the {@link DiffList} is already in the cached the cached version
	 * is returned. Otherwise a new {@link DiffList} is constructed and
	 * added to the cache.
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 * 
	 * @see DiffCache
	 */
	public DiffList getDiffFiles(ID id, IProgressMonitor progressMonitor) {
		return diffCache.getPayload(id, progressMonitor);
	}

	/**
	 * Returns all {@link Diff}s associated with a given {@link ID}.
	 * <p>
	 * In contrast to {@link #getDiffFiles(ID, IProgressMonitor)} this method
	 * always creates the objects anew and does not use any caching
	 * functionality.
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 */
	public DiffList createDiffFiles(ID id, IProgressMonitor progressMonitor) {
		scanIfNecessary(SubMonitor.convert(progressMonitor));
		DiffList diffFiles = DiffRecordList.create(
				this.dataResourceLists.get(id), this.trunk,
				this.sourceCache, progressMonitor);
		return diffFiles;
	}

	public void scanIfNecessary(SubMonitor monitor) {
		if (this.dataResourceLists == null && this.fileDateRanges == null) {
			scan(monitor);
		} else if (this.dataResourceLists == null
				&& this.fileDateRanges != null) {
			LOGGER.fatal("State error in "
					+ DiffContainer.class.getSimpleName() + "");
		} else if (this.dataResourceLists != null
				&& this.fileDateRanges == null) {
			LOGGER.fatal("State error in "
					+ DiffContainer.class.getSimpleName() + "");
		} else {
			// nothing to do
		}
	}

}
