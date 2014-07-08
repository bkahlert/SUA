package de.fu_berlin.imp.apiua.core.services.location;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;


public interface ILocatorProviderFactory extends IExecutableExtensionFactory {
	@Override
	public ILocatorProvider create() throws CoreException;
}
