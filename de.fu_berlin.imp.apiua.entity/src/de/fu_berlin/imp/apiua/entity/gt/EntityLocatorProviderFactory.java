package de.fu_berlin.imp.apiua.entity.gt;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.imp.apiua.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorProviderFactory;

public class EntityLocatorProviderFactory implements ILocatorProviderFactory {

	@Override
	public ILocatorProvider create() throws CoreException {
		return new EntityLocatorProvider();
	}

}
