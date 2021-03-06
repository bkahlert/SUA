package de.fu_berlin.imp.apiua.diff.services;

import de.fu_berlin.imp.apiua.diff.model.ICompilable;

/**
 * This service provides information about {@link ICompilable}s.
 * 
 * @author bkahlert
 * 
 */
public interface ICompilationService {

	public void addCompilationServiceListener(
			ICompilationServiceListener compilationServiceListener);

	public void removeCompilationServiceListener(
			ICompilationServiceListener compilationServiceListener);

	/**
	 * Returns true if the {@link ICompilable} can be successfully compiled;
	 * false otherwise.
	 * 
	 * @param compilable
	 * @return null if the compilation state is unknown.
	 */
	public Boolean compiles(ICompilable compilable);

	/**
	 * Sets if a {@link ICompilable} can be successfully compiled.
	 * 
	 * @param compilables
	 * @param true if the {@link ICompilable} compiles, false if not; null if
	 *        the compilation state is unknown.
	 * @return true if the compilation state could be set; false otherwise
	 */
	public boolean compiles(ICompilable[] compilables, Boolean state);

	/**
	 * Returns the compiler output for the given {@link ICompilable}.
	 * 
	 * @param compilable
	 * @return
	 */
	public String compilerOutput(ICompilable compilable);

	/**
	 * Sets the given compiler output to the given {@link ICompilable}.
	 * 
	 * @param compilable
	 * @param html
	 */
	public void compilerOutput(ICompilable compilable, String html);

	/**
	 * Returns the output that is generated when the given {@link ICompilable}
	 * is executed.
	 * 
	 * @param compilable
	 * @return
	 */
	public String executionOutput(ICompilable compilable);

	/**
	 * Sets the output that is generated when the given {@link ICompilable} is
	 * executed.
	 * 
	 * @param compilable
	 * @param html
	 */
	public void executionOutput(ICompilable compilable, String string);
}
