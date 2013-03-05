package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.DataServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;

public class CompilationService implements ICompilationService, IDisposable {

	private static final Logger LOGGER = Logger
			.getLogger(CompilationService.class);

	private static final String SCOPE = "compilation";
	private static final String NAME = "states.properties";

	private CompilationServiceListenerNotifier notifier = new CompilationServiceListenerNotifier();

	private static File getCompilationStateData(
			IBaseDataContainer baseDataContainer) throws IOException {
		return baseDataContainer.getStaticFile(SCOPE, NAME);
	}

	private static Map<URI, Boolean> getCompilationStates(
			IBaseDataContainer[] baseDataContainers) {
		Map<URI, Boolean> compilationStates = new HashMap<URI, Boolean>();
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			try {
				File compilationStateFile = getCompilationStateData(baseDataContainer);
				Properties properties = new Properties();
				properties.load(new FileReader(compilationStateFile));
				for (Entry<Object, Object> entry : properties.entrySet()) {
					try {
						URI uri = new URI(entry.getKey().toString());
						String value = entry.getValue().toString();
						if (value.equalsIgnoreCase("null")) {
							compilationStates.put(uri, null);
						} else if (value.equalsIgnoreCase("true")) {
							compilationStates.put(uri, true);
						} else if (value.equalsIgnoreCase("false")) {
							compilationStates.put(uri, false);
						} else {
							LOGGER.warn(compilationStateFile + ":" + uri
									+ " contains invalid compilation state "
									+ value);
						}
					} catch (Exception e) {
						LOGGER.warn(compilationStateFile + " processing error",
								e);
					}
				}
			} catch (RuntimeException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return compilationStates;
	}

	private static void setCompilationStates(
			IBaseDataContainer[] baseDataContainers,
			Map<URI, Boolean> compilationStates) {
		Properties properties = new Properties();
		for (Entry<URI, Boolean> entry : compilationStates.entrySet()) {
			String key = entry.getKey().toString();
			String value = "null";
			if (Boolean.TRUE.equals(entry.getValue()))
				value = "true";
			else if (Boolean.FALSE.equals(entry.getValue()))
				value = "false";
			properties.put(key, value);
		}
		File compilationStateFile = null;
		try {
			compilationStateFile = File.createTempFile("compilation_states",
					".properties");
			OutputStream out = new FileOutputStream(compilationStateFile);
			properties.store(out,
					"Last Update: " + new TimeZoneDate().toISO8601());
		} catch (IOException e) {
			LOGGER.error("Error writing compilation states", e);
		}

		if (compilationStateFile != null) {
			for (IBaseDataContainer baseDataContainer : baseDataContainers) {
				try {
					baseDataContainer
							.putFile(SCOPE, NAME, compilationStateFile);
				} catch (IOException e) {
					LOGGER.error("Error writing compilation states to "
							+ baseDataContainer, e);
				}
			}
		}
	}

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
				compilationStates = getCompilationStates(baseDataContainers);
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

	public CompilationService(IBaseDataContainer baseDataContainer) {
		this.baseDataContainers = new IBaseDataContainer[] { baseDataContainer };
		this.compilationStates = getCompilationStates(this.baseDataContainers);
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
		setCompilationStates(baseDataContainers, compilationStates);
		this.notifier.compilationStateChanged(compilables, state);
		return true;
	}

	@Override
	public void dispose() {
		if (this.dataService != null)
			this.dataService.removeDataServiceListener(dataServiceListener);
	}
}
