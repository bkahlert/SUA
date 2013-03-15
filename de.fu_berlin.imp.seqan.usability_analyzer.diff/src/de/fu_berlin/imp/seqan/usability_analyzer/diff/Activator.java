package de.fu_berlin.imp.seqan.usability_analyzer.diff;

import java.io.FileFilter;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.FileFilterUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.DiffLabelProvider;

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

	private ILabelProviderService labelProviderService = null;
	private ILabelProviderFactory labelProviderFactory = new ILabelProviderService.URIPathLabelProviderFactory(
			0, "diff") {
		@Override
		protected ILabelProvider create() {
			return new DiffLabelProvider();
		}
	};

	private SUADiffPreferenceUtil diffPreferenceUtil;

	private List<FileFilter> oldFileFilters;
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (Activator.this.diffPreferenceUtil
					.fileFilterPatternsChanged(event)) {
				List<FileFilter> newFileFilterPatterns = Activator.this.diffPreferenceUtil
						.getFileFilters();

				for (FileFilter newFileFilter : newFileFilterPatterns) {
					if (!Activator.this.oldFileFilters.contains(newFileFilter)) {
						FileFilterUtil.notifyFileFilterAdded(newFileFilter);
					}
				}

				for (FileFilter oldFileFilter : Activator.this.oldFileFilters) {
					if (!newFileFilterPatterns.contains(oldFileFilter)) {
						FileFilterUtil.notifyFileFilterRemoved(oldFileFilter);
					}
				}

				Activator.this.oldFileFilters = newFileFilterPatterns;
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
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		this.labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		this.labelProviderService
				.addLabelProviderFactory(this.labelProviderFactory);

		URL confURL = this.getBundle().getEntry("log4j.properties");
		PropertyConfigurator
				.configure(FileLocator.toFileURL(confURL).getFile());

		new SUACorePreferenceUtil();
		this.diffPreferenceUtil = new SUADiffPreferenceUtil();
		this.oldFileFilters = this.diffPreferenceUtil.getFileFilters();
		this.diffPreferenceUtil
				.addPropertyChangeListener(this.propertyChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		this.diffPreferenceUtil
				.removePropertyChangeListener(this.propertyChangeListener);

		this.labelProviderService
				.removeLabelProviderFactory(this.labelProviderFactory);
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

	public DiffContainer getDiffDataContainer() {
		return this.diffContainer;
	}

	public void setDiffDataDirectory(DiffContainer diffContainer) {
		this.diffContainer = diffContainer;
	}

}
