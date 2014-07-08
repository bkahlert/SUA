package de.fu_berlin.imp.apiua.entity;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.apiua.entity.gt.EntityLabelProvider;
import de.fu_berlin.imp.apiua.entity.gt.EntityLocatorProvider;
import de.fu_berlin.imp.apiua.entity.model.EntityDataContainer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.apiua.entity"; //$NON-NLS-1$

	private static Logger LOGGER = null;

	private static Activator plugin;

	private ILabelProviderService labelProviderService = null;
	private ILabelProviderFactory labelProviderFactory = new ILabelProviderService.LocatablePathLabelProviderFactory(
			0, EntityLocatorProvider.ENTITY_NAMESPACE) {
		@Override
		protected ILabelProvider create() {
			return new EntityLabelProvider();
		}
	};

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

		if (LOGGER == null) {
			LOGGER = Logger.getLogger(Activator.class);
		}

		new SUACorePreferenceUtil();
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

	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * @return the loadedData
	 */
	public EntityDataContainer getLoadedData() {
		return this.loadedData;
	}

	/**
	 * @param entityDataContainer
	 *            the loadedData to set
	 */
	public void setLoadedData(EntityDataContainer entityDataContainer) {
		this.loadedData = entityDataContainer;
	}

}
