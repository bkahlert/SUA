package de.fu_berlin.imp.apiua.core.model;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.PlatformUI;

public class URIAdapterFactory implements IAdapterFactory {

	private static final Logger LOGGER = Logger
			.getLogger(URIAdapterFactory.class);

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ILocatable.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof URI) {
			final URI uri = (URI) adaptableObject;
			if (adapterType == ILocatable.class) {
				ILocatorService locatorService = (ILocatorService) PlatformUI
						.getWorkbench().getService(ILocatorService.class);
				if (locatorService != null) {
					try {
						return locatorService.resolve(uri, null).get();
					} catch (Exception e) {
						LOGGER.warn("Error resolving " + uri);
					}
				}
				return null;
			}
			return null;
		}
		return null;
	}

}
