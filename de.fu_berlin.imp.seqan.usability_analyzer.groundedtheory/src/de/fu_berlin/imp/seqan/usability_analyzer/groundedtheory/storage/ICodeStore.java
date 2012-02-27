package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.security.InvalidParameterException;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

public interface ICodeStore {

	public ICode createCode(String caption) throws CodeStoreFullException;

	public ICodeInstance createCodeInstance(ICode code, ICodeable codeable)
			throws InvalidParameterException, CodeStoreReadException,
			DuplicateCodeInstanceException;

	public ICode[] loadCodes() throws CodeStoreReadException;

	public ICodeInstance[] loadCodeInstances() throws CodeStoreReadException;

	public void addAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeStoreReadException;

	public void saveCodes(ICode[] codes) throws CodeStoreReadException,
			CodeStoreWriteException;

	public void addAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException, CodeStoreReadException;

	public void saveCodeInstances(ICodeInstance[] codeInstances)
			throws CodeStoreReadException, CodeStoreWriteException;

	public void save(ICode[] codes, ICodeInstance[] codeInstances)
			throws CodeStoreWriteException;

	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeInstanceDoesNotExistException, CodeStoreWriteException,
			CodeStoreReadException;

	public void deleteCodeInstances(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException;

	public void deleteCode(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException, CodeDoesNotExistException;
}
