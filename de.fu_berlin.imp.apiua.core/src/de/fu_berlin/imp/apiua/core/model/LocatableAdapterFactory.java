package de.fu_berlin.imp.apiua.core.model;

import de.fu_berlin.imp.apiua.core.model.URI;

import org.eclipse.core.runtime.IAdapterFactory;

public class LocatableAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { URI.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ILocatable) {
			final ILocatable locatable = (ILocatable) adaptableObject;
			if (adapterType == URI.class) {
				return locatable.getUri();
			}
			return null;
		}
		return null;
	}

}
