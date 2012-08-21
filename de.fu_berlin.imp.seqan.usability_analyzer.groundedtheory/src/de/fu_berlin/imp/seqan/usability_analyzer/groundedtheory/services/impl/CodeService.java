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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.osgi.service.component.ComponentContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.NoNullSet;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
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
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.CodeStoreFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

public class CodeService implements ICodeService {

	private static final Logger LOGGER = Logger.getLogger(CodeService.class);

	private Cache<URI, ICodeable> uriCache = new Cache<URI, ICodeable>(
			new CacheFetcher<URI, ICodeable>() {
				@Override
				public ICodeable fetch(URI codeInstanceID,
						IProgressMonitor progressMonitor) {
					List<ICodeableProvider> codeableProviders = getRegisteredCodeableProviders();
					if (codeableProviders == null)
						return null;
					for (ICodeableProvider codeableProvider : codeableProviders) {
						FutureTask<ICodeable> codedObject = codeableProvider
								.getCodedObject(codeInstanceID);
						if (codedObject != null)
							try {
								return codedObject.get();
							} catch (InterruptedException e) {
								LOGGER.error(
										"Could not retrieve the coded object",
										e);
							} catch (ExecutionException e) {
								LOGGER.error(
										"Could not retrieve the coded object",
										e);
							}
					}
					return null;
				}
			}, 200);

	@SuppressWarnings("unused")
	private ComponentContext context;
	private ICodeStore codeStore;
	private CodeServiceListenerNotifier codeServiceListenerNotifier;

	public CodeService() throws IOException {
		this(new CodeStoreFactory().getCodeStore());
	}

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
		for (ICode subCode : getSubCodes(code)) {
			instances.addAll(getAllInstances(subCode));
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
	public void removeCodes(List<ICode> codes, final ICodeable codeable)
			throws CodeServiceException {
		if (codes.size() == 0)
			return;
		try {
			List<ICode> removedCodes = new LinkedList<ICode>();
			for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
				if (codes.contains(codeInstance.getCode())
						&& codeInstance.getId().equals(
								codeable.getCodeInstanceID())) {
					this.codeStore.deleteCodeInstance(codeInstance);
					removedCodes.add(codeInstance.getCode());
				}
			}
			if (removedCodes.size() == 0)
				throw new CodeInstanceDoesNotExistException();

			codeServiceListenerNotifier.codesRemoved(removedCodes,
					Arrays.asList(codeable));
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
		return uriCache.getPayload(codeInstanceID, null);
	}

	@SuppressWarnings("serial")
	@Override
	public boolean showCodedObjectInWorkspace(final URI codeInstanceID) {
		return this.showCodedObjectsInWorkspace(new ArrayList<URI>() {
			{
				add(codeInstanceID);
			}
		});
	}

	@Override
	/**
	 * Shows the given {@link URI}s in the workspace.
	 * <p>
	 * Since {@link ICodeableProvider#showCodedObjectsInWorkspace(List)} is expected
	 * to start a separate thread, all {@link ICodeableProvider}s are handled parallel.
	 */
	public boolean showCodedObjectsInWorkspace(List<URI> codeInstanceIDs) {
		List<ICodeableProvider> codeableProviders = this
				.getRegisteredCodeableProviders();
		if (codeableProviders == null)
			return true;

		List<Future<Boolean>> rs = new ArrayList<Future<Boolean>>();
		for (ICodeableProvider codeableProvider : codeableProviders) {
			Future<Boolean> future = codeableProvider
					.showCodedObjectsInWorkspace(codeInstanceIDs);
			rs.add(future);
		}
		for (Future<Boolean> r : rs) {
			try {
				if (!r.get())
					return false;
			} catch (InterruptedException e) {
				LOGGER.error("Error while showing coded objects", e);
				return false;
			} catch (ExecutionException e) {
				LOGGER.error("Error while showing coded objects", e);
				return false;
			}
		}
		return true;
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
			this.codeServiceListenerNotifier.codesRemoved(Arrays.asList(code),
					codeables);
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
		String old = this.codeStore.getMemo(code);
		if ((old != null && old.equals(html)) || (old == null && html == null)
				|| (old == null && html.trim().isEmpty()))
			return;
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
		String old = this.codeStore.getMemo(codeInstance);
		if ((old != null && old.equals(html)) || (old == null && html == null)
				|| (old == null && html.trim().isEmpty()))
			return;
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
		String old = this.codeStore.getMemo(codeable);
		if ((old != null && old.equals(html)) || (old == null && html == null)
				|| (old == null && html.trim().isEmpty()))
			return;
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

	@Override
	public List<Object> getEpisodedKeys() {
		List<Object> keys = new LinkedList<Object>();
		for (IEpisode episode : this.codeStore.getEpisodes()) {
			Object key = episode.getKey();
			if (!keys.contains(key))
				keys.add(key);
		}
		return keys;
	}

	private Set<IEpisode> getEpisodes(Object key) {
		Set<IEpisode> episodes = new NoNullSet<IEpisode>();
		for (IEpisode episode : this.codeStore.getEpisodes())
			if (episode.getKey().equals(key))
				episodes.add(episode);
		return episodes;
	}

	@Override
	public Set<IEpisode> getEpisodes(ID id) {
		return getEpisodes((Object) id);
	}

	@Override
	public Set<IEpisode> getEpisodes(Fingerprint fingerprint) {
		return getEpisodes((Object) fingerprint);
	}

	@Override
	public void addEpisodeAndSave(IEpisode episode) throws CodeServiceException {
		Set<IEpisode> episodes = this.codeStore.getEpisodes();
		if (!episodes.contains(episode)) {
			episodes.add(episode);
			try {
				this.codeStore.save();
				this.codeServiceListenerNotifier.episodeAdded(episode);
			} catch (CodeStoreWriteException e) {
				throw new CodeServiceException(e);
			}
		} else {
			throw new EpisodeAlreadyExistsException(episode);
		}
	}

	@Override
	public void replaceEpisodeAndSave(IEpisode oldEpisode, IEpisode newEpisode)
			throws CodeServiceException {
		if (oldEpisode == null || newEpisode == null)
			throw new CodeServiceException(new IllegalArgumentException(
					"Arguments must not be null"));
		Set<IEpisode> episodes = this.codeStore.getEpisodes();
		if (episodes.contains(oldEpisode)) {
			this.uriCache.removeKey(oldEpisode.getCodeInstanceID());
			episodes.remove(oldEpisode);
			episodes.add(newEpisode);

			List<ICode> codes = getCodes(oldEpisode);
			removeCodes(codes, oldEpisode);
			addCodes(codes,
					new LinkedList<ICodeable>(Arrays.asList(newEpisode)));

			String memo = loadMemo(oldEpisode);
			setMemo(oldEpisode, null);
			setMemo(newEpisode, memo);
			try {
				this.codeStore.save();
				this.codeServiceListenerNotifier.episodeReplaced(oldEpisode,
						newEpisode);
			} catch (CodeStoreWriteException e) {
				throw new CodeServiceException(e);
			}
		} else {
			throw new EpisodeDoesNotExistException(oldEpisode);
		}
	}

	@Override
	public void deleteEpisodeAndSave(List<IEpisode> episodesToDelete)
			throws CodeServiceException {
		Set<IEpisode> episodes = this.codeStore.getEpisodes();
		Set<IEpisode> deletedEpisodes = new NoNullSet<IEpisode>();
		for (IEpisode episodeToDelete : episodesToDelete) {
			if (episodes.contains(episodeToDelete)) {
				this.uriCache.removeKey(episodeToDelete.getCodeInstanceID());
				episodes.remove(episodeToDelete);
				removeCodes(getCodes(episodeToDelete), episodeToDelete);
				deletedEpisodes.add(episodeToDelete);
			}
		}

		try {
			this.codeStore.save();
			this.codeServiceListenerNotifier.episodesDeleted(deletedEpisodes);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}

		@SuppressWarnings("unchecked")
		Collection<IEpisode> notDeletedEpisodes = CollectionUtils.disjunction(
				episodesToDelete, deletedEpisodes);
		if (notDeletedEpisodes.size() > 0)
			throw new EpisodeDoesNotExistException(notDeletedEpisodes);
	}
}
