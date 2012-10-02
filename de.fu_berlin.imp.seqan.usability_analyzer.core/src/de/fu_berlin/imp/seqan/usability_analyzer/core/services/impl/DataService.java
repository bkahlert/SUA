package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bkahlert.devel.nebula.data.DependencyGraph;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;

public class DataService implements IDataService {

	private static final Logger LOGGER = Logger.getLogger(DataService.class);

	private static final ExecutorService LOADER_POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(2);

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
		for (String file : StringUtils.split(
				new SUACorePreferenceUtil().getDataDirectory(), "|")) {
			FileBaseDataContainer container = new FileBaseDataContainer(
					new File(file));
			containers.add(container);
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
		sb.setLength(sb.length() - 1);
		new SUACorePreferenceUtil().setDataDirectory(sb.toString());
	}

	private DataListenerNotifier notifier = new DataListenerNotifier();
	private List<? extends IBaseDataContainer> activeBaseDataDirectories = loadActiveFromPreferences();

	public DataService() {
	}

	@Override
	public void addDataDirectoryServiceListener(
			IDataServiceListener dataServiceListener) {
		notifier.addDataDirectoryServiceListener(dataServiceListener);
	}

	@Override
	public void removeDataDirectoryServiceListener(
			IDataServiceListener dataServiceListener) {
		notifier.removeDataDirectoryServiceListener(dataServiceListener);
	}

	@Override
	public List<? extends IBaseDataContainer> getActiveDataDirectories() {
		return this.activeBaseDataDirectories;
	}

	@Override
	public void setActiveDataDirectories(
			List<? extends IBaseDataContainer> baseDataContainers) {
		dispose();

		this.activeBaseDataDirectories = baseDataContainers;
		saveActiveToPreferences(baseDataContainers);
		loadData(baseDataContainers);

		notifier.activeDataDirectoriesChanged(baseDataContainers);
	}

	private static void loadData(
			final List<? extends IBaseDataContainer> dataResourceContainers) {
		final DependencyGraph<String> loaderDependencies = new DependencyGraph<String>();
		final Map<String, IDataLoadProvider> dataLoadProviders = new HashMap<String, IDataLoadProvider>();
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.core.dataload");
		for (IConfigurationElement e : config) {
			try {
				String source = e.getAttribute("source");
				List<String> dependencies = new ArrayList<String>();
				for (IConfigurationElement element : e.getChildren()) {
					if ("Dependency".equals(element.getName())) {
						dependencies.add(element.getAttribute("source"));
					}
				}
				loaderDependencies.addNode(source, dependencies);
				dataLoadProviders.put(source, (IDataLoadProvider) e
						.createExecutableExtension("class"));
			} catch (CoreException e1) {
				LOGGER.fatal(e1);
			} catch (InvalidRegistryObjectException e1) {
				LOGGER.fatal("source does not exist in the defined dependency",
						e1);
			}
		}

		LOGGER.info("Loading " + StringUtils.join(dataResourceContainers, ", ")
				+ "...");
		final Job loader = new Job("Loading "
				+ StringUtils.join(dataResourceContainers, ", ") + "...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				long loadStart = System.currentTimeMillis();
				monitor.beginTask(getName(), dataLoadProviders.size());
				for (List<String> sources : loaderDependencies
						.getOrderedValues()) {
					LOGGER.info("-- set: " + sources);
					List<Future<Job>> futures = ExecutorUtil
							.nonUIAsyncExec(
									LOADER_POOL,
									sources,
									new ExecutorUtil.ParametrizedCallable<String, Job>() {
										@Override
										public Job call(final String source)
												throws Exception {
											final IDataLoadProvider dataLoadProvider = dataLoadProviders
													.get(source);
											Job loader = new Job(
													dataLoadProvider
															.getJobName(dataResourceContainers)) {
												@Override
												protected IStatus run(
														IProgressMonitor monitor) {
													final SubMonitor subMonitor = SubMonitor
															.convert(monitor);

													long start = System
															.currentTimeMillis();
													dataLoadProvider
															.load(dataResourceContainers,
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
			LOGGER.error("Error loading " + dataResourceContainers, e);
		}
	}

	@Override
	public List<? extends IBaseDataContainer> getDataDirectories() {
		return loadFromPreferences();
	}

	@Override
	public void addDataDirectories(
			List<? extends IBaseDataContainer> dataContainers) {
		List<IBaseDataContainer> currentDataDirectories = loadFromPreferences();
		for (IBaseDataContainer fileDataContainer : dataContainers) {
			if (!currentDataDirectories.contains(fileDataContainer))
				currentDataDirectories.add(fileDataContainer);
		}
		saveToPreferences(currentDataDirectories);
		notifier.dataDirectoriesAdded(dataContainers);
	}

	@Override
	public void removeDataDirectories(
			List<? extends IBaseDataContainer> dataContainers) {
		List<IBaseDataContainer> currentDataDirectories = loadFromPreferences();
		for (FileDataContainer fileDataContainer : ArrayUtils
				.getAdaptableObjects(dataContainers.toArray(),
						FileDataContainer.class)) {
			currentDataDirectories.remove(fileDataContainer.getFile()
					.toString());
		}
		saveToPreferences(currentDataDirectories);
		notifier.dataDirectoriesRemoved(dataContainers);
	}

	public void dispose() {
		LOGGER.info("Disposing "
				+ StringUtils.join(this.activeBaseDataDirectories, ", "));
		for (IBaseDataContainer activeBaseDataContainer : this.activeBaseDataDirectories) {
			activeBaseDataContainer.dispose();
		}
		LOGGER.info("Disposed "
				+ StringUtils.join(this.activeBaseDataDirectories, ", "));
	}
}
