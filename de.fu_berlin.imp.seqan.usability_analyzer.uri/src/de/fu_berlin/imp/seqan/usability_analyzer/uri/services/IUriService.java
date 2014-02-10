package de.fu_berlin.imp.seqan.usability_analyzer.uri.services;

import java.util.Collection;
import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.IUri;

public interface IUriService {

	/**
	 * Returns all stored {@link IUri}s.
	 * 
	 * @return {@link Set} of {@link IUri}. Modifications to this {@link Set}
	 *         are *not* reflected in the internal data structure.
	 */
	public Set<IUri> getUris();

	/**
	 * Permanently adds the given {@link IUri}.
	 * 
	 * @param uri
	 * @return an empty list if no {@link ICode}s were found; never returns null
	 * @throws UriServiceException
	 */
	public void addUri(IUri uri) throws UriServiceException;

	/**
	 * Permanently removed the given {@link IUri}.
	 * 
	 * @throws UriServiceException
	 */
	public void removeUri(IUri uri) throws UriServiceException;

	/**
	 * Permanently replace the old {@link IUri} by a new one.
	 * 
	 * @throws UriServiceException
	 */
	public void replaceUri(IUri oldUri, IUri newUri) throws UriServiceException;

	/**
	 * Permanently removed the given {@link IUri}.
	 * 
	 * @param uris
	 */
	public void removeUris(Collection<IUri> uris) throws UriServiceException;

	/**
	 * Registers a {@link IUriServiceListener}
	 * 
	 * @param uriServiceListener
	 */
	public void addUriServiceListener(IUriServiceListener uriServiceListener);

	/**
	 * Unregisters a {@link IUriServiceListener}
	 * 
	 * @param uriServiceListener
	 */
	public void removeUriServiceListener(IUriServiceListener uriServiceListener);

}
