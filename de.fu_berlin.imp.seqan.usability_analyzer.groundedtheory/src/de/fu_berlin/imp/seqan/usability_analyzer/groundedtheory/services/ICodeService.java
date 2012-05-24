package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;

public interface ICodeService {

	public ICodeStore getCodeStore();

	public List<ICode> getCodes(ICodeable codeable) throws CodeServiceException;

	public void addCodeServiceListener(ICodeServiceListener iCodeServiceListener);

	public void removeCodeServiceListener(
			ICodeServiceListener iCodeServiceListener);

	public ICode addCode(String codeCaption, ICodeable codeable)
			throws CodeServiceException;

	public ICode addCode(ICode code, ICodeable codeable)
			throws CodeServiceException;

	public List<ICodeInstance> getInstances(ICode code);

	public void putInstances(ICode code, List<ICodeable> instances);

	/**
	 * Removes an {@link ICode} from an {@link ICodeable}
	 * 
	 * @param code
	 * @param codeable
	 * @throws CodeServiceException
	 */
	public void removeCode(ICode code, ICodeable codeable)
			throws CodeServiceException;

	/**
	 * Removes an {@link ICode} from all {@link ICodeable}s and deletes the
	 * {@link ICode} itself
	 * 
	 * @param code
	 * @throws CodeServiceException
	 */
	public void deleteCode(ICode code) throws CodeServiceException;

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
}
