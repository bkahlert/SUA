package de.fu_berlin.imp.seqan.usability_analyzer.entity;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.EntityDataContainer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.entity"; //$NON-NLS-1$

	private static Logger LOGGER = null;

	private static Activator plugin;

	private EntityDataContainer loadedData = null;

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

		if (LOGGER == null)
			LOGGER = Logger.getLogger(Activator.class);

		new SUACorePreferenceUtil();
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

	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * @return the loadedData
	 */
	public EntityDataContainer getLoadedData() {
		return loadedData;
	}

	/**
	 * @param entityDataContainer
	 *            the loadedData to set
	 */
	public void setLoadedData(EntityDataContainer entityDataContainer) {
		this.loadedData = entityDataContainer;
	}

}
