package de.fu_berlin.imp.seqan.usability_analyzer.diff.services;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;

public interface ICompilationService {
	/**
	 * Returns true if the {@link ICompilable} can be successfully compiled;
	 * false otherwise.
	 * 
	 * @param compilable
	 * @return null if the compilation state is unknown.
	 */
	public Boolean compiles(ICompilable compilable);
}
