package de.fu_berlin.imp.apiua.core.services.impl;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import de.fu_berlin.imp.apiua.core.Activator;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IHighlightService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.core.services.IWorkSessionService;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.services.location.impl.ImportanceService;
import de.fu_berlin.imp.apiua.core.services.location.impl.LocatorService;

public class ServiceFactory extends AbstractServiceFactory {

	private static final Logger LOGGER = Logger.getLogger(ServiceFactory.class);

	private static IWorkSessionService WORKSESSION_SERVICE;
	private static IDataService DATA_DIRECTORIES_SERVICE;
	private static IHighlightService HIGHLIGHT_SERVICE;
	private static ILabelProviderService LABELPROVIDER_SERVICE;
	private static IUriPresenterService URIPRESENTER_SERVICE;
	private static ILocatorService LOCATOR_SERVICE;
	private static IImportanceService IMPORTANCE_SERVICE;

	public ServiceFactory() {
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface,
			IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface == IWorkSessionService.class) {
			if (WORKSESSION_SERVICE == null) {
				try {
					WORKSESSION_SERVICE = new WorkSessionService();
				} catch (IOException e) {
					LOGGER.error("Could not create "
							+ IWorkSessionService.class, e);
				}
			}
			return WORKSESSION_SERVICE;
		}
		if (serviceInterface == IDataService.class) {
			if (DATA_DIRECTORIES_SERVICE == null) {
				DATA_DIRECTORIES_SERVICE = new DataService();
				Activator.getDefault().getBundle().getBundleContext()
						.addBundleListener(new BundleListener() {
							private boolean disposed = false;

							@Override
							synchronized public void bundleChanged(
									BundleEvent event) {
								if (!this.disposed
										&& event.getType() == BundleEvent.STOPPED) {
									DATA_DIRECTORIES_SERVICE.unloadData();
									this.disposed = true;
								}
							}
						});
			}
			return DATA_DIRECTORIES_SERVICE;
		}
		if (serviceInterface == IHighlightService.class) {
			if (HIGHLIGHT_SERVICE == null) {
				HIGHLIGHT_SERVICE = new HighlightService();
			}
			return HIGHLIGHT_SERVICE;
		}
		if (serviceInterface == ILabelProviderService.class) {
			if (LABELPROVIDER_SERVICE == null) {
				LABELPROVIDER_SERVICE = new LabelProviderService();
			}
			return LABELPROVIDER_SERVICE;
		}

		if (serviceInterface == IUriPresenterService.class) {
			if (URIPRESENTER_SERVICE == null) {
				URIPRESENTER_SERVICE = new UriPresenterService();
			}
			return URIPRESENTER_SERVICE;
		}

		if (serviceInterface == ILocatorService.class) {
			if (LOCATOR_SERVICE == null) {
				LOCATOR_SERVICE = new LocatorService();
			}
			return LOCATOR_SERVICE;
		}

		if (serviceInterface == IImportanceService.class) {
			if (IMPORTANCE_SERVICE == null) {
				try {
					IMPORTANCE_SERVICE = new ImportanceService(
							this.getImportanceFile());
				} catch (IOException e) {
					LOGGER.error("Error creating " + IImportanceService.class,
							e);
				}
			}
			return IMPORTANCE_SERVICE;
		}

		return null;
	}

	public File getImportanceFile() {
		return new File(new File(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString()), "importance.txt");
	}
}
