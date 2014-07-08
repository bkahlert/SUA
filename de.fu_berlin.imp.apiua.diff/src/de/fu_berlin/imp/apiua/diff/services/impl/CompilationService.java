package de.fu_berlin.imp.apiua.diff.services.impl;

import java.io.IOException;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.services.DataServiceAdapter;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IDataServiceListener;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.diff.model.ICompilable;
import de.fu_berlin.imp.apiua.diff.services.ICompilationService;
import de.fu_berlin.imp.apiua.diff.services.ICompilationServiceListener;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

public class CompilationService implements ICompilationService, IDisposable {

	static final Logger LOGGER = Logger.getLogger(CompilationService.class);

	private CompilationServiceListenerNotifier notifier = new CompilationServiceListenerNotifier();

	private IBaseDataContainer[] baseDataContainers = null;
	private Map<URI, Boolean> compilationStates = null;

	private IDataService dataService = null;
	private IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> dataContainers) {
			if (dataContainers != null && dataContainers.size() > 0) {
				CompilationService.this.baseDataContainers = dataContainers
						.toArray(new IBaseDataContainer[0]);
				try {
					CompilationService.this.compilationStates = CompilationServiceUtils
							.getCompilationStates(CompilationService.this.baseDataContainers);
				} catch (IOException e) {
					LOGGER.error("Error reading compulation states", e);
					CompilationService.this.compilationStates = null;
				}
			} else {
				CompilationService.this.baseDataContainers = null;
				CompilationService.this.compilationStates = null;
			}
		}

		@Override
		public void dataDirectoriesUnloaded(
				List<? extends IBaseDataContainer> dataContainers) {
			CompilationService.this.baseDataContainers = null;
			CompilationService.this.compilationStates = null;
		}
	};

	/**
	 * Constructs a {@link CompilationService} that uses relies on an available
	 * {@link ILocatorService}.
	 */
	public CompilationService() {
		this.dataService = (IDataService) PlatformUI.getWorkbench().getService(
				IDataService.class);
		this.dataService.addDataServiceListener(this.dataServiceListener);
		this.baseDataContainers = this.dataService.getActiveDataDirectories()
				.toArray(new IBaseDataContainer[0]);
	}

	/**
	 * For testing purposes only!
	 * 
	 * @param baseDataContainer
	 * @throws IOException
	 */
	CompilationService(IBaseDataContainer baseDataContainer) throws IOException {
		this.baseDataContainers = new IBaseDataContainer[] { baseDataContainer };
		this.compilationStates = CompilationServiceUtils
				.getCompilationStates(this.baseDataContainers);
	}

	@Override
	public void addCompilationServiceListener(
			ICompilationServiceListener compilationServiceListener) {
		this.notifier.addCompilationServiceListener(compilationServiceListener);
	}

	@Override
	public void removeCompilationServiceListener(
			ICompilationServiceListener compilationServiceListener) {
		this.notifier
				.removeCompilationServiceListener(compilationServiceListener);
	}

	@Override
	public Boolean compiles(ICompilable compilable) {
		if (this.compilationStates == null) {
			return null;
		}
		URI uri = compilable.getUri();
		if (!this.compilationStates.containsKey(uri)) {
			return null;
		}
		return this.compilationStates.get(uri);
	}

	@Override
	public boolean compiles(ICompilable[] compilables, Boolean state) {
		if (this.compilationStates == null) {
			return false;
		}
		for (ICompilable compilable : compilables) {
			this.compilationStates.put(compilable.getUri(), state);
		}
		try {
			CompilationServiceUtils.setCompilationStates(
					this.baseDataContainers, this.compilationStates);
		} catch (IOException e) {
			LOGGER.error("Error setting the compilation state", e);
		}
		this.notifier.compilationStateChanged(compilables, state);
		return true;
	}

	@Override
	public String compilerOutput(ICompilable compilable) {
		Assert.isNotNull(compilable);
		try {
			return CompilationServiceUtils.getCompilerOutput(
					this.baseDataContainers, compilable.getUri());
		} catch (IOException e) {
			LOGGER.error("Error reading the compiler output", e);
		}
		return "";
	}

	@Override
	public void compilerOutput(ICompilable compilable, String html) {
		Assert.isNotNull(compilable);
		if (html == null) {
			html = "";
		}
		if (this.compilerOutput(compilable).equals(html)) {
			return;
		}
		try {
			CompilationServiceUtils.setCompilerOutput(this.baseDataContainers,
					compilable.getUri(), html);
		} catch (IOException e) {
			LOGGER.error("Error reading the compiler output", e);
		}
		this.notifier.compilerOutputChanged(compilable, html);
	}

	@Override
	public String executionOutput(ICompilable compilable) {
		Assert.isNotNull(compilable);
		try {
			return CompilationServiceUtils.getExecutionOutput(
					this.baseDataContainers, compilable.getUri());
		} catch (IOException e) {
			LOGGER.error("Error reading the execution output", e);
		}
		return "";
	}

	@Override
	public void executionOutput(ICompilable compilable, String html) {
		Assert.isNotNull(compilable);
		if (html == null) {
			html = "";
		}
		if (this.executionOutput(compilable).equals(html)) {
			return;
		}
		try {
			CompilationServiceUtils.setExecutionOutput(this.baseDataContainers,
					compilable.getUri(), html);
		} catch (IOException e) {
			LOGGER.error("Error reading the execution output", e);
		}
		this.notifier.executionOutputChanged(compilable, html);
	}

	@Override
	public void dispose() {
		if (this.dataService != null) {
			this.dataService
					.removeDataServiceListener(this.dataServiceListener);
		}
	}
}
