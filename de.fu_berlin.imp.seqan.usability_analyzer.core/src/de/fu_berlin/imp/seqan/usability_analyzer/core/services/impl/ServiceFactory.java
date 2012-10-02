package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;

public class ServiceFactory extends AbstractServiceFactory {

	private static final Logger LOGGER = Logger.getLogger(ServiceFactory.class);

	private static IWorkSessionService WORKSESSION_SERVICE;
	private static IDataService DATA_DIRECTORIES_SERVICE;

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
								if (!disposed
										&& event.getType() == BundleEvent.STOPPED) {
									DATA_DIRECTORIES_SERVICE.dispose();
									disposed = true;
								}
							}
						});
			}
			return DATA_DIRECTORIES_SERVICE;
		}

		return null;
	}
}
