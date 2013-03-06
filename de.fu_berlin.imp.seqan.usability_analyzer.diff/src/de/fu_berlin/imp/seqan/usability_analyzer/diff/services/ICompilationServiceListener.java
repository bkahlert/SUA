package de.fu_berlin.imp.seqan.usability_analyzer.diff.services;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;

/**
 * Notifies about changes concerning the compilation state of
 * {@link ICompilable}s.
 * 
 * @author bkahlert
 * 
 */
public interface ICompilationServiceListener {
	/**
	 * Gets called if the compilation state of some {@link ICompilable}s
	 * changed.
	 * 
	 * @param compilables
	 * @param state
	 */
	public void compilationStateChanged(ICompilable[] compilables, Boolean state);

	/**
	 * Gets called if the compiler output of a {@link ICompilable} changed.
	 * 
	 * @param compilable
	 * @param html
	 */
	public void compilerOutputChanged(ICompilable compilable, String html);
}
