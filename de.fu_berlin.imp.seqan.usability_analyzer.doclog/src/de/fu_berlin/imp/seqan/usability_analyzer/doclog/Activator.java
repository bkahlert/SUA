package de.fu_berlin.imp.seqan.usability_analyzer.doclog;

import java.awt.AWTException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.bkahlert.devel.web.screenshots.ScreenshotTaker;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataDirectory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog"; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger(Activator.class);

	private static Activator plugin;

	private Rectangle maxCaptureArea;

	private DoclogDataDirectory doclogDataDirectory = null;

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

		try {
			this.maxCaptureArea = new ScreenshotTaker().getMaxCaptureArea();
		} catch (PartInitException e) {
			LOGGER.fatal(e);
		} catch (AWTException e) {
			LOGGER.fatal(e);
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

	public Rectangle getMaxCaptureArea() {
		return maxCaptureArea;
	}

	public DoclogDataDirectory getDoclogContainer() {
		return doclogDataDirectory;
	}

	public void setDoclogDataDirectory(DoclogDataDirectory doclogDataDirectory) {
		this.doclogDataDirectory = doclogDataDirectory;
	}

}
