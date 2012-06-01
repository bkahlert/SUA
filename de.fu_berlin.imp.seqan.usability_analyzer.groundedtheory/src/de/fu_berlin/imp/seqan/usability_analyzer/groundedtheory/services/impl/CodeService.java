package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
		try {
			ICode code = codeStore.createCode(codeCaption);
			addCode(code, codeable);
			return code;
		} catch (CodeStoreFullException e) {
			throw new CodeServiceException(e);
		}
	}

	public ICode addCode(ICode code, final ICodeable codeable)
			throws CodeServiceException {
		try {
			if (!codeStore.getTopLevelCodes().contains(code)) {
				codeStore.addAndSaveCode(code);
				codeServiceListenerNotifier.codeCreated(code);
			}
			ICodeInstance codeInstance = codeStore.createCodeInstance(code,
					codeable);
			codeStore.addAndSaveCodeInstance(codeInstance);
			codeServiceListenerNotifier.codeAssigned(code,
					Arrays.asList(codeable));
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
		ArrayList<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
		for (ICodeInstance codeInstance : codeStore.loadInstances()) {
			if (codeInstance.getCode().equals(code))
				codeInstances.add(codeInstance);
		}
		return codeInstances;
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
	public void setParent(ICode childNode, ICode newParentNode) {
		ICode oldParentNode = null;

		for (ICode code : this.codeStore.getTopLevelCodes()) {
			if (code.getChildCodes().contains(childNode)) {
				code.getChildCodes().remove(childNode);
				oldParentNode = code;
			}
		}

		if (newParentNode != null)
			newParentNode.addChildCode(childNode);

		codeServiceListenerNotifier.codeMoved(childNode, oldParentNode,
				newParentNode);
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

}
