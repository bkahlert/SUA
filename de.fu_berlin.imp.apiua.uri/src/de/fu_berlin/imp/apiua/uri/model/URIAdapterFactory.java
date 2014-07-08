package de.fu_berlin.imp.apiua.uri.model;

import de.fu_berlin.imp.apiua.core.model.URI;

import org.eclipse.core.runtime.IAdapterFactory;

public class URIAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { URI.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IUri) {
			final IUri uri = (IUri) adaptableObject;
			if (adapterType == URI.class) {
				return uri.getUri();
			}
			return null;
		}
		return null;
	}

}
