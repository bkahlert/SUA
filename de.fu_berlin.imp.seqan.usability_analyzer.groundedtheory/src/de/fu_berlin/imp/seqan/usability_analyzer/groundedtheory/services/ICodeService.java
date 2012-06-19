package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;

public interface ICodeService {

	public ICodeStore getCodeStore();

	public List<ICode> getCodes(ICodeable codeable) throws CodeServiceException;

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
	 * @return
	 * @throws CodeServiceException
	 */
	public ICode createCode(String caption) throws CodeServiceException;

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
	 * @param codeable
	 * @return
	 * @throws CodeServiceException
	 */
	public ICode addCode(String codeCaption, ICodeable codeable)
			throws CodeServiceException;

	/**
	 * TODO
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @param codeable
	 * @return
	 * @throws CodeServiceException
	 */
	public ICode addCode(ICode code, ICodeable codeable)
			throws CodeServiceException;

	public Set<URI> getCodedIDs();

	public List<ICodeInstance> getInstances(ICode code);

	public void putInstances(ICode code, List<ICodeable> instances);

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
	 * Removes a {@link ICode} from an {@link ICodeable}
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @param codeable
	 * @throws CodeServiceException
	 */
	public void removeCode(ICode code, ICodeable codeable)
			throws CodeServiceException;

	/**
	 * Removes a {@link ICode} from all {@link ICodeable}s and deletes the
	 * {@link ICode} itself
	 * <p>
	 * This operation is broadcasted through {@link ICodeServiceListener}
	 * 
	 * @param code
	 * @throws CodeServiceException
	 */
	public void deleteCode(ICode code) throws CodeServiceException;

	/**
	 * Removes a {@link ICode} from all {@link ICodeable}s and deletes the
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

	/**
	 * @see {@link ICodeableProvider#getCodedObject(URI)}
	 */
	public ICodeable getCodedObject(URI codeInstanceID);

	/**
	 * @see {@link ICodeableProvider#showCodedObjectsInWorkspace(List)}
	 */
	public void showCodedObjectInWorkspace(URI codeInstanceID);

	/**
	 * @see {@link ICodeableProvider#showCodedObjectsInWorkspace(List)}
	 */
	public void showCodedObjectsInWorkspace(List<URI> codeInstanceIDs);

	/**
	 * @see {@link ICodeableProvider#getLabelProvider(String)}
	 */
	public ILabelProvider getLabelProvider(URI codeInstanceID);

	public ICode getParent(ICode code);

	public List<ICode> getChildren(ICode code);

	public List<ICode> getTopLevelCodes();

	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeServiceException;
}
