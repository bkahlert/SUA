package de.fu_berlin.imp.seqan.usability_analyzer.uri.services.impl;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import de.fu_berlin.imp.seqan.usability_analyzer.uri.services.IUriService;

public class UriServiceFactory extends AbstractServiceFactory implements
		ServiceFactory<IUriService> {

	private static final Logger LOGGER = Logger
			.getLogger(UriServiceFactory.class);

	private static IUriService URI_SERVICE;

	public UriServiceFactory() {
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface,
			IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface == IUriService.class) {
			if (URI_SERVICE == null) {
				try {
					URI_SERVICE = new UriService(this.getUriFile());
				} catch (IOException e) {
					LOGGER.error("Could not create " + IUriService.class, e);
				}
			}
			return URI_SERVICE;
		}

		return null;
	}

	@Override
	public IUriService getService(Bundle bundle,
			ServiceRegistration<IUriService> registration) {
		return null;
	}

	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<IUriService> registration, IUriService service) {
	}

	public File getUriFile() {
		return new File(new File(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString()), "uri.txt");
	}

}
