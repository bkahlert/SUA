package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorService;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataList;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.AggregatedBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.CachingDiffFileComparator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.Trunk;

/**
 * Instances of this class represent {@link IDataContainer}s that contain
 * {@link IDiff}s.
 * <p>
 * For each class of {@link IDiff}s a sub {@link IDataContainer} with a valid
 * {@link IIdentifier} as its name must exist. Each of those sub
 * {@link IDataContainer} contains the {@link IDiff}s belonging to this
 * {@link IIdentifier}.
 * 
 * @author bkahlert
 * 
 */
public class DiffContainer extends AggregatedBaseDataContainer {

	private static final Logger LOGGER = Logger.getLogger(DiffContainer.class);

	public static final int DIFF_CACHE_SIZE = 5;

	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService();

	/**
	 * Scans through the given directory, looks for sub directories with valid
	 * names (see {@link IIdentifier#isValid(String)}) and maps all containing
	 * files their corresponding {@link IIdentifier}.
	 * 
	 * @param diffContainer
	 * @return
	 */
	private static Map<ID, DataList> readDiffFilesMapping(
			DiffContainer diffContainer) {
		Map<ID, DataList> rawFiles = new HashMap<ID, DataList>();
		for (IDataContainer diffFileDir : diffContainer.getDiffFileDirectory()
				.getSubContainers()) {
			if (IdentifierFactory.createFrom(diffFileDir.getName()) == null) {
				LOGGER.warn("Directory with invalid "
						+ IIdentifier.class.getSimpleName()
						+ " name detected: " + diffFileDir.toString());
				continue;
			}

			for (IData diffFile : diffFileDir.getResources()) {
				if (!Diff.PATTERN.matcher(diffFile.getName()).matches()
						&& !Diff.ZIPPED_PATTERN.matcher(diffFile.getName())
								.matches()) {
					continue;
				}
				ID id = DiffDataUtils.getId(diffFile);
				if (!rawFiles.containsKey(id)) {
					rawFiles.put(id, new DataList());
				}
				rawFiles.get(id).add(diffFile);
			}
		}
		return rawFiles;
	}

	private static void sortDiffFiles(DataList diffFiles,
			Comparator<IData> fileComparator) {
		Collections.sort(diffFiles, fileComparator);
	}

	private static TimeZoneDateRange calculateDateRange(DataList dataList) {
		TimeZoneDate start = null;
		TimeZoneDate end = null;
		if (dataList.size() > 0) {
			start = DiffDataUtils.getDate(dataList.get(0), null);
			end = DiffDataUtils
					.getDate(dataList.get(dataList.size() - 1), null);
		}
		return new TimeZoneDateRange(start, end);
	}

	private IDataContainer diffContainer;
	private ITrunk trunk;
	private ISourceStore sourceCache;
	private Map<ID, DataList> dataLists;
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
	public DiffContainer(List<? extends IBaseDataContainer> baseDataContainers) {
		super(baseDataContainers);
		this.diffContainer = this.getSubContainer("diff");
		try {
			this.trunk = new Trunk(this.getSubContainer("trunk"));
		} catch (Exception e) {
			LOGGER.error("Could not access sub directory \"trunk\"", e);
		}
		this.sourceCache = new SourceCache(this);

		this.diffCache = new DiffCache(this, DIFF_CACHE_SIZE);
	}

	public DiffContainer(IBaseDataContainer baseDataContainer) {
		this(Arrays.asList(baseDataContainer));
	}

	public void scan(SubMonitor monitor) {
		monitor = SubMonitor.convert(monitor);
		this.dataLists = readDiffFilesMapping(this);
		this.fileDateRanges = new HashMap<ID, TimeZoneDateRange>(
				this.dataLists.size());

		long size = 0;
		final HashMap<IIdentifier, Long> sizes = new HashMap<IIdentifier, Long>();
		for (IIdentifier identifier : this.dataLists.keySet()) {
			long fileListSize = 0;
			for (IData file : this.dataLists.get(identifier)) {
				fileListSize += file.getLength();
			}
			sizes.put(identifier, fileListSize);
			size += fileListSize;
		}

		monitor.beginTask("Loading " + this, (int) (size / 1000l));
		List<Future<Integer>> futures = EXECUTOR_SERVICE
				.nonUIAsyncExec(
						this.dataLists.keySet(),
						new com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable<ID, Integer>() {
							@Override
							public Integer call(ID id) throws Exception {
								final DataList dataList = DiffContainer.this.dataLists
										.get(id);

								final CachingDiffFileComparator cachingDiffFileComparator = new CachingDiffFileComparator();

								sortDiffFiles(dataList,
										cachingDiffFileComparator);
								TimeZoneDateRange dateRange = calculateDateRange(dataList);
								synchronized (DiffContainer.this.fileDateRanges) {
									DiffContainer.this.fileDateRanges.put(id,
											dateRange);
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
	 * Returns a list of all {@link IIdentifier}s occurring in the managed
	 * {@link Diff}s.
	 * 
	 * @return
	 */
	public Set<ID> getIDs() {
		this.scanIfNecessary(null);
		return this.dataLists.keySet();
	}

	/**
	 * Return the {@link TimeZoneDateRange} determined by the earliest and
	 * latest {@link Diff}
	 * 
	 * @param id
	 * @return
	 */
	public TimeZoneDateRange getDateRange(IIdentifier id) {
		this.scanIfNecessary(null);
		return this.fileDateRanges.get(id);
	}

	public IDataContainer getDiffFileDirectory() {
		return this.diffContainer;
	}

	/**
	 * Returns all {@link Diff}s associated with a given {@link IIdentifier}.
	 * <p>
	 * If the {@link Diffs} is already in the cached the cached version is
	 * returned. Otherwise a new {@link Diffs} is constructed and added to the
	 * cache.
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 * 
	 * @see DiffCache
	 */
	public IDiffs getDiffFiles(IIdentifier id, IProgressMonitor progressMonitor) {
		return this.diffCache.getPayload(id, progressMonitor);
	}

	/**
	 * Returns all {@link IDiff}s associated with a given {@link IIdentifier}.
	 * <p>
	 * In contrast to {@link #getDiffFiles(IIdentifier, IProgressMonitor)} this
	 * method always creates the objects anew and does not use any caching
	 * functionality.
	 * 
	 * @param identifier
	 * @param progressMonitor
	 * @return
	 */
	public IDiffs createDiffFiles(IIdentifier identifier,
			IProgressMonitor progressMonitor) {
		this.scanIfNecessary(SubMonitor.convert(progressMonitor));
		DataList dataList = this.dataLists.get(identifier);
		if (dataList != null) {
			IDiffs diffFiles = DiffRecords.create(
					this.dataLists.get(identifier), this.trunk,
					this.sourceCache, progressMonitor);
			return diffFiles;
		} else {
			return null;
		}
	}

	public void scanIfNecessary(SubMonitor monitor) {
		if (this.dataLists == null && this.fileDateRanges == null) {
			this.scan(monitor);
		} else if (this.dataLists != null && this.fileDateRanges != null) {
			// nothing to do
		} else {
			LOGGER.fatal("State error in "
					+ DiffContainer.class.getSimpleName() + "");
		}
	}

}
