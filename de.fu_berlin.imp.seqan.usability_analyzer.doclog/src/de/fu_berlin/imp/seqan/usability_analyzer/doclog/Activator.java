package de.fu_berlin.imp.seqan.usability_analyzer.doclog;

import java.awt.AWTException;
import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.bkahlert.devel.web.screenshots.ScreenshotTaker;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog"; //$NON-NLS-1$

	private static Activator plugin;

	private DoclogDirectory doclogDirectory;
	private Rectangle maxCaptureArea;

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

		SUACorePreferenceUtil corePreferenceUtil = new SUACorePreferenceUtil();
		File logDirectory = corePreferenceUtil.getLogDirectory();
		if (logDirectory != null && logDirectory.isDirectory()
				&& logDirectory.canRead()) {
			doclogDirectory = new DoclogDirectory(logDirectory);
			doclogDirectory.scan();
		} else {
			logger.warn("No valid log directory specified");
		}

		try {
			this.maxCaptureArea = new ScreenshotTaker().getMaxCaptureArea();
		} catch (PartInitException e1) {
			logger.fatal(e1);
		} catch (AWTException e1) {
			logger.fatal(e1);
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

	public DoclogDirectory getDoclogDirectory() {
		return this.doclogDirectory;
	}

	public Rectangle getMaxCaptureArea() {
		return maxCaptureArea;
	}

}
