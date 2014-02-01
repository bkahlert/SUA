package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bkahlert.devel.nebula.utils.ExecutorService;
import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.DataLoaderManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;

public class DataService implements IDataService {

	private static final Logger LOGGER = Logger.getLogger(DataService.class);

	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService();

	public static List<IBaseDataContainer> loadFromPreferences() {
		List<IBaseDataContainer> containers = new ArrayList<IBaseDataContainer>();
		for (String file : new SUACorePreferenceUtil().getDataDirectories()) {
			FileBaseDataContainer container = new FileBaseDataContainer(
					new File(file));
			containers.add(container);
		}
		return containers;
	}

	public static void saveToPreferences(
			List<? extends IBaseDataContainer> baseDataContainers) {
		List<String> c = new ArrayList<String>();
		for (FileDataContainer fileDataContainer : ArrayUtils
				.getAdaptableObjects(baseDataContainers.toArray(),
						FileDataContainer.class)) {
			c.add(fileDataContainer.getFile().getAbsolutePath());
		}
		new SUACorePreferenceUtil().setDataDirectories(c);
	}

	public static List<IBaseDataContainer> loadActiveFromPreferences() {
		List<IBaseDataContainer> containers = new ArrayList<IBaseDataContainer>();
		String internalDataDirectory = new SUACorePreferenceUtil()
				.getDataDirectory();
		if (internalDataDirectory != null) {
			for (String file : StringUtils.split(internalDataDirectory, "|")) {
				FileBaseDataContainer container = new FileBaseDataContainer(
						new File(file));
				containers.add(container);
			}
		}
		return containers;
	}

	public static void saveActiveToPreferences(
			List<? extends IBaseDataContainer> baseDataContainers) {
		StringBuilder sb = new StringBuilder();
		for (FileDataContainer fileDataContainer : ArrayUtils
				.getAdaptableObjects(baseDataContainers.toArray(),
						FileDataContainer.class)) {
			sb.append(fileDataContainer.getFile().toString());
			sb.append("|");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		new SUACorePreferenceUtil().setDataDirectory(sb.toString());
	}

	private DataServiceListenerNotifier notifier = new DataServiceListenerNotifier();
	private DataLoaderManager dataLoaderManager = new DataLoaderManager();
	private List<? extends IBaseDataContainer> activeBaseDataDirectories = new ArrayList<IBaseDataContainer>();

	public DataService() {
	}

	@Override
	public void addDataServiceListener(IDataServiceListener dataServiceListener) {
		this.notifier.addDataDirectoryServiceListener(dataServiceListener);
	}

	@Override
	public void removeDataServiceListener(
			IDataServiceListener dataServiceListener) {
		this.notifier.removeDataDirectoryServiceListener(dataServiceListener);
	}

	@Override
	public List<? extends IBaseDataContainer> getActiveDataDirectories() {
		return this.activeBaseDataDirectories;
	}

	@Override
	public void loadDataDirectories(
			List<? extends IBaseDataContainer> baseDataContainers) {
		if (baseDataContainers == null) {
			baseDataContainers = new ArrayList<IBaseDataContainer>();
		}

		unloadData(this.dataLoaderManager, this.activeBaseDataDirectories);
		this.notifier.dataDirectoriesUnloaded(this.activeBaseDataDirectories);

		saveActiveToPreferences(baseDataContainers);
		loadData(this.dataLoaderManager, baseDataContainers);
		this.activeBaseDataDirectories = baseDataContainers;

		this.notifier.dataDirectoriesLoaded(this.activeBaseDataDirectories);
	}

	private static void loadData(final DataLoaderManager dataLoaderManager,
			final List<? extends IBaseDataContainer> baseDataContainers) {
		LOGGER.info("Loading " + StringUtils.join(baseDataContainers, ", ")
				+ "...");
		final Job loader = new Job("Loading "
				+ StringUtils.join(baseDataContainers, ", ") + "...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				long loadStart = System.currentTimeMillis();
				monitor.beginTask(this.getName(),
						dataLoaderManager.getNumDataLoaderProviders());
				for (List<String> sources : dataLoaderManager
						.getLoadDependencies()) {
					LOGGER.info("-- set: " + sources);
					List<Future<Job>> futures = EXECUTOR_SERVICE
							.nonUIAsyncExec(sources,
									new ParametrizedCallable<String, Job>() {
										@Override
										public Job call(final String source)
												throws Exception {
											final IDataLoadProvider dataLoadProvider = dataLoaderManager
													.getDataLoadProvider(source);
											Job loader = new Job(
													dataLoadProvider
															.getUnloaderJobName(baseDataContainers)) {
												@Override
												protected IStatus run(
														IProgressMonitor monitor) {
													final SubMonitor subMonitor = SubMonitor
															.convert(monitor);

													long start = System
															.currentTimeMillis();
													dataLoadProvider.load(
															baseDataContainers,
															subMonitor);
													LOGGER.info("---- loaded "
															+ source
															+ " within "
															+ (System
																	.currentTimeMillis() - start)
															+ "ms");

													subMonitor.done();
													return Status.OK_STATUS;
												}
											};
											loader.setProgressGroup(monitor, 1);
											loader.setSystem(true);
											loader.schedule();
											return loader;
										}
									});
					for (Future<Job> future : futures) {
						try {
							future.get().join();
							monitor.worked(1);
						} catch (InterruptedException e) {
							LOGGER.error(e);
						} catch (ExecutionException e) {
							LOGGER.error(e);
						}
					}
				}
				LOGGER.info("Finished loading within "
						+ (System.currentTimeMillis() - loadStart) + "ms");
				return Status.OK_STATUS;
			}
		};
		loader.schedule();
		try {
			loader.join();
		} catch (InterruptedException e) {
			LOGGER.error("Error loading " + baseDataContainers, e);
		}
	}

	public static void unloadData(final DataLoaderManager dataLoaderManager,
			final List<? extends IBaseDataContainer> baseDataContainers) {
		LOGGER.info("Unloading " + StringUtils.join(baseDataContainers, ", ")
				+ "...");
		final Job unloader = new Job("Unloading "
				+ StringUtils.join(baseDataContainers, ", ") + "...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				long unloadStart = System.currentTimeMillis();
				monitor.beginTask(this.getName(),
						dataLoaderManager.getNumDataLoaderProviders());
				for (List<String> sources : dataLoaderManager
						.getUnloadDependencies()) {
					LOGGER.info("-- set: " + sources);
					List<Future<Job>> futures = EXECUTOR_SERVICE
							.nonUIAsyncExec(sources,
									new ParametrizedCallable<String, Job>() {
										@Override
										public Job call(final String source)
												throws Exception {
											final IDataLoadProvider dataLoadProvider = dataLoaderManager
													.getDataLoadProvider(source);
											Job loader = new Job(
													dataLoadProvider
															.getLoaderJobName(baseDataContainers)) {
												@Override
												protected IStatus run(
														IProgressMonitor monitor) {
													final SubMonitor subMonitor = SubMonitor
															.convert(monitor);

													long start = System
															.currentTimeMillis();
													dataLoadProvider
															.unload(subMonitor);
													LOGGER.info("---- unloaded "
															+ source
															+ " within "
															+ (System
																	.currentTimeMillis() - start)
															+ "ms");

													subMonitor.done();
													return Status.OK_STATUS;
												}
											};
											loader.setProgressGroup(monitor, 1);
											loader.setSystem(true);
											loader.schedule();
											return loader;
										}
									});
					for (Future<Job> future : futures) {
						try {
							future.get().join();
							monitor.worked(1);
						} catch (InterruptedException e) {
							LOGGER.error(e);
						} catch (ExecutionException e) {
							LOGGER.error(e);
						}
					}
				}
				LOGGER.info("Finished unloading within "
						+ (System.currentTimeMillis() - unloadStart) + "ms");
				return Status.OK_STATUS;
			}
		};
		unloader.schedule();
		try {
			unloader.join();
		} catch (InterruptedException e) {
			LOGGER.error("Error unloading " + baseDataContainers, e);
		}
	}

	@Override
	public List<? extends IBaseDataContainer> getDataDirectories() {
		return loadFromPreferences();
	}

	@Override
	public void addDataDirectories(
			List<? extends IBaseDataContainer> dataContainers) {
		if (dataContainers.size() == 0) {
			return;
		}
		List<IBaseDataContainer> currentDataDirectories = loadFromPreferences();
		for (IBaseDataContainer fileDataContainer : dataContainers) {
			if (!currentDataDirectories.contains(fileDataContainer)) {
				currentDataDirectories.add(fileDataContainer);
			}
		}
		saveToPreferences(currentDataDirectories);
		this.notifier.dataDirectoriesAdded(dataContainers);
	}

	@Override
	public void removeDataDirectories(
			List<? extends IBaseDataContainer> dataContainers) {
		if (dataContainers.size() == 0) {
			return;
		}
		List<IBaseDataContainer> currentDataDirectories = loadFromPreferences();
		for (IBaseDataContainer baseDataContainer : dataContainers) {
			currentDataDirectories.remove(baseDataContainer);
		}
		saveToPreferences(currentDataDirectories);
		this.notifier.dataDirectoriesRemoved(dataContainers);
	}

	@Override
	public void unloadData() {
		this.dataLoaderManager.unload();
		this.activeBaseDataDirectories = new ArrayList<IBaseDataContainer>();
	}

	@Override
	public void restoreLastDataDirectories() {
		List<IBaseDataContainer> baseDataContainers = loadActiveFromPreferences();
		this.loadDataDirectories(baseDataContainers);
	}
}
