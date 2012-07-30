package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeHasChildCodesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

class CodeService implements ICodeService {

	private static final Logger LOGGER = Logger.getLogger(CodeService.class);

	private ICodeStore codeStore;
	private CodeServiceListenerNotifier codeServiceListenerNotifier;

	public CodeService(ICodeStore codeStore) throws IOException {
		Assert.isNotNull(codeStore);
		this.codeStore = codeStore;
		this.codeServiceListenerNotifier = new CodeServiceListenerNotifier();
	}

	@Override
	public void addCodeServiceListener(ICodeServiceListener codeServiceListener) {
		codeServiceListenerNotifier.addCodeServiceListener(codeServiceListener);
	}

	@Override
	public void removeCodeServiceListener(
			ICodeServiceListener codeServiceListener) {
		codeServiceListenerNotifier
				.removeCodeServiceListener(codeServiceListener);
	}

	@Override
	public ICodeStore getCodeStore() {
		return this.codeStore;
	}

	@Override
	public ICode createCode(String caption) throws CodeServiceException {
		ICode code;
		try {
			code = codeStore.createCode(caption);
		} catch (CodeStoreFullException e) {
			throw new CodeServiceException(e);
		}
		codeServiceListenerNotifier.codesCreated(Arrays.asList(code));
		return code;
	}

	@Override
	public ICode getCode(long id) {
		return this.codeStore.getCode(id);
	}

	@Override
	public List<ICode> getCodes(ICodeable codeable) throws CodeServiceException {
		LinkedList<ICode> codes = new LinkedList<ICode>();
		for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
			if (codeInstance.getId().equals(codeable.getCodeInstanceID())) {
				codes.add(codeInstance.getCode());
			}
		}
		return codes;
	}

	public ICode addCode(String codeCaption, ICodeable codeable)
			throws CodeServiceException {
		ICode code = createCode(codeCaption);
		addCode(code, codeable);
		return code;
	}

	public void addCode(ICode code, final ICodeable codeable)
			throws CodeServiceException {
		this.addCodes(Arrays.asList(code), Arrays.asList(codeable));
	}

	@Override
	public void addCodes(List<ICode> codes, List<ICodeable> codeables)
			throws CodeServiceException {
		try {
			for (ICode code : codes) {
				if (!codeStore.codeExists(code))
					codeStore.addAndSaveCode(code);
			}
			codeServiceListenerNotifier.codesCreated(codes);
			ICodeInstance[] codeInstances = codeStore.createCodeInstances(
					codes.toArray(new ICode[0]),
					codeables.toArray(new ICodeable[0]));
			codeStore.addAndSaveCodeInstances(codeInstances);
			codeServiceListenerNotifier.codeAssigned(codes, codeables);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreReadException e) {
			throw new CodeServiceException(e);
		} catch (DuplicateCodeInstanceException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public Set<URI> getCodedIDs() {
		Set<URI> codedIDs = new HashSet<URI>();
		for (ICodeInstance instance : this.codeStore.loadInstances()) {
			URI id = instance.getId();
			if (!codedIDs.contains(id))
				codedIDs.add(id);
		}
		return codedIDs;
	}

	@Override
	public List<ICodeInstance> getInstances(ICode code) {
		ArrayList<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
		for (ICodeInstance codeInstance : codeStore.loadInstances()) {
			if (codeInstance.getCode().equals(code))
				codeInstances.add(codeInstance);
		}
		return codeInstances;
	}

	@Override
	public Collection<? extends ICodeInstance> getAllInstances(ICode code) {
		List<ICodeInstance> instances = getInstances(code);
		List<ICode> subCodes = getSubCodes(code);
		for (ICode subCode : subCodes) {
			instances.addAll(getInstances(subCode));
		}
		return instances;
	}

	@Override
	public void putInstances(ICode code, List<ICodeable> instances) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renameCode(ICode code, String newCaption)
			throws CodeServiceException {
		String oldCaption = code.getCaption();
		try {
			code.setCaption(newCaption);
			codeServiceListenerNotifier.codeRenamed(code, oldCaption,
					newCaption);
		} catch (Exception e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public List<ICode> getTopLevelCodes() {
		return this.codeStore.getTopLevelCodes();
	}

	@Override
	public ICode getParent(ICode code) {
		return this.codeStore.getParent(code);
	}

	@Override
	public void setParent(ICode code, ICode parentCode)
			throws CodeServiceException {
		ICode oldParentCode;
		try {
			oldParentCode = this.codeStore.setParent(code, parentCode);
			codeServiceListenerNotifier.codeMoved(code, oldParentCode,
					parentCode);
		} catch (CodeDoesNotExistException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public List<ICode> getChildren(ICode code) {
		return this.codeStore.getChildren(code);
	}

	@Override
	public List<ICode> getSubCodes(ICode code) {
		return this.codeStore.getSubCodes(code);
	}

	@Override
	public void removeCode(ICode code, final ICodeable codeable)
			throws CodeServiceException {
		try {
			int numRemoved = 0;
			for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
				if (codeInstance.getCode().equals(code)
						&& codeInstance.getId().equals(
								codeable.getCodeInstanceID())) {
					this.codeStore.deleteCodeInstance(codeInstance);
					codeServiceListenerNotifier.codeRemoved(code,
							Arrays.asList(codeable));
					numRemoved++;
				}
			}
			if (numRemoved == 0)
				throw new CodeInstanceDoesNotExistException();
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeInstanceDoesNotExistException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void deleteCode(ICode code) throws CodeServiceException {
		try {
			this.codeStore.removeAndSaveCode(code);
			codeServiceListenerNotifier.codeDeleted(code);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeDoesNotExistException e) {
			throw new CodeServiceException(e);
		} catch (CodeHasChildCodesException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void deleteCode(ICode code, boolean forceDelete)
			throws CodeServiceException {
		try {
			this.codeStore.removeAndSaveCode(code, forceDelete);
			codeServiceListenerNotifier.codeDeleted(code);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeDoesNotExistException e) {
			throw new CodeServiceException(e);
		} catch (CodeHasChildCodesException e) {
			throw new CodeServiceException(e);
		}
	}

	private List<ICodeableProvider> getRegisteredCodeableProviders() {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.codeableprovider");
		List<ICodeableProvider> registeredCodeableProviders = new ArrayList<ICodeableProvider>();
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof ICodeableProvider) {
					registeredCodeableProviders.add((ICodeableProvider) o);
				}
			} catch (CoreException e1) {
				LOGGER.error("Error retrieving a currently registered "
						+ ICodeableProvider.class.getSimpleName(), e1);
				return null;
			}
		}
		return registeredCodeableProviders;
	}

	@Override
	public ICodeable getCodedObject(URI codeInstanceID) {
		List<ICodeableProvider> codeableProviders = this
				.getRegisteredCodeableProviders();
		if (codeableProviders == null)
			return null;
		for (ICodeableProvider codeableProvider : codeableProviders) {
			FutureTask<ICodeable> codedObject = codeableProvider
					.getCodedObject(codeInstanceID);
			if (codedObject != null)
				try {
					return codedObject.get();
				} catch (InterruptedException e) {
					LOGGER.error("Could not retrieve the coded object", e);
				} catch (ExecutionException e) {
					LOGGER.error("Could not retrieve the coded object", e);
				}
		}
		return null;
	}

	@SuppressWarnings("serial")
	@Override
	public void showCodedObjectInWorkspace(final URI codeInstanceID) {
		this.showCodedObjectsInWorkspace(new ArrayList<URI>() {
			{
				add(codeInstanceID);
			}
		});
	}

	@Override
	public void showCodedObjectsInWorkspace(List<URI> codeInstanceIDs) {
		List<ICodeableProvider> codeableProviders = this
				.getRegisteredCodeableProviders();
		if (codeableProviders == null)
			return;
		for (ICodeableProvider codeableProvider : codeableProviders) {
			codeableProvider.showCodedObjectsInWorkspace(codeInstanceIDs);
		}
	}

	@Override
	public ILabelProvider getLabelProvider(URI codeInstanceID) {
		List<ICodeableProvider> codeableProviders = this
				.getRegisteredCodeableProviders();
		if (codeableProviders == null)
			return null;
		for (ICodeableProvider codeableProvider : codeableProviders) {
			ILabelProvider labelProvider = codeableProvider
					.getLabelProvider(codeInstanceID);
			if (labelProvider != null)
				return labelProvider;
		}
		return null;
	}

	@Override
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeServiceException {
		try {
			this.codeStore.deleteCodeInstance(codeInstance);
			ICode code = codeInstance.getCode();
			List<ICodeable> codeables = Arrays.asList(this
					.getCodedObject(codeInstance.getId()));
			this.codeServiceListenerNotifier.codeRemoved(code, codeables);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeInstanceDoesNotExistException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public String loadMemo(ICode code) {
		return this.codeStore.getMemo(code);
	}

	@Override
	public String loadMemo(ICodeInstance codeInstance) {
		return this.codeStore.getMemo(codeInstance);
	}

	@Override
	public String loadMemo(ICodeable codeable) {
		return this.codeStore.getMemo(codeable);
	}

	@Override
	public void setMemo(ICode code, String html) throws CodeServiceException {
		try {
			this.codeStore.setMemo(code, html);
			this.codeServiceListenerNotifier.memoModified(code, html);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void setMemo(ICodeInstance codeInstance, String html)
			throws CodeServiceException {
		try {
			this.codeStore.setMemo(codeInstance, html);
			this.codeServiceListenerNotifier.memoModified(codeInstance, html);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void setMemo(ICodeable codeable, String html)
			throws CodeServiceException {
		try {
			this.codeStore.setMemo(codeable, html);
			this.codeServiceListenerNotifier.memoModified(codeable, html);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public boolean isMemo(ICode code) {
		String html = this.codeStore.getMemo(code);
		return html != null && !html.trim().isEmpty();
	}

	@Override
	public boolean isMemo(ICodeable codeable) {
		String html = this.codeStore.getMemo(codeable);
		return html != null && !html.trim().isEmpty();
	}

	@Override
	public boolean isMemo(ICodeInstance codeInstance) {
		String html = this.codeStore.getMemo(codeInstance);
		return html != null && !html.trim().isEmpty();
	}
}
