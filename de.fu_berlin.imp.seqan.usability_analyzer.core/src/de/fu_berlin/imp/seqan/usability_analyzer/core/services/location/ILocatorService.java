package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;

/**
 * Instances of this class can be part of an {@link IWorkSession}.
 * 
 * @author bkahlert
 * 
 */
public interface ILocatorService {

	/**
	 * Returns the {@link ILocatable} that is addressed by the given {@link URI}
	 * .
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param uri
	 * @param monitor
	 * @return
	 */
	public Future<ILocatable> resolve(URI uri, IProgressMonitor monitor);

	/**
	 * Returns the {@link ILocatable}s that are addressed by the given
	 * {@link URI}s.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param uris
	 * @param monitor
	 * @return
	 */
	public Future<ILocatable[]> resolve(URI[] uris, IProgressMonitor monitor);

	/**
	 * Removes an eventually cached resolve attempt.
	 * <p>
	 * This method must be called if a the identified object for a given
	 * {@link URI} changed.
	 * 
	 * @param uri
	 */
	public void uncache(URI uri);

	/**
	 * Removes an eventually cached resolve attempt.
	 * <p>
	 * This method must be called if a the identified object for a given
	 * {@link URI} changed.
	 * 
	 * @param uris
	 */
	public void uncache(URI[] uris);

	/**
	 * Shows the object associated with the given {@link URI} in the active
	 * Eclipse Workbench.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param uris
	 * @param open
	 *            if true also opens the instance
	 * @param monitor
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public Future<Boolean> showInWorkspace(URI uri, boolean open,
			IProgressMonitor monitor);

	/**
	 * Shows the objects associated with the given {@link URI}s in the active
	 * Eclipse Workbench.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param uris
	 * @param open
	 *            if true also opens the instance
	 * @param monitor
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public Future<Boolean> showInWorkspace(URI[] uris, boolean open,
			IProgressMonitor monitor);

	/**
	 * Shows the given {@link ILocatable} in the active Eclipse Workbench.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param uris
	 * @param open
	 *            if true also opens the instance
	 * @param monitor
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public Future<Boolean> showInWorkspace(ILocatable locatable, boolean open,
			IProgressMonitor monitor);

	/**
	 * Shows the given {@link ILocatable}s in the active Eclipse Workbench.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param uris
	 * @param open
	 *            if true also opens the instance
	 * @param monitor
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public Future<Boolean> showInWorkspace(ILocatable[] locatables,
			boolean open, IProgressMonitor monitor);

}
