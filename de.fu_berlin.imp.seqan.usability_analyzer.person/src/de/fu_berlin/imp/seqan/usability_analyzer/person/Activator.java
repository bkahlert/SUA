package de.fu_berlin.imp.seqan.usability_analyzer.person;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.DoclogManager;
import de.fu_berlin.imp.seqan.usability_analyzer.person.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyRecordManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.person"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private SUACorePreferenceUtil preferenceUtil;

	private DiffFileManager diffFileManager;
	private DoclogManager doclogManager;
	private SurveyRecordManager surveyRecordManager;

	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private PersonManager personManager;

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

		this.preferenceUtil = new SUACorePreferenceUtil();
		File logfilePath = this.preferenceUtil.getLogfilePath();
		File surveyRecordPath = this.preferenceUtil.getSurveyRecordPath();

		try {
			this.diffFileManager = new DiffFileManager(logfilePath,
					new SUADiffPreferenceUtil().getTrunkDirectory());
			this.doclogManager = de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator
					.getDefault().initDoclogManager(logfilePath);
			this.surveyRecordManager = new SurveyRecordManager(surveyRecordPath);

			this.statsFileManager = new StatsFileManager(logfilePath);
			this.cMakeCacheFileManager = new CMakeCacheFileManager(logfilePath);

			this.mapper = new Mapper(this.doclogManager, logfilePath);
			this.personManager = new PersonManager(diffFileManager,
					doclogManager, surveyRecordManager, this.statsFileManager,
					this.cMakeCacheFileManager, mapper);
		} catch (DataSourceInvalidException e) {
			ErrorDialog
					.openError(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							"Data source directory",
							"Cannot find the full set of data in the provided log directory.",
							new Status(
									IStatus.ERROR,
									Activator.PLUGIN_ID,
									"The provided directory could not be read.",
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

	public DiffFileManager getDiffFileManager() {
		return diffFileManager;
	}

	public DoclogManager getDoclogManager() {
		return doclogManager;
	}

	public SurveyRecordManager getSurveyRecordManager() {
		return surveyRecordManager;
	}

	public Mapper getMapper() {
		return this.mapper;
	}

	public PersonManager getPersonManager() {
		return this.personManager;
	}

}
