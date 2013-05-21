package de.fu_berlin.imp.seqan.usability_analyzer.diff.gt;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeableProviderFactory;

public class DiffCodeableProviderFactory implements ICodeableProviderFactory {

	@Override
	public ILocatorProvider create() throws CoreException {
		return new DiffCodeableProvider();
	}

}
