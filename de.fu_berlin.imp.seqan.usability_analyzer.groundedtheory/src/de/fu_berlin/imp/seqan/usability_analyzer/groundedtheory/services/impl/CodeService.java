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

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.component.ComponentContext;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.NoNullSet;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ILocatorProvider;
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

	private Cache<URI, ILocatable> uriCache = new Cache<URI, ILocatable>(
			new CacheFetcher<URI, ILocatable>() {
				@Override
				public ILocatable fetch(URI codeInstanceID,
						IProgressMonitor progressMonitor) {
					List<ILocatorProvider> locatorProviders = CodeService.this.getRegisteredCodeableProviders();
					if (locatorProviders == null) {
						return null;
					}
					for (ILocatorProvider locatorProvider : locatorProviders) {
						Future<ILocatable> codedObject = locatorProvider
								.getObject(codeInstanceID);
						if (codedObject != null) {
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
		this.codeServiceListenerNotifier
				.addCodeServiceListener(codeServiceListener);
	}

	@Override
	public void removeCodeServiceListener(
			ICodeServiceListener codeServiceListener) {
		this.codeServiceListenerNotifier
				.removeCodeServiceListener(codeServiceListener);
	}

	@Override
	public ICodeStore getCodeStore() {
		return this.codeStore;
	}

	@Override
	public ICode createCode(String caption, RGB color)
			throws CodeServiceException {
		ICode code;
		try {
			code = this.codeStore.createCode(caption, color);
		} catch (CodeStoreFullException e) {
			throw new CodeServiceException(e);
		}
		this.codeServiceListenerNotifier.codesCreated(Arrays.asList(code));
		return code;
	}

	@Override
	public ICode getCode(long id) {
		return this.codeStore.getCode(id);
	}

	@Override
	public List<ICode> getCodes(ILocatable codeable)
			throws CodeServiceException {
		return this.getCodes(codeable.getUri());
	}

	@Override
	public List<ICode> getCodes(URI codeableId) throws CodeServiceException {
		LinkedList<ICode> codes = new LinkedList<ICode>();
		for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
			if (codeInstance.getId().equals(codeableId)) {
				codes.add(codeInstance.getCode());
			}
		}
		return codes;
	}

	@Override
	public ICode addCode(String codeCaption, RGB color, ILocatable codeable)
			throws CodeServiceException {
		ICode code = this.createCode(codeCaption, color);
		this.addCode(code, codeable);
		return code;
	}

	@Override
	public void addCode(ICode code, final ILocatable codeable)
			throws CodeServiceException {
		this.addCodes(Arrays.asList(code), Arrays.asList(codeable));
	}

	@Override
	public void addCodes(List<ICode> codes, List<ILocatable> codeables)
			throws CodeServiceException {
		try {
			for (ICode code : codes) {
				if (!this.codeStore.codeExists(code)) {
					this.codeStore.addAndSaveCode(code);
					this.codeServiceListenerNotifier.codesCreated(codes);
				}
			}
			ICodeInstance[] codeInstances = this.codeStore.createCodeInstances(
					codes.toArray(new ICode[0]),
					codeables.toArray(new ILocatable[0]));
			this.codeStore.addAndSaveCodeInstances(codeInstances);
			this.codeServiceListenerNotifier.codeAssigned(codes, codeables);
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
			if (!codedIDs.contains(id)) {
				codedIDs.add(id);
			}
		}
		return codedIDs;
	}

	@Override
	public List<ICodeInstance> getInstances() {
		ArrayList<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
		for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
			codeInstances.add(codeInstance);
		}
		return codeInstances;
	}

	@Override
	public List<ICodeInstance> getInstances(IIdentifier identifier) {
		ArrayList<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
		for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
			String[] uriParts = codeInstance.getId().toString().split("/");
			if (identifier.equals(IdentifierFactory.createFrom(uriParts[3]))) {
				codeInstances.add(codeInstance);
			}
		}
		return codeInstances;
	}

	@Override
	public List<ICodeInstance> getInstances(ICode code) {
		ArrayList<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
		for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
			if (codeInstance.getCode().equals(code)) {
				codeInstances.add(codeInstance);
			}
		}
		return codeInstances;
	}

	@Override
	public Collection<? extends ICodeInstance> getAllInstances(ICode code) {
		List<ICodeInstance> instances = this.getInstances(code);
		for (ICode subCode : this.getSubCodes(code)) {
			instances.addAll(this.getAllInstances(subCode));
		}
		return instances;
	}

	@Override
	public void putInstances(ICode code, List<ILocatable> instances) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renameCode(ICode code, String newCaption)
			throws CodeServiceException {
		String oldCaption = code.getCaption();
		try {
			code.setCaption(newCaption);
			this.codeServiceListenerNotifier.codeRenamed(code, oldCaption,
					newCaption);
		} catch (Exception e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void recolorCode(ICode code, RGB newColor)
			throws CodeServiceException {
		RGB oldColor = code.getColor();
		try {
			code.setColor(newColor);
			this.codeServiceListenerNotifier.codeRecolored(code, oldColor,
					newColor);
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
			this.codeServiceListenerNotifier.codeMoved(code, oldParentCode,
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
	public void removeCodes(List<ICode> codes, final ILocatable codeable)
			throws CodeServiceException {
		if (codes.size() == 0) {
			return;
		}
		try {
			List<ICode> removedCodes = new LinkedList<ICode>();
			for (ICodeInstance codeInstance : this.codeStore.loadInstances()) {
				if (codes.contains(codeInstance.getCode())
						&& codeInstance.getId().equals(codeable.getUri())) {
					this.codeStore.deleteCodeInstance(codeInstance);
					removedCodes.add(codeInstance.getCode());
				}
			}
			if (removedCodes.size() == 0) {
				throw new CodeInstanceDoesNotExistException();
			}

			this.codeServiceListenerNotifier.codesRemoved(removedCodes,
					Arrays.asList(codeable));
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeInstanceDoesNotExistException e) {
			throw new CodeServiceException(e);
		}
	}

	// TODO check if removed is also fired to allow viewers to only listen to
	// removals (so they don't have to refresh their viewers although they are
	// not affected
	@Override
	public void deleteCode(ICode code) throws CodeServiceException {
		try {
			this.codeStore.removeAndSaveCode(code);
			this.codeServiceListenerNotifier.codeDeleted(code);
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
			this.codeServiceListenerNotifier.codeDeleted(code);
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeDoesNotExistException e) {
			throw new CodeServiceException(e);
		} catch (CodeHasChildCodesException e) {
			throw new CodeServiceException(e);
		}
	}

	private List<ILocatorProvider> getRegisteredCodeableProviders() {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.codeableprovider");
		List<ILocatorProvider> registeredCodeableProviders = new ArrayList<ILocatorProvider>();
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof ILocatorProvider) {
					registeredCodeableProviders.add((ILocatorProvider) o);
				}
			} catch (CoreException e1) {
				LOGGER.error("Error retrieving a currently registered "
						+ ILocatorProvider.class.getSimpleName(), e1);
				return null;
			}
		}
		return registeredCodeableProviders;
	}

	@Override
	public ILocatable getCodedObject(URI codeInstanceID) {
		return this.uriCache.getPayload(codeInstanceID, null);
	}

	@Override
	public boolean showCodedObjectInWorkspace(final URI codeInstanceID,
			boolean show) {
		return this.showCodedObjectsInWorkspace(
				new ArrayList<URI>(Arrays.asList(codeInstanceID)), show);
	}

	@Override
	/**
	 * Shows the given {@link URI}s in the workspace.
	 * <p>
	 * Since {@link ILocatorProvider#showCodedObjectsInWorkspace(List)} is expected
	 * to start a separate thread, all {@link ILocatorProvider}s are handled parallel.
	 */
	public boolean showCodedObjectsInWorkspace(List<URI> uris, boolean show) {
		List<ILocatorProvider> locatorProviders = this
				.getRegisteredCodeableProviders();
		if (locatorProviders == null) {
			return true;
		}

		List<Future<Boolean>> rs = new ArrayList<Future<Boolean>>();
		for (ILocatorProvider locatorProvider : locatorProviders) {
			Future<Boolean> future = locatorProvider.showInWorkspace(
					uris.toArray(new URI[0]), show);
			rs.add(future);
		}
		for (Future<Boolean> r : rs) {
			try {
				if (!r.get()) {
					return false;
				}
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
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeServiceException {
		try {
			this.codeStore.deleteCodeInstance(codeInstance);
			ICode code = codeInstance.getCode();
			List<ILocatable> codeables = Arrays.asList(this
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
	public String loadMemo(ILocatable codeable) {
		return this.codeStore.getMemo(codeable);
	}

	@Override
	public void setMemo(ICode code, String html) throws CodeServiceException {
		String oldHtml = this.codeStore.getMemo(code);

		if (oldHtml == null || oldHtml.trim().isEmpty()) {
			oldHtml = "";
		}
		if (html == null || html.trim().isEmpty()) {
			html = "";
		}
		if (oldHtml.equals(html)) {
			return;
		}

		try {
			this.codeStore.setMemo(code, html);
			if (oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoAdded(code, html);
			} else if (!oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoModified(code, html);
			} else if (!oldHtml.equals("") && html.equals("")) {
				this.codeServiceListenerNotifier.memoRemoved(code, html);
			} else {
				throw new CodeStoreWriteException("STATE ERROR");
			}
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void setMemo(ICodeInstance codeInstance, String html)
			throws CodeServiceException {
		String oldHtml = this.codeStore.getMemo(codeInstance);

		if (oldHtml == null || oldHtml.trim().isEmpty()) {
			oldHtml = "";
		}
		if (html == null || html.trim().isEmpty()) {
			html = "";
		}
		if (oldHtml.equals(html)) {
			return;
		}

		try {
			this.codeStore.setMemo(codeInstance, html);
			if (oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoAdded(codeInstance, html);
			} else if (!oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoModified(codeInstance,
						html);
			} else if (!oldHtml.equals("") && html.equals("")) {
				this.codeServiceListenerNotifier
						.memoRemoved(codeInstance, html);
			} else {
				throw new CodeStoreWriteException("STATE ERROR");
			}
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void setMemo(ILocatable codeable, String html)
			throws CodeServiceException {
		String oldHtml = this.codeStore.getMemo(codeable);

		if (oldHtml == null || oldHtml.trim().isEmpty()) {
			oldHtml = "";
		}
		if (html == null || html.trim().isEmpty()) {
			html = "";
		}
		if (oldHtml.equals(html)) {
			return;
		}

		try {
			this.codeStore.setMemo(codeable, html);
			if (oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoAdded(codeable, html);
			} else if (!oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoModified(codeable, html);
			} else if (!oldHtml.equals("") && html.equals("")) {
				this.codeServiceListenerNotifier.memoRemoved(codeable, html);
			} else {
				throw new CodeStoreWriteException("STATE ERROR");
			}
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
	public boolean isMemo(ILocatable codeable) {
		String html = this.codeStore.getMemo(codeable);
		return html != null && !html.trim().isEmpty();
	}

	@Override
	public boolean isMemo(ICodeInstance codeInstance) {
		String html = this.codeStore.getMemo(codeInstance);
		return html != null && !html.trim().isEmpty();
	}

	@Override
	public List<IIdentifier> getEpisodedIdentifiers() {
		List<IIdentifier> identifiers = new LinkedList<IIdentifier>();
		for (IEpisode episode : this.codeStore.getEpisodes()) {
			IIdentifier identifier = episode.getIdentifier();
			if (!identifiers.contains(identifier)) {
				identifiers.add(identifier);
			}
		}
		return identifiers;
	}

	@Override
	public Set<IEpisode> getEpisodes(IIdentifier identifier) {
		Set<IEpisode> episodes = new NoNullSet<IEpisode>();
		for (IEpisode episode : this.codeStore.getEpisodes()) {
			if (episode.getIdentifier().equals(identifier)) {
				episodes.add(episode);
			}
		}
		return episodes;
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
		if (oldEpisode == null || newEpisode == null) {
			throw new CodeServiceException(new IllegalArgumentException(
					"Arguments must not be null"));
		}
		Set<IEpisode> episodes = this.codeStore.getEpisodes();
		if (episodes.contains(oldEpisode)) {
			this.uriCache.removeKey(oldEpisode.getUri());
			episodes.remove(oldEpisode);
			episodes.add(newEpisode);

			List<ICode> codes = this.getCodes(oldEpisode);
			this.removeCodes(codes, oldEpisode);
			this.addCodes(codes,
					new LinkedList<ILocatable>(Arrays.asList(newEpisode)));

			String memo = this.loadMemo(oldEpisode);
			this.setMemo(oldEpisode, null);
			this.setMemo(newEpisode, memo);
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
				this.uriCache.removeKey(episodeToDelete.getUri());
				episodes.remove(episodeToDelete);
				this.removeCodes(this.getCodes(episodeToDelete),
						episodeToDelete);
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
		if (notDeletedEpisodes.size() > 0) {
			throw new EpisodeDoesNotExistException(notDeletedEpisodes);
		}
	}
}
