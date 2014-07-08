package de.fu_berlin.imp.apiua.diff.services.impl;

import org.apache.log4j.Logger;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import de.fu_berlin.imp.apiua.diff.services.ICompilationService;

public class CompilationServiceFactory extends AbstractServiceFactory implements
		ServiceFactory<ICompilationService> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ICompilationService.class);

	private static ICompilationService COMPLIATION_SERVICE;

	public CompilationServiceFactory() {
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface,
			IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface == ICompilationService.class) {
			if (COMPLIATION_SERVICE == null) {
				COMPLIATION_SERVICE = new CompilationService();
			}
			return COMPLIATION_SERVICE;
		}

		return null;
	}

	@Override
	public ICompilationService getService(Bundle bundle,
			ServiceRegistration<ICompilationService> registration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<ICompilationService> registration,
			ICompilationService service) {
		// TODO Auto-generated method stub

	}
}
