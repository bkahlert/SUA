package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;

public interface ICodeService {

	public ICodeStore getCodeStore();

	/**
	 * Returns all {@link ICode}s associated with the given {@link URI}.
	 * 
	 * @param uri
	 * @return an empty list if no {@link ICode}s were found; never returns null
	 * @throws CodeServiceException
	 */
	public List<ICode> getCodes(URI uri) throws CodeServiceException;

	/**
	 * Registers a {@link ICodeServiceListener}
	 * 
	 * @param codeServiceListener
	 */
	public void addCodeServiceListener(ICodeServiceListener codeServiceListener);

	/**
	 * Unregisters a {@link ICodeServiceListener}
	 * 
	 * @param codeServiceListener
	 */
	public void removeCodeServiceListener(
			ICodeServiceListener codeServiceListener);

	/**
	 * Creates a {@link ICode} with the given caption.
	 * 
	 * @param caption
	 * @param color
	 * @return
	 * @throws CodeServiceException
	 */
	public ICode createCode(String caption, RGB color)
			throws CodeServiceException;

	/**
	 * Returns an existing {@link ICode} based on it's internal id
	 * 
	 * @param id
	 * @return
	 */
	public ICode getCode(long id);

	/**
	 * TODO
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param codeCaption
	 * @param rgb
	 * @param uri
	 * @return
	 * @throws CodeServiceException
	 */
	public ICode addCode(String codeCaption, RGB rgb, URI uri)
			throws CodeServiceException;

	/**
	 * Associates the {@link ILocatable} behind the given {@link URI} with the
	 * given {@link ICode}. Internally an {@link ICodeInstance} object is
	 * creates whose {@link URI} is returned.
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @param uri
	 * @throws CodeServiceException
	 */
	public URI addCode(ICode code, URI uri) throws CodeServiceException;

	public URI[] addCodes(List<ICode> codes, List<URI> uris)
			throws CodeServiceException;

	public Set<URI> getCodedIDs();

	/**
	 * Returns all existing {@link ICodeInstance}.
	 * 
	 * @return
	 */
	List<ICodeInstance> getInstances();

	/**
	 * Returns all {@link ICodeInstance}s belonging to {@link ILocatable}s of
	 * the given {@link IIdentifier}.
	 * <p>
	 * E.g. {@link ILocatable} belonging to ID 20x13b2.
	 * 
	 * @param identifier
	 * @return
	 */
	List<ICodeInstance> getInstances(IIdentifier identifier);

	/**
	 * Returns all direct {@link ICodeInstance}s of the given {@link ICode}.
	 * <p>
	 * If you also want to consider child {@link ICode}s see
	 * {@link #getAllInstances(ICode)}.
	 * 
	 * @param code
	 * @return
	 */
	public List<ICodeInstance> getInstances(ICode code);

	/**
	 * Returns all direct and indirect {@link ICodeInstance}s of the given
	 * {@link ICode}. This includes those of sub {@link ICode}.
	 * <p>
	 * If you only want to get immediate {@link ICodeInstance}s use
	 * {@link #getInstances(ICode)}.
	 * 
	 * @param code
	 * @return
	 */
	public Collection<? extends ICodeInstance> getAllInstances(ICode code);

	public void putInstances(ICode code, List<URI> uris);

	/**
	 * Renames a {@link ICode}
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @param newCaption
	 * @throws CodeServiceException
	 */
	public void renameCode(ICode code, String newCaption)
			throws CodeServiceException;

	/**
	 * Recolors a {@link ICode}
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @param newColor
	 * @throws CodeServiceException
	 */
	public void recolorCode(ICode code, RGB newColor)
			throws CodeServiceException;

	/**
	 * Sets a parent {@link ICode} for a given {@link ICode} allowing the
	 * modeling of hierarchies.
	 * 
	 * @param childNode
	 * @param parentNode
	 *            can be null if childNode should be a top level {@link ICode}
	 * @throws CodeServiceException
	 */
	public void setParent(ICode childNode, ICode parentNode)
			throws CodeServiceException;

	/**
	 * Removes a {@link ICode} from an {@link ILocatable}
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param codes
	 * @param uri
	 * @throws CodeServiceException
	 */
	public void removeCodes(List<ICode> codes, URI uri)
			throws CodeServiceException;

	/**
	 * Removes a {@link ICode} from all {@link ILocatable}s and deletes the
	 * {@link ICode} itself
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @throws CodeServiceException
	 */
	public void deleteCode(ICode code) throws CodeServiceException;

	/**
	 * Removes a {@link ICode} from all {@link ILocatable}s and deletes the
	 * {@link ICode} itself
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @param forceDelete
	 * @throws CodeServiceException
	 */
	public void deleteCode(ICode code, boolean forceDelete)
			throws CodeServiceException;

	public ICode getParent(ICode code);

	/**
	 * Returns all sub {@link ICode}s of the given {@link ICode} of depth 1.
	 * 
	 * @param code
	 * @return
	 */
	public List<ICode> getChildren(ICode code);

	/**
	 * Returns all sub {@link ICode}s of the given {@link ICode} of arbitrary
	 * depth.
	 * 
	 * @param code
	 * @return
	 */
	public List<ICode> getSubCodes(ICode code);

	public List<ICode> getTopLevelCodes();

	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeServiceException;

	/**
	 * Sets the memo for the given {@link URI}.
	 * 
	 * @param uri
	 * @param html
	 * @throws CodeServiceException
	 */
	public void setMemo(URI uri, String html) throws CodeServiceException;

	/**
	 * Returns the memo for the given {@link URI}.
	 * 
	 * @param uri
	 */
	public String loadMemo(URI uri);

	/**
	 * Returns true if the given {@link URI} has a memo.
	 * 
	 * @param uri
	 */
	public boolean isMemo(URI uri);

	/**
	 * Returns the {@link IIdentifier}s that have at least one {@link IEpisode}.
	 * 
	 * @return
	 */
	public List<IIdentifier> getEpisodedIdentifiers();

	/**
	 * Returns the {@link IEpisode}s associated to a given {@link IIdentifier}.
	 * 
	 * @param identifiers
	 * @return
	 */
	public Set<IEpisode> getEpisodes(IIdentifier identifiers);

	/**
	 * Adds an episode to the {@link ICodeStore}.
	 * 
	 * @param episode
	 * @throws CodeServiceException
	 */
	public void addEpisodeAndSave(IEpisode episode) throws CodeServiceException;

	public void replaceEpisodeAndSave(IEpisode oldEpisode, IEpisode newEpisode)
			throws CodeServiceException;

	public void deleteEpisodeAndSave(List<IEpisode> episodes)
			throws CodeServiceException;

	/**
	 * Reattaches all resources (codes and memos) from one to another
	 * {@link ILocatable}.
	 * 
	 * @param src
	 * @param dest
	 * @throws CodeServiceException
	 */
	public void reattachAndSave(URI src, URI dest) throws CodeServiceException;
}
