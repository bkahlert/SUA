package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;

public class ServiceFactory extends AbstractServiceFactory {

	private static final Logger LOGGER = Logger.getLogger(ServiceFactory.class);

	private static IWorkSessionService WORKSESSION_SERVICE;

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

		return null;
	}
}
