package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public interface ILocatorProvider {

	/**
	 * Returns a list of allowed namespaces.
	 * <p>
	 * <ul>
	 * <li>If an empty list is returned no namespaces are allowed.</li>
	 * <li>If <code>null</code> is returned all namespaces are allowed.</li>
	 * </ul>
	 * 
	 * @return
	 */
	public String[] getAllowedNamespaces();

	/**
	 * Returns the {@link ILocatable} that is addressed by the given URI.
	 * <p>
	 * This method is always called in a separate {@link Thread} and is allowed
	 * to be long running.
	 * 
	 * @param monitor
	 *            TODO
	 * @param locatable
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
	public boolean showInWorkspace(ILocatable[] locatables, boolean open,
			IProgressMonitor monitor);

}
