package de.fu_berlin.imp.seqan.usability_analyzer.diff;

import java.io.FileFilter;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.FileFilterUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Activator.class);

	// The shared instance
	private static Activator plugin;

	private SUADiffPreferenceUtil diffPreferenceUtil;

	private List<FileFilter> oldFileFilters;
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (diffPreferenceUtil.fileFilterPatternsChanged(event)) {
				List<FileFilter> newFileFilterPatterns = diffPreferenceUtil
						.getFileFilters();

				for (FileFilter newFileFilter : newFileFilterPatterns) {
					if (!oldFileFilters.contains(newFileFilter)) {
						FileFilterUtil.notifyFileFilterAdded(newFileFilter);
					}
				}

				for (FileFilter oldFileFilter : oldFileFilters) {
					if (!newFileFilterPatterns.contains(oldFileFilter)) {
						FileFilterUtil.notifyFileFilterRemoved(oldFileFilter);
					}
				}

				oldFileFilters = newFileFilterPatterns;
			}
		}
	};

	private DiffContainer diffContainer = null;

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

		new SUACorePreferenceUtil();
		diffPreferenceUtil = new SUADiffPreferenceUtil();
		oldFileFilters = diffPreferenceUtil.getFileFilters();
		diffPreferenceUtil.addPropertyChangeListener(propertyChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		diffPreferenceUtil.removePropertyChangeListener(propertyChangeListener);
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

	public DiffContainer getDiffDataDirectories() {
		return this.diffContainer;
	}

	public void setDiffDataDirectory(DiffContainer diffContainer) {
		this.diffContainer = diffContainer;
	}

}
