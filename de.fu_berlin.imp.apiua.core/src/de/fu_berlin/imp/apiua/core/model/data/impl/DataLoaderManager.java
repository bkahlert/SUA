package de.fu_berlin.imp.apiua.core.model.data.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.bkahlert.nebula.data.DependencyGraph;

import de.fu_berlin.imp.apiua.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

public class DataLoaderManager {

	private static final Logger LOGGER = Logger
			.getLogger(DataLoaderManager.class);

	/**
	 * This {@link IDataLoadProvider} is executed before the first and after the
	 * last registered {@link IDataLoadProvider}.
	 */
	private IDataLoadProvider dataServiceLoaderBracket = new IDataLoadProvider() {
		private List<? extends IBaseDataContainer> baseDataContainer = null;

		@Override
		public String getLoaderJobName(
				List<? extends IBaseDataContainer> baseDataProviders) {
			return "Preparing load process...";
		}

		@Override
		public String getUnloaderJobName(
				List<? extends IBaseDataContainer> baseDataContainer) {
			return "Cleaning up...";
		}

		@Override
		public IDataContainer load(
				List<? extends IBaseDataContainer> baseDataContainer,
				IProgressMonitor progressMonitor) {
			this.baseDataContainer = baseDataContainer;
			return new AggregatedBaseDataContainer(baseDataContainer);
		}

		@Override
		public void unload(IProgressMonitor progressMonitor) {
			if (this.baseDataContainer == null)
				return;
			for (IBaseDataContainer baseDataContainer : this.baseDataContainer) {
				LOGGER.info("Disposing " + baseDataContainer);
				baseDataContainer.dispose();
				LOGGER.info("Disposed " + baseDataContainer);
			}
		}
	};

	private final DependencyGraph<String> loaderDependencies = new DependencyGraph<String>();
	private final Map<String, IDataLoadProvider> dataLoadProviders = new HashMap<String, IDataLoadProvider>();

	public DataLoaderManager() {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.apiua.core.dataload");
		for (IConfigurationElement e : config) {
			try {
				String source = e.getAttribute("source");
				List<String> dependencies = new ArrayList<String>();
				for (IConfigurationElement element : e.getChildren()) {
					if ("Dependency".equals(element.getName())) {
						dependencies.add(element.getAttribute("source"));
					}
				}
				dependencies.add(DataLoaderManager.class.getCanonicalName());
				addDataLoaderProvider(source, dependencies,
						(IDataLoadProvider) e
								.createExecutableExtension("class"));
			} catch (CoreException e1) {
				LOGGER.fatal(e1);
			} catch (InvalidRegistryObjectException e1) {
				LOGGER.fatal("source does not exist in the defined dependency",
						e1);
			}
		}

		this.addDataLoaderProvider(DataLoaderManager.class.getCanonicalName(),
				new ArrayList<String>(), dataServiceLoaderBracket);
	}

	public void addDataLoaderProvider(String source, List<String> dependencies,
			IDataLoadProvider dataLoadProvider) {
		loaderDependencies.addNode(source, dependencies);
		dataLoadProviders.put(source, dataLoadProvider);
	}

	public List<List<String>> getLoadDependencies() {
		return loaderDependencies.getOrderedValues();
	}

	public List<List<String>> getUnloadDependencies() {
		List<List<String>> unloadDependencies = getLoadDependencies();
		Collections.reverse(unloadDependencies);
		return unloadDependencies;
	}

	public IDataLoadProvider getDataLoadProvider(String source) {
		return this.dataLoadProviders.get(source);
	}

	public int getNumDataLoaderProviders() {
		return this.dataLoadProviders.size();
	}

	public void unload() {
		this.dataServiceLoaderBracket.unload(new NullProgressMonitor());
	}
}