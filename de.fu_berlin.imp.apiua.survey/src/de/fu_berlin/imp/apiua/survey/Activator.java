package de.fu_berlin.imp.apiua.survey;

import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.apiua.survey.model.SurveyContainer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in DateId
	public static final String PLUGIN_ID = "de.fu_berlin.imp.apiua.survey"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ILabelProviderService labelProviderService = null;
	private final ILabelProviderFactory labelProviderFactory = new ILabelProviderService.LocatablePathLabelProviderFactory(
			0, SurveyLocatorProvider.SURVEY_NAMESPACE) {
		@Override
		protected ILabelProvider create() {
			return new SurveyLabelProvider();
		}
	};

	private SurveyContainer surveyContainer = null;

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

	public void setSurveyContainer(SurveyContainer surveyContainer) {
		this.surveyContainer = surveyContainer;
	}

	public SurveyContainer getSurveyContainer() {
		return this.surveyContainer;
	}

}
