package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public interface ICodeableProviderFactory extends IExecutableExtensionFactory {
	@Override
	public ILocatorProvider create() throws CoreException;
}
