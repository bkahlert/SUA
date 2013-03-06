package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.DataServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;

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
				baseDataContainers = dataContainers
						.toArray(new IBaseDataContainer[0]);
				try {
					compilationStates = CompilationServiceUtils
							.getCompilationStates(baseDataContainers);
				} catch (IOException e) {
					LOGGER.error("Error reading compulation states", e);
					compilationStates = null;
				}
			} else {
				baseDataContainers = null;
				compilationStates = null;
			}
		}

		@Override
		public void dataDirectoriesUnloaded(
				List<? extends IBaseDataContainer> dataContainers) {
			baseDataContainers = null;
			compilationStates = null;
		}
	};

	/**
	 * Constructs a {@link CompilationService} that uses relies on an available
	 * {@link IDataService}.
	 */
	public CompilationService() {
		this.dataService = (IDataService) PlatformUI.getWorkbench().getService(
				IDataService.class);
		this.dataService.addDataServiceListener(dataServiceListener);
	}

	public CompilationService(IBaseDataContainer baseDataContainer)
			throws IOException {
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
		if (compilationStates == null)
			return null;
		URI uri = compilable.getUri();
		if (!compilationStates.containsKey(uri))
			return null;
		return compilationStates.get(uri);
	}

	@Override
	public boolean compiles(ICompilable[] compilables, Boolean state) {
		if (this.compilationStates == null)
			return false;
		for (ICompilable compilable : compilables) {
			this.compilationStates.put(compilable.getUri(), state);
		}
		try {
			CompilationServiceUtils.setCompilationStates(baseDataContainers,
					compilationStates);
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
					baseDataContainers, compilable.getUri());
		} catch (IOException e) {
			LOGGER.error("Error reading the compiler output", e);
		}
		return "";
	}

	@Override
	public void compilerOutput(ICompilable compilable, String html) {
		Assert.isNotNull(compilable);
		if (html == null)
			html = "";
		if (compilerOutput(compilable).equals(html))
			return;
		try {
			CompilationServiceUtils.setCompilerOutput(baseDataContainers,
					compilable.getUri(), html);
		} catch (IOException e) {
			LOGGER.error("Error reading the compiler output", e);
		}
		this.notifier.compilerOutputChanged(compilable, html);
	}

	@Override
	public void dispose() {
		if (this.dataService != null)
			this.dataService.removeDataServiceListener(dataServiceListener);
	}
}
