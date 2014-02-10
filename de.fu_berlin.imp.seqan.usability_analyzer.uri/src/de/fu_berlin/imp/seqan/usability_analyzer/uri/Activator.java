package de.fu_berlin.imp.seqan.usability_analyzer.uri;

import java.net.URI;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.viewers.UriLabelProvider;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.uri"; //$NON-NLS-1$

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private UriLocatorProvider uriLocatorProvider = new UriLocatorProvider();

	private ILabelProviderService labelProviderService = null;
	private ILabelProviderFactory labelProviderFactory = new ILabelProviderFactory() {
		@Override
		public ILabelProvider createFor(URI uri) {
			if (!Activator.this.uriLocatorProvider
					.isResolvabilityImpossible(uri)
					&& Activator.this.uriLocatorProvider.getObject(uri, null) != null) {
				return new UriLabelProvider();
			} else {
				return null;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		URL confURL = bundleContext.getBundle().getEntry("log4j.properties");
		PropertyConfigurator
				.configure(FileLocator.toFileURL(confURL).getFile());

		this.labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		this.labelProviderService
				.addLabelProviderFactory(this.labelProviderFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		this.labelProviderService
				.removeLabelProviderFactory(this.labelProviderFactory);

		Activator.context = null;
	}

}
