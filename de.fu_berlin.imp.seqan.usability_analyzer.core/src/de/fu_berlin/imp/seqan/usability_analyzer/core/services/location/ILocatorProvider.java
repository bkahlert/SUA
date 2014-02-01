package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

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
	 * Returns the {@link ILocatable} that is addressed by the given URI.
	 * <p>
	 * Avoid calling this method in the UI thread since it may be be long
	 * running.
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
