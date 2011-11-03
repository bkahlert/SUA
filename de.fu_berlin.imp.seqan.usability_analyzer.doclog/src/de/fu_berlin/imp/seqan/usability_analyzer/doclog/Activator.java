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

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private Logger logger = Logger.getLogger(Activator.class);

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog"; //$NON-NLS-1$

	private static Activator plugin;

	private DoclogManager doclogManager;
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

	public DoclogManager initDoclogManager(File logDirectory)
			throws DataSourceInvalidException {
		try {
			this.doclogManager = new DoclogManager(logDirectory);
			this.logger.info(DoclogManager.class.getSimpleName()
					+ " initiated successfully");
			return this.doclogManager;
		} catch (DataSourceInvalidException e) {
			this.logger.error(DoclogManager.class.getSimpleName()
					+ " could not be initiated", e);
			throw e;
		}
	}

	public DoclogManager getDoclogManager() {
		return this.doclogManager;
	}

	public Rectangle getMaxCaptureArea() {
		return maxCaptureArea;
	}

}
