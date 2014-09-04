package de.fu_berlin.imp.apiua.groundedtheory.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.bkahlert.nebula.utils.Triple;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IllegalDimensionValueException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

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
	 * Returns all {@link ICodeInstance}s belonging to an phaenomenon
	 * {@link URI}. In other words: If uri is associated with c
	 * {@link ICodeInstance#getId()} equals p.
	 * 
	 * @param code
	 * @return
	 */
	public List<ICodeInstance> getInstances(URI uri);

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
	 * Returns the memo in plain text for the given {@link URI}.
	 * 
	 * @param uri
	 */
	public String loadMemoPlain(URI uri);

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
	 * Returns the {@link ICode}'s current {@link IDimension}.
	 * 
	 * @param uri
	 * @return <code>null</code> if no {@link IDimension} is set
	 */
	public IDimension getDimension(URI uri);

	/**
	 * Sets the {@link ICode}'s current {@link IDimension}.
	 * <p>
	 * <strong>Note:</strong>This automatically resets the currently set value.
	 * 
	 * @param code
	 * @param dimension
	 * @throws CodeStoreWriteException
	 */
	public void setDimension(ICode code, IDimension dimension)
			throws CodeStoreWriteException;

	/**
	 * Returns the {@link URI}'s set {@link IDimension} value.
	 * 
	 * @param uri
	 * @param code
	 * @return
	 */
	public String getDimensionValue(URI uri, ICode code);

	/**
	 * Returns the all {@link IDimension} values associated with the given
	 * {@link URI}.
	 * 
	 * @param codeInstance
	 * @return a list of triples. Each triple consists of
	 *         <ol>
	 *         <li>{@link URI} the {@link IDimension} belongs to</li>
	 *         <li>the {@link IDimension} the value belongs to</li>
	 *         <li>the {@link IDimension} value itself</li>
	 *         </ol>
	 * @throws CodeServiceException
	 */
	public List<Triple<URI, IDimension, String>> getDimensionValues(
			ICodeInstance codeInstance) throws CodeServiceException;

	/**
	 * Set's the {@link IDimensionable}'s {@link IDimension} value.
	 * 
	 * @param uri
	 * @param code
	 * @param value
	 * @throws IllegalDimensionValueException
	 *             is thrown if no {@link IDimension} is set or is incompatible
	 *             with the current {@link IDimension}.
	 * @throws CodeStoreWriteException
	 */
	public void setDimensionValue(URI uri, ICode code, String value)
			throws IllegalDimensionValueException, CodeStoreWriteException;

	/**
	 * Returns the {@link ICode}s that serve as properties for the given
	 * {@link ICode}.
	 * 
	 * @param code
	 * @return
	 */
	public List<ICode> getProperties(ICode code);

	/**
	 * Sets the given {@link ICode}s as the properties to the given
	 * {@link ICode}.
	 * 
	 * @param code
	 * @param properties
	 * @throws CodeStoreWriteException
	 */
	public void setProperties(ICode code, List<ICode> properties)
			throws CodeStoreWriteException;

	/**
	 * Add the given {@link ICode} as a property to the given {@link ICode}.
	 * 
	 * @param code
	 * @param property
	 * @throws CodeStoreWriteException
	 */
	public void addProperty(ICode code, ICode property)
			throws CodeStoreWriteException;

	/**
	 * Removes the given {@link ICode} as a property from the given
	 * {@link ICode}.
	 * 
	 * @param code
	 * @param property
	 * @throws CodeStoreWriteException
	 */
	public void removeProperty(ICode code, ICode property)
			throws CodeStoreWriteException;

	/**
	 * Gets all existing {@link IAxialCodingModel} from the {@link ICodeStore}.
	 * 
	 * @return
	 * 
	 * @throws CodeStoreWriteException
	 */
	public List<URI> getAxialCodingModels() throws CodeStoreReadException;

	/**
	 * Adds a new {@link IAxialCodingModel} to the {@link ICodeStore}. If a
	 * {@link IAxialCodingModel} has the same {@link URI} it replaces the
	 * already set model.
	 * 
	 * @param axialCodingModel
	 * @throws CodeStoreWriteException
	 */
	public void addAxialCodingModel(IAxialCodingModel axialCodingModel)
			throws CodeStoreWriteException;

	/**
	 * Removes a given {@link IAxialCodingModel} from the {@link ICodeStore} and
	 * returns it.
	 * 
	 * @param uri
	 * @return
	 * @throws CodeStoreWriteException
	 */
	public void removeAxialCodingModel(URI uri) throws CodeStoreWriteException;

	/**
	 * Gets a given {@link IAxialCodingModel} from the {@link ICodeStore}.
	 * 
	 * @param uri
	 * @return
	 * @throws CodeStoreReadException
	 */
	public IAxialCodingModel getAxialCodingModel(URI uri)
			throws CodeStoreReadException;

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
