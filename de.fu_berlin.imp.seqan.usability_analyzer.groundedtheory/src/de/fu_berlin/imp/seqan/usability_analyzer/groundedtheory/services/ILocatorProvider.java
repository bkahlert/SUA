package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.concurrent.Future;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public interface ILocatorProvider {

	/**
	 * Returns the {@link ILocatable} that is addressed by the given URI.
	 * 
	 * @param locatable
	 * @return
	 */
	public Future<ILocatable> getObject(URI uri);

	/**
	 * Shows / highlights the objects associated with the given {@link URI} in
	 * the active Eclipse Workbench.
	 * 
	 * @param uris
	 * @param open
	 *            if true also opens the instance
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public Future<Boolean> showInWorkspace(URI[] uris, boolean open);

}
