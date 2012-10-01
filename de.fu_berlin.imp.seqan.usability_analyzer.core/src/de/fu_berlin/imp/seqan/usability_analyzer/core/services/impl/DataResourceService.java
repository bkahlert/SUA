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
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataDirectoriesService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataDirectoriesServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;

public class DataResourceService implements IDataDirectoriesService {

	private static final Logger LOGGER = Logger
			.getLogger(DataResourceService.class);

	private static final ExecutorService LOADER_POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(2);

	private DataResourceListenerNotifier notifier = new DataResourceListenerNotifier();
	private SUACorePreferenceUtil prefs;

	public DataResourceService() {
		this.prefs = new SUACorePreferenceUtil();
	}

	@Override
	public void addDataDirectoryServiceListener(
			IDataDirectoriesServiceListener dataDirectoriesServiceListener) {
		notifier.addDataDirectoryServiceListener(dataDirectoriesServiceListener);
	}

	@Override
	public void removeDataDirectoryServiceListener(
			IDataDirectoriesServiceListener dataDirectoriesServiceListener) {
		notifier.removeDataDirectoryServiceListener(dataDirectoriesServiceListener);
	}

	@Override
	public List<IBaseDataContainer> getActiveDataDirectories() {
		List<IBaseDataContainer> containers = new ArrayList<IBaseDataContainer>();
		for (String file : StringUtils.split(prefs.getDataDirectory(), "|")) {
			FileBaseDataContainer container = new FileBaseDataContainer(
					new File(file));
			containers.add(container);
		}
		return containers;
	}

	@Override
	public void setActiveDataDirectories(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		StringBuilder sb = new StringBuilder();
		for (FileDataContainer fileDataContainer : ArrayUtils
				.getAdaptableObjects(dataResourceContainers.toArray(),
						FileDataContainer.class)) {
			sb.append(fileDataContainer.getFile().toString());
			sb.append("|");
		}
		sb.setLength(sb.length() - 1);
		this.prefs.setDataDirectory(sb.toString());

		loadData(dataResourceContainers);

		notifier.activeDataDirectoriesChanged(dataResourceContainers);
	}

	private void loadData(
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
		List<IBaseDataContainer> dataResourceContainers = new ArrayList<IBaseDataContainer>();
		for (String dataDirectory : prefs.getDataDirectories()) {
			dataResourceContainers.add(new FileBaseDataContainer(new File(
					dataDirectory)));
		}
		return dataResourceContainers;
	}

	@Override
	public void addDataDirectories(
			List<? extends IBaseDataContainer> dataContainers) {
		List<String> currentDataDirectories = this.prefs.getDataDirectories();
		for (FileDataContainer fileDataContainer : ArrayUtils
				.getAdaptableObjects(dataContainers.toArray(),
						FileDataContainer.class)) {
			currentDataDirectories.add(fileDataContainer.getFile().toString());
		}
		this.prefs.setDataDirectories(currentDataDirectories);
		notifier.dataDirectoriesAdded(dataContainers);
	}

	@Override
	public void removeDataDirectories(
			List<? extends IBaseDataContainer> dataContainers) {
		List<String> currentDataDirectories = this.prefs.getDataDirectories();
		for (FileDataContainer fileDataContainer : ArrayUtils
				.getAdaptableObjects(dataContainers.toArray(),
						FileDataContainer.class)) {
			currentDataDirectories.remove(fileDataContainer.getFile()
					.toString());
		}
		this.prefs.setDataDirectories(currentDataDirectories);
		notifier.dataDirectoriesRemoved(dataContainers);
	}

}
