package de.fu_berlin.imp.apiua.doclog.gt;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.imp.apiua.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorProviderFactory;

public class DoclogLocatorProviderFactory implements ILocatorProviderFactory {

	@Override
	public ILocatorProvider create() throws CoreException {
		return new DoclogLocatorProvider();
	}

}
