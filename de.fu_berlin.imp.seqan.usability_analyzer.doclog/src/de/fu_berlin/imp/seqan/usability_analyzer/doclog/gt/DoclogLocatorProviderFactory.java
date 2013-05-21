package de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProviderFactory;

public class DoclogLocatorProviderFactory implements ILocatorProviderFactory {

	@Override
	public ILocatorProvider create() throws CoreException {
		return new DoclogLocatorProvider();
	}

}
