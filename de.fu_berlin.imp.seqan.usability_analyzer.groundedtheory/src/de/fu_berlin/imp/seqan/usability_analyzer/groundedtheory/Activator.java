package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.GTLabelProvider;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory"; //$NON-NLS-1$
	private static Activator plugin;

	private ILabelProviderService labelProviderService = null;
	private final ILabelProviderFactory labelProviderFactory = new ILabelProviderService.LocatablePathLabelProviderFactory(
			0,
			new String[] {
					"code",
					"codeinstance",
					"episode",
					AxialCodingModelLocatorProvider.AXIAL_CODING_MODEL_NAMESPACE }) {
		@Override
		protected ILabelProvider create() {
			return new GTLabelProvider();
		}
	};

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		this.labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		this.labelProviderService
				.addLabelProviderFactory(this.labelProviderFactory);
	}

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
}
