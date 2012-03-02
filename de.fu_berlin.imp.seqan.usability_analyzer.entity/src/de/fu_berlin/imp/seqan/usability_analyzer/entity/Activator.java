package de.fu_berlin.imp.seqan.usability_analyzer.entity;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorsUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyRecordManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.entity"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(Activator.class);

	// The shared instance
	private static Activator plugin;

	private SUACorePreferenceUtil preferenceUtil;

	private SurveyRecordManager surveyRecordManager;

	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private EntityManager entityManager;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		URL confURL = getBundle().getEntry("log4j.properties");
		PropertyConfigurator
				.configure(FileLocator.toFileURL(confURL).getFile());

		Logger logger = Logger.getLogger(Activator.class);

		this.preferenceUtil = new SUACorePreferenceUtil();
		File logfilePath = this.preferenceUtil.getLogDirectory();
		File surveyRecordPath = this.preferenceUtil.getSurveyRecordPath();

		DiffFileDirectory diffFileDirectory = de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator
				.getDefault().getDiffFileDirectory();
		DoclogDirectory doclogDirectory = de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator
				.getDefault().getDoclogDirectory();

		try {
			long start = System.currentTimeMillis();

			this.surveyRecordManager = new SurveyRecordManager(surveyRecordPath);
			this.statsFileManager = new StatsFileManager(logfilePath);
			this.cMakeCacheFileManager = new CMakeCacheFileManager(logfilePath);

			ExecutorService executorService = ExecutorsUtil
					.newFixedMultipleOfProcessorsThreadPool(2);
			Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						surveyRecordManager.scanRecords();
					} catch (Exception e) {
						LOGGER.fatal(e);
					}
					return null;
				}
			});
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						statsFileManager.scanFiles();
					} catch (Exception e) {
						LOGGER.fatal(e);
					}
					return null;
				}
			});
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						cMakeCacheFileManager.scanFiles();
					} catch (Exception e) {
						LOGGER.fatal(e);
					}
					return null;
				}
			});
			try {
				executorService.invokeAll(callables);
			} catch (InterruptedException e) {
				LOGGER.fatal("Error matching " + Entity.class.getSimpleName(),
						e);
			}

			this.mapper = new Mapper(doclogDirectory, logfilePath);
			this.entityManager = new EntityManager(diffFileDirectory,
					doclogDirectory, surveyRecordManager,
					this.statsFileManager, this.cMakeCacheFileManager, mapper);
			this.entityManager.scan();

			logger.info(EntityManager.class.getSimpleName() + " "
					+ doclogDirectory.getName() + " scanned within "
					+ (System.currentTimeMillis() - start) + "ms.");
		} catch (DataSourceInvalidException e) {
			ErrorDialog
					.openError(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							"Data source directory",
							"Cannot find the full set of de.fu_berlin.imp.seqan.usability_analyzer.doclog.data in the provided log directory.",
							new Status(
									IStatus.ERROR,
									Activator.PLUGIN_ID,
									"The provided directory could not be read. Please check the configuration.",
									e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public SurveyRecordManager getSurveyRecordManager() {
		return surveyRecordManager;
	}

	public Mapper getMapper() {
		return this.mapper;
	}

	public EntityManager getPersonManager() {
		return this.entityManager;
	}

}
