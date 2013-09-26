package de.fu_berlin.imp.seqan.usability_analyzer.doclog;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.DoclogLabelProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog"; //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Activator.class);

	private static Activator plugin;

	private ILabelProviderService labelProviderService = null;
	private ILabelProviderFactory labelProviderFactory = new ILabelProviderService.LocatablePathLabelProviderFactory(
			0, "doclog") {
		@Override
		protected ILabelProvider create() {
			return new DoclogLabelProvider();
		}
	};

	private DoclogDataContainer doclogDataContainer = null;

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

	public DoclogDataContainer getDoclogContainer() {
		return this.doclogDataContainer;
	}

	public void setDoclogDataDirectory(DoclogDataContainer doclogDataContainer) {
		this.doclogDataContainer = doclogDataContainer;
	}

}
