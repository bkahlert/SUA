package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

class CodeService implements ICodeService {

	private ICodeStore codeStore;
	private CodeServiceListenerNotifier codeServiceListenerNotifier;

	public CodeService(ICodeStore codeStore) throws IOException {
		Assert.isNotNull(codeStore);
		this.codeStore = codeStore;
		this.codeServiceListenerNotifier = new CodeServiceListenerNotifier();
	}

	@Override
	public void addCodeServiceListener(CodeServiceListener codeServiceListener) {
		codeServiceListenerNotifier.addCodeServiceListener(codeServiceListener);
	}

	@Override
	public void removeCodeServiceListener(
			CodeServiceListener codeServiceListener) {
		codeServiceListenerNotifier
				.removeCodeServiceListener(codeServiceListener);
	}

	@Override
	public ICodeStore getCodeStore() {
		return this.codeStore;
	}

	@Override
	public List<ICode> getCodes(ICodeable codeable) throws CodeServiceException {
		LinkedList<ICode> codes = new LinkedList<ICode>();
		try {
			for (ICodeInstance codeInstance : this.codeStore
					.loadCodeInstances()) {
				if (codeInstance.getId().equals(codeable.getCodeInstanceId())) {
					codes.add(codeInstance.getCode());
				}
			}
		} catch (CodeStoreReadException e) {
			throw new CodeServiceException(e);
		}
		String codeInstanceId = codeable.getCodeInstanceId();
		if ("0xigg7z3soutj46b".equals(codeInstanceId)) {
			codes.add(new Code(1l, "Ich bin ein Code"));
		}
		return codes;
	}

	public ICode addCode(String codeCaption, ICodeable codeable)
			throws CodeServiceException {
		try {
			ICode code = codeStore.createCode(codeCaption);
			addCode(code, codeable);
			return code;
		} catch (CodeStoreFullException e) {
			throw new CodeServiceException(e);
		}
	}

	@SuppressWarnings("serial")
	public ICode addCode(ICode code, final ICodeable codeable)
			throws CodeServiceException {
		try {
			if (!ArrayUtils.contains(codeStore.loadCodes(), code)) {
				codeStore.addAndSaveCode(code);
				codeServiceListenerNotifier.codeCreated(code);
			}
			ICodeInstance codeInstance = codeStore.createCodeInstance(code,
					codeable);
			codeStore.addAndSaveCodeInstance(codeInstance);
			codeServiceListenerNotifier.codeAssigned(code,
					new ArrayList<ICodeable>() {
						{
							add(codeable);
						}
					});
			return code;
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreReadException e) {
			throw new CodeServiceException(e);
		} catch (DuplicateCodeInstanceException e) {
			throw new CodeServiceException(e);
		}

	}

	@Override
	public List<ICodeInstance> getInstances(ICode code) {
		try {
			ArrayList<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
			for (ICodeInstance codeInstance : codeStore.loadCodeInstances()) {
				if (codeInstance.getCode().equals(code))
					codeInstances.add(codeInstance);
			}
			return codeInstances;
		} catch (CodeStoreReadException e) {
			return null;
		}
	}

	@Override
	public void putInstances(ICode code, List<ICodeable> instances) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("serial")
	@Override
	public void removeCode(ICode code, final ICodeable codeable)
			throws CodeServiceException {
		try {
			int numRemoved = 0;
			for (ICodeInstance codeInstance : this.codeStore
					.loadCodeInstances()) {
				if (codeInstance.getCode().equals(code)
						&& codeInstance.getId().equals(
								codeable.getCodeInstanceId())) {
					this.codeStore.deleteCodeInstance(codeInstance);
					codeServiceListenerNotifier.codeRemoved(code,
							new ArrayList<ICodeable>() {
								{
									add(codeable);
								}
							});
					numRemoved++;
				}
			}
			if (numRemoved == 0)
				throw new CodeInstanceDoesNotExistException();
		} catch (CodeStoreReadException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeInstanceDoesNotExistException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void deleteCode(ICode code) throws CodeServiceException {
		try {
			this.codeStore.deleteCode(code);
			codeServiceListenerNotifier.codeDeleted(code);
		} catch (CodeStoreReadException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeDoesNotExistException e) {
			throw new CodeServiceException(e);
		}
	}

}
