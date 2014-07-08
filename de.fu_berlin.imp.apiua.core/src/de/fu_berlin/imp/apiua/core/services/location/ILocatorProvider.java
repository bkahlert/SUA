package de.fu_berlin.imp.apiua.core.services.location;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;

import org.eclipse.core.runtime.IProgressMonitor;

public interface ILocatorProvider {

	/**
	 * Returns <code>true</code> is the given {@link URI} definitely can't be
	 * resolved.
	 * <p>
	 * This method is useful if you plan to call the costly method
	 * {@link #getObject(URI, IProgressMonitor)}.
	 * 
	 * @param uri
	 *            to be checked
	 * 
	 * @return
	 */
	public boolean isResolvabilityImpossible(URI uri);

	/**
	 * Returns the type of the object that is addressed by the given URI.
	 * 
	 * @param uri
	 * @param monitor
	 * 
	 * @return
	 */
	public Class<? extends ILocatable> getType(URI uri);

	/**
	 * Returns <code>true</code> iff {@link #getObject(URI, IProgressMonitor)}
	 * is short running. Otherwise <code>false</code> is returned which means
	 * calling {@link #getObject(URI, IProgressMonitor)} is expensive and should
	 * be done in a separate thread.
	 * 
	 * @param uri
	 * @return
	 */
	public boolean getObjectIsShortRunning(URI uri);

	/**
	 * Returns the {@link ILocatable} that is addressed by the given URI.
	 * <p>
	 * Avoid calling this method in the UI thread if
	 * {@link #getObjectIsShortRunning(URI)} returned
	 * <code>false</false> since getting the object may take some time.
	 * 
	 * @param uri
	 * @param monitor
	 * 
	 * @return
	 */
	public ILocatable getObject(URI uri, IProgressMonitor monitor);

	/**
	 * Shows the given {@link ILocatable}s in the active Eclipse Workbench.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param locatable
	 * @param open
	 *            if true also opens the instance
	 * @param monitor
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public boolean showInWorkspace(URI[] uris, boolean open,
			IProgressMonitor monitor);

}
