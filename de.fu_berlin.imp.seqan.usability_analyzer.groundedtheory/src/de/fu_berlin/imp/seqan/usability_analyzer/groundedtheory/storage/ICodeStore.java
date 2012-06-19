package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeHasChildCodesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

public interface ICodeStore {

	public List<ICode> getTopLevelCodes();

	public Set<ICodeInstance> loadInstances();

	/**
	 * Returns an existing {@link ICode} based on it's internal id
	 * 
	 * @param id
	 * @return
	 */
	public ICode getCode(long id);

	public ICode createCode(String caption) throws CodeStoreFullException;

	public ICodeInstance createCodeInstance(ICode code, ICodeable codeable)
			throws InvalidParameterException, CodeStoreReadException,
			DuplicateCodeInstanceException;

	public void addAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeStoreReadException;

	public void addAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException, CodeStoreReadException;

	public void removeAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeHasChildCodesException, CodeDoesNotExistException;

	public void removeAndSaveCode(ICode code, boolean deleteInstances)
			throws CodeStoreWriteException, CodeHasChildCodesException,
			CodeDoesNotExistException;

	public void removeAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException, CodeStoreReadException;

	public void save() throws CodeStoreWriteException;

	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeInstanceDoesNotExistException, CodeStoreWriteException;

	public void deleteCodeInstances(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException;

	public ICode getParent(ICode code);

	public ICode setParent(ICode childNode, ICode newParentNode)
			throws CodeDoesNotExistException, CodeStoreWriteException;

	public List<ICode> getChildren(ICode code);

	public boolean codeExists(ICode code);
}
