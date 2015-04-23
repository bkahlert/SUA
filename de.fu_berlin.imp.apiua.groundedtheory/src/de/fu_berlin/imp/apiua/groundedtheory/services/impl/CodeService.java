package de.fu_berlin.imp.apiua.groundedtheory.services.impl;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;
import org.osgi.service.component.ComponentContext;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IteratorUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.utils.Triple;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.IImportanceInterceptor;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.util.NoNullSet;
import de.fu_berlin.imp.apiua.groundedtheory.AxialCodingModelLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.CodeLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation.EndPoint;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ProposedRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.Relation;
import de.fu_berlin.imp.apiua.groundedtheory.model.RelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IllegalDimensionValueException;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeHasChildCodesException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.DuplicateCodeInstanceException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.DuplicateRelationException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.DuplicateRelationInstanceException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.RelationDoesNotExistException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.RelationInstanceDoesNotExistException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.impl.CodeStoreFactory;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingComposite;
import de.fu_berlin.imp.apiua.groundedtheory.views.MergedProposedRelation;

public class CodeService implements ICodeService, IDisposable {

	private static final Logger LOGGER = Logger.getLogger(CodeService.class);

	private IImportanceService importanceService = null;
	private final IImportanceInterceptor importanceInterceptor = uris -> {
		// for (Entry<URI, Importance> importance : uris.entrySet()) {
		// for (ICode code : CodeService.this.getCodes(importance.getKey())) {
		// Importance codeImportance = CodeService.this.importanceService
		// .getImportance(code.getUri());
		// if (codeImportance == Importance.HIGH) {
		// importance.setValue(Importance.HIGH);
		// }
		// }
		// }
	};

	@SuppressWarnings("unused")
	private ComponentContext context;
	private final ICodeStore codeStore;
	private final CodeServiceListenerNotifier codeServiceListenerNotifier;
	private final CodeLocatorProvider codeLocatorProvider = new CodeLocatorProvider();

	public CodeService() throws IOException {
		this(new CodeStoreFactory().getCodeStore());
	}

	public CodeService(ICodeStore codeStore) throws IOException {
		Assert.isNotNull(codeStore);
		this.codeStore = codeStore;
		this.codeServiceListenerNotifier = new CodeServiceListenerNotifier();
		try {
			this.importanceService = (IImportanceService) PlatformUI
					.getWorkbench().getService(IImportanceService.class);
			this.importanceService
					.addImportanceInterceptor(this.importanceInterceptor);
		} catch (NoClassDefFoundError e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void dispose() {
		if (this.importanceService != null) {
			this.importanceService
					.removeImportanceInterceptor(this.importanceInterceptor);
		}
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
		return this.codeStore.getCodeHierarchyView().getCode(id);
	}

	@Override
	public Set<ICode> getExplicitCodes(URI uri) {
		return this.codeStore.getCodeInstanceView()
				.getExplicitCodesByPhenomenon(uri);
	}

	@Override
	public Set<ICode> getAllCodes(URI uri) {
		return this.codeStore.getCodeInstanceView()
				.getAllCodesByPhenomenon(uri);
	}

	@Override
	public ICode addCode(String codeCaption, RGB color, URI uri)
			throws CodeServiceException {
		ICode code = this.createCode(codeCaption, color);
		this.addCode(code, uri);
		return code;
	}

	@Override
	public URI addCode(ICode code, final URI uri) throws CodeServiceException {
		return this.addCodes(new HashSet<>(Arrays.asList(code)), new HashSet<>(
				Arrays.asList(uri)))[0];
	}

	@Override
	public URI[] addCodes(Set<ICode> codes, Set<URI> uris)
			throws CodeServiceException {
		try {
			for (ICode code : codes) {
				if (!this.codeStore.codeExists(code)) {
					this.codeStore.addAndSaveCode(code);
					this.codeServiceListenerNotifier
							.codesCreated(new LinkedList<>(codes));
				}
			}
			ICodeInstance[] codeInstances = this.codeStore.createCodeInstances(
					codes.toArray(new ICode[0]), uris.toArray(new URI[0]));
			this.codeStore.addAndSaveCodeInstances(codeInstances);
			this.codeServiceListenerNotifier.codesAssigned(new LinkedList<>(
					codes), new LinkedList<>(uris));
			URI[] codeInstanceUris = new URI[codeInstances.length];
			for (int i = 0; i < codeInstanceUris.length; i++) {
				codeInstanceUris[i] = codeInstances[i].getUri();
			}
			return codeInstanceUris;
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreReadException e) {
			throw new CodeServiceException(e);
		} catch (DuplicateCodeInstanceException e) {
			throw new CodeServiceException(e);
		} catch (InvalidParameterException e) {
			throw new CodeServiceException(e);
		} catch (CodeStoreFullException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public Set<URI> getCodedIDs() {
		return this.codeStore.getCodeInstanceView().getCodedPhenomena();
	}

	@Override
	public Set<ICodeInstance> getExplicitInstances(ICode code) {
		return this.codeStore.getCodeInstanceView()
				.getExplicitCodeInstancesByCode(Arrays.asList(code));
	}

	@Override
	public Set<ICodeInstance> getAllInstances(ICode code) {
		return this.codeStore.getCodeInstanceView().getAllCodeInstancesByCode(
				Arrays.asList(code));
	}

	@Override
	public List<ICodeInstance> getExplicitInstances(URI uri) {
		return this.codeStore.getCodeInstanceView()
				.getExplicitCodeInstancesByPhenomenon(uri);
	}

	@Override
	public List<ICodeInstance> getAllInstances(URI uri) {
		return this.codeStore.getCodeInstanceView()
				.getAllCodeInstancesByPhenomenon(uri);
	}

	@Override
	public void renameCode(ICode code, String newCaption)
			throws CodeServiceException {
		String oldCaption = code.getCaption();
		if (ObjectUtils.equals(oldCaption, newCaption)) {
			return;
		}
		try {
			code.setCaption(newCaption);
			this.codeStore.save();
			this.codeServiceListenerNotifier.codeRenamed(code, oldCaption,
					newCaption);
		} catch (Exception e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void recolorCode(ICode code, RGB newColor)
			throws CodeServiceException {
		try {
			code.setColor(newColor);
			this.codeStore.save();
			this.codeServiceListenerNotifier
					.codesRecolored(Arrays.asList(code));
		} catch (Exception e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void recolorCode(List<Pair<ICode, RGB>> newColors)
			throws CodeServiceException {
		for (Pair<ICode, RGB> newColor : newColors) {
			newColor.getFirst().setColor(newColor.getSecond());
		}
		try {
			this.codeStore.save();
			this.codeServiceListenerNotifier.codesRecolored(newColors.stream()
					.map(c -> c.getFirst()).collect(Collectors.toList()));
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
		return this.codeStore.getCodeHierarchyView().getChildren(code);
	}

	@Override
	public List<ICode> getDescendents(ICode code) {
		return this.codeStore.getCodeHierarchyView().getDescendents(code);
	}

	@Override
	public ICode getCommonAncestor(Set<ICode> codes) {
		return this.codeStore.getCodeHierarchyView().getCommonAncestor(codes);
	}

	@Override
	public URI getCommonAncestor(List<URI> uris) {
		return this.codeStore.getCodeHierarchyView().getCommonAncestor(uris);
	}

	@Override
	public int getPosition(ICode code) {
		return this.codeStore.getPosition(code);
	}

	@Override
	public void setPosition(ICode code, int pos) {
		this.codeStore.setPosition(code, pos);
		this.codeServiceListenerNotifier.codeMoved(code, null, null);
	}

	@Override
	public void removeCodes(Set<ICode> codes, final URI uri)
			throws CodeServiceException {
		if (codes.size() == 0) {
			return;
		}
		try {
			List<ICode> removedCodes = new LinkedList<ICode>();
			for (ICodeInstance codeInstance : this.codeStore
					.getCodeInstanceView().getExplicitCodeInstances()) {
				if (codes.contains(codeInstance.getCode())
						&& codeInstance.getId().equals(uri)) {
					this.codeStore.deleteCodeInstance(codeInstance);
					removedCodes.add(codeInstance.getCode());
				}
			}
			if (removedCodes.size() == 0) {
				throw new CodeInstanceDoesNotExistException();
			}

			this.codeServiceListenerNotifier.codesRemoved(removedCodes,
					Arrays.asList(uri));
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
		List<ICode> properties = this.getProperties(code);
		List<URI> propertyUris = new LinkedList<URI>();
		for (ICode propertyCode : properties) {
			propertyUris.add(propertyCode.getUri());
		}
		try {
			this.codeStore.removeAndSaveCode(code, forceDelete);
			this.codeServiceListenerNotifier.propertiesChanged(code.getUri(),
					new LinkedList<URI>(), propertyUris);
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
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeServiceException {
		try {
			this.codeStore.deleteCodeInstance(codeInstance);
			ICode code = codeInstance.getCode();
			this.codeServiceListenerNotifier.codesRemoved(Arrays.asList(code),
					Arrays.asList(codeInstance.getId()));
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		} catch (CodeInstanceDoesNotExistException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public IRelation getRelation(URI uri) {
		return this.codeStore.getRelationHierarchyView()
				.getRelationByRelationUri(uri);
	}

	@Override
	public Set<IRelation> getAllRelations() {
		return this.codeStore.getRelationHierarchyView().getAllRelations();
	}

	@Override
	public Set<IRelation> getRelationsByIdentifier(URI phenomenon) {
		Assert.isLegal(phenomenon != null);
		IIdentifier id = URIUtils.getIdentifier(phenomenon);
		Set<IRelation> relations = new HashSet<>();
		if (id == null) {
			for (URI shortenedPhenomenon = URIUtils.shorten(phenomenon); shortenedPhenomenon != null; shortenedPhenomenon = URIUtils
					.shorten(shortenedPhenomenon)) {
				for (IRelationInstance relationInstance : this.codeStore
						.getRelationInstanceView().getAllRelationInstances()) {
					if (relationInstance.getPhenomenon().toString()
							.startsWith(shortenedPhenomenon.toString())
							&& !relations.contains(relationInstance
									.getRelation())) {
						relations.add(relationInstance.getRelation());
					}
				}
				if (relations.size() > 0) {
					break;
				}
			}
		} else {
			return this.codeStore.getRelationInstanceView()
					.getAllRelationInstancesByIdentifier(id).stream()
					.map(i -> i.getRelation()).collect(Collectors.toSet());
		}
		return relations;
	}

	@Override
	public Set<IRelation> getExplicitRelations(URI from, URI to) {
		return this.codeStore.getRelationHierarchyView()
				.getExplicitRelationsStartingFrom(from).stream()
				.filter(r -> r.getTo().equals(to)).collect(Collectors.toSet());
	}

	@Override
	public Set<IRelation> getExplicitRelations() {
		return this.codeStore.getRelationHierarchyView().getExplicitRelations();
	}

	@Override
	public Set<ImplicitRelation> getImplicitRelations(IRelation relation) {
		return this.codeStore.getRelationHierarchyView()
				.getImplicitRelationsByRelationUri(relation);
	}

	@Override
	public Set<ProposedRelation> getProposedRelation(Collection<URI> froms,
			Collection<URI> tos, int maxRelationsBetweenTwoElements) {
		return this.codeStore.getRelationHierarchyView().getProposedRelation(
				froms, tos, maxRelationsBetweenTwoElements);
	}

	@Override
	public Set<MergedProposedRelation> getMergedProposedRelation(URI from,
			URI to) {
		return this.codeStore.getRelationHierarchyView()
				.getMergedProposedRelation(from, to);
	}

	@Override
	public Set<MergedProposedRelation> getMergedProposedRelation(
			List<URI> froms, List<URI> tos) {
		return this.codeStore.getRelationHierarchyView()
				.getMergedProposedRelation(froms, tos);
	}

	@Override
	public Set<IRelation> getExplicitRelationsStartingFrom(URI from) {
		return this.codeStore.getRelationHierarchyView()
				.getExplicitRelationsStartingFrom(from);
	}

	@Override
	public Set<IRelation> getExplicitRelationsEndingAt(URI to) {
		return this.codeStore.getRelationHierarchyView()
				.getExplicitRelationsEndingAt(to);
	}

	@Override
	public Set<IRelation> getAllRelationsStartingFrom(URI from) {
		return this.codeStore.getRelationHierarchyView()
				.getAllRelationsStartingFrom(from);
	}

	@Override
	public Set<IRelation> getAllRelationsEndingAt(URI to) {
		return this.codeStore.getRelationHierarchyView()
				.getAllRelationsEndingAt(to);
	}

	@Override
	public IRelation createRelation(URI from, URI to, String name)
			throws RelationDoesNotExistException, CodeStoreWriteException {
		Relation relation = null;
		while (true) {
			try {
				relation = new Relation(new URI("apiua://relation/"
						+ StringUtils.createRandomString(32)), from, to, name);
				this.codeStore.addRelation(relation);
				break;
			} catch (DuplicateRelationException e) {
			}
		}
		this.codeStore.save();
		Set<IRelation> set = new HashSet<IRelation>();
		set.add(relation);
		this.codeServiceListenerNotifier.relationsAdded(set);
		return relation;
	}

	@Override
	public void deleteRelation(IRelation relation)
			throws RelationDoesNotExistException, CodeStoreWriteException {
		Set<IRelationInstance> relationInstances = this
				.getExplicitRelationInstances(relation);
		for (IRelationInstance relationInstance : relationInstances) {
			try {
				this.codeStore.deleteRelationInstance(relationInstance);
			} catch (RelationInstanceDoesNotExistException e) {
				LOGGER.error(
						"Error deleting "
								+ IRelationInstance.class.getSimpleName()
								+ ": " + relationInstance, e);
			}
		}
		this.codeStore.deleteRelation(relation);
		this.codeStore.save();
		LocatorService.INSTANCE.uncache(relation.getUri());
		Set<IRelation> set = new HashSet<IRelation>();
		set.add(relation);
		this.codeServiceListenerNotifier
				.relationInstancesDeleted(relationInstances);
		this.codeServiceListenerNotifier.relationsDeleted(set);
	}

	@Override
	public void renameRelation(IRelation relation, String newName)
			throws RelationDoesNotExistException, CodeStoreWriteException {
		if (relation.getName().equals(newName)) {
			return;
		}
		if (LocatorService.INSTANCE != null) {
			LocatorService.INSTANCE.uncache(relation.getUri());
		}
		Relation newRelation = new Relation(relation.getUri(),
				relation.getFrom(), relation.getTo(), newName);
		this.codeStore.replaceRelation(relation, newRelation);
		this.codeStore.save();
		Set<IRelation> set = new HashSet<IRelation>();
		set.add(relation);
		this.codeServiceListenerNotifier.relationsUpdated(set);
	}

	@Override
	public void updateRelation(IRelation relation, EndPoint endPoint,
			URI newEndPoint) throws CodeStoreWriteException,
			RelationDoesNotExistException {
		if (LocatorService.INSTANCE != null) {
			LocatorService.INSTANCE.uncache(relation.getUri());
		}
		Relation newRelation;
		switch (endPoint) {
		case FROM:
			newRelation = new Relation(relation.getUri(), newEndPoint,
					relation.getTo(), relation.getName());
			break;
		case TO:
			newRelation = new Relation(relation.getUri(), relation.getFrom(),
					newEndPoint, relation.getName());
			break;
		default:
			throw new RuntimeException(new NotImplementedException());
		}
		this.codeStore.replaceRelation(relation, newRelation);
		this.codeStore.save();
		Set<IRelation> set = new HashSet<IRelation>();
		set.add(relation);
		this.codeServiceListenerNotifier.relationsUpdated(set);
	}

	@Override
	public IRelationInstance getRelationInstance(URI uri) {
		return this.codeStore.getRelationInstanceView()
				.getRelationInstancesByRelationInstanceUri(uri);
	}

	@Override
	public Set<IRelationInstance> getExplicitRelationInstances(
			IRelation relation) {
		return this.codeStore.getRelationInstanceView()
				.getExplicitRelationInstancesByRelation(
						relation != null ? relation.getUri() : null);
	}

	@Override
	public Set<ImplicitRelationInstance> getImplicitRelationInstances(
			IRelation relation) throws CodeDoesNotExistException {
		return this.codeStore.getRelationInstanceView()
				.getImplicitRelationInstancesByRelation(relation.getUri());
	}

	@Override
	public Set<IRelationInstance> getAllRelationInstances(IRelation relation) {
		return this.codeStore.getRelationInstanceView()
				.getAllRelationInstancesByRelation(relation.getUri());
	}

	@Override
	public Set<IRelationInstance> getExplicitRelationInstances(URI uri) {
		return this.codeStore.getRelationInstanceView()
				.getExplicitRelationInstancesByPhenomenon(uri);
	}

	@Override
	public Set<IRelationInstance> getAllRelationInstances(URI uri) {
		return this.codeStore.getRelationInstanceView()
				.getAllRelationInstancesByPhenomenon(uri);
	}

	@Override
	public Set<IRelationInstance> getRelationInstancesStartingFrom(URI from) {
		return this.codeStore.getRelationInstanceView()
				.getExplicitRelationInstancesStartingFrom(from);
	}

	@Override
	public Set<IRelationInstance> getAllRelationInstancesStartingFrom(URI from)
			throws CodeDoesNotExistException {
		return this.codeStore.getRelationInstanceView()
				.getAllRelationInstancesStartingFrom(from);
	}

	@Override
	public Set<IRelationInstance> getRelationInstancesEndingAt(URI to) {
		return this.codeStore.getRelationInstanceView()
				.getExplicitRelationInstancesEndingAt(to);
	}

	@Override
	public Set<IRelationInstance> getAllRelationInstancesEndingAt(URI to)
			throws CodeDoesNotExistException {
		return this.codeStore.getRelationInstanceView()
				.getAllRelationInstancesEndingAt(to);
	}

	@Override
	public IRelationInstance createRelationInstance(URI phenomenon,
			IRelation relation) throws RelationDoesNotExistException,
			CodeStoreWriteException {
		RelationInstance relationInstance = null;
		while (true) {
			try {
				relationInstance = new RelationInstance(new URI(
						"apiua://relationInstance/"
								+ StringUtils.createRandomString(32)),
						relation, phenomenon);
				this.codeStore.addRelationInstance(relationInstance);
				break;
			} catch (DuplicateRelationInstanceException e) {
			}
		}
		this.codeStore.save();
		Set<IRelationInstance> set = new HashSet<IRelationInstance>();
		set.add(relationInstance);
		this.codeServiceListenerNotifier.relationInstancesAdded(set);
		return relationInstance;
	}

	@Override
	public void deleteRelationInstance(IRelationInstance relationInstance)
			throws RelationInstanceDoesNotExistException,
			CodeStoreWriteException {
		this.codeStore.deleteRelationInstance(relationInstance);
		this.codeStore.save();
		LocatorService.INSTANCE.uncache(relationInstance.getUri());
		Set<IRelationInstance> set = new HashSet<IRelationInstance>();
		set.add(relationInstance);
		this.codeServiceListenerNotifier.relationInstancesDeleted(set);
	}

	@Override
	public boolean isGrounded(IRelation relation) {
		return this.getExplicitRelationInstances(relation).size() > 0;
	}

	@Override
	public boolean isExplicitlyGrounded(URI phenomenon, IRelation relation) {
		return this.isExplicitlyGrounded(phenomenon, relation.getFrom(),
				relation.getTo());
	}

	@Override
	public boolean isExplicitlyGrounded(URI phenomenon, URI from, URI to) {
		for (IRelationInstance relationInstance : this
				.getExplicitRelationInstances(phenomenon)) {
			if (relationInstance.getRelation().getFrom().equals(from)
					&& relationInstance.getRelation().getTo().equals(to)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String loadMemo(URI uri) {
		return this.codeStore.getMemo(uri);
	}

	@Override
	public String loadMemoPlain(URI uri) {
		String htmlMemo = this.loadMemo(uri);
		if (htmlMemo != null && !htmlMemo.trim().isEmpty()) {
			return StringUtils.htmlToPlain(htmlMemo);
		}
		return null;
	}

	@Override
	public void setMemo(URI uri, String html) throws CodeServiceException {
		String oldHtml = this.codeStore.getMemo(uri);

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
			this.codeStore.setMemo(uri, html);
			if (oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoAdded(uri, html);
			} else if (!oldHtml.equals("") && !html.equals("")) {
				this.codeServiceListenerNotifier.memoModified(uri, html);
			} else if (!oldHtml.equals("") && html.equals("")) {
				this.codeServiceListenerNotifier.memoRemoved(uri, html);
			} else {
				throw new CodeStoreWriteException("STATE ERROR");
			}
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public boolean isMemo(URI uri) {
		String html = this.codeStore.getMemo(uri);
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
			if (LocatorService.INSTANCE != null) {
				LocatorService.INSTANCE.uncache(oldEpisode.getUri());
			}
			episodes.remove(oldEpisode);
			episodes.add(newEpisode);

			this.reattachAndSave(oldEpisode.getUri(), newEpisode.getUri());

			this.codeServiceListenerNotifier.episodeReplaced(oldEpisode,
					newEpisode);
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
				if (LocatorService.INSTANCE != null) {
					LocatorService.INSTANCE.uncache(episodeToDelete.getUri());
				}
				episodes.remove(episodeToDelete);
				this.removeCodes(
						this.getExplicitCodes(episodeToDelete.getUri()),
						episodeToDelete.getUri());
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

	@Override
	public IDimension getDimension(URI uri) {
		return this.codeStore.getDimension(uri);
	}

	@Override
	public void setDimension(ICode code, IDimension newDimension)
			throws CodeStoreWriteException {
		IDimension oldDimension = this.getDimension(code.getUri());
		if (ObjectUtils.equals(oldDimension, newDimension)) {
			return;
		}
		this.codeStore.setDimension(code.getUri(), newDimension);
		this.codeStore.save();
		this.codeServiceListenerNotifier.dimensionChanged(code.getUri(),
				oldDimension, newDimension);
	}

	@Override
	public String getDimensionValue(URI uri, ICode code) {
		Assert.isNotNull(uri);
		return this.codeStore.getDimensionValue(uri, code.getUri());
	}

	@Override
	public void setDimensionValue(URI uri, ICode code, String value)
			throws IllegalDimensionValueException, CodeStoreWriteException {
		Assert.isNotNull(uri);
		Assert.isNotNull(code);
		if (this.getDimension(code.getUri()) == null
				|| !this.getDimension(code.getUri()).isLegal(value)) {
			throw new IllegalDimensionValueException(this.getDimension(code
					.getUri()), value);
		}
		String oldValue = this.codeStore.getDimensionValue(uri, code.getUri());
		if (ObjectUtils.equals(oldValue, value)) {
			return;
		}
		this.codeStore.setDimensionValue(uri, code.getUri(), value);
		this.codeServiceListenerNotifier.dimensionValueChanged(code.getUri(),
				oldValue, value);
		this.codeStore.save();
	}

	@Override
	public Map<ICode, Pair<IDimension, String>> getDimensionValues(
			ICodeInstance codeInstance) {
		Map<ICode, Pair<IDimension, String>> values = new HashMap<>();
		for (Triple<Integer, ICode, IDimension> property : this
				.getPropertyTree(codeInstance.getCode())) {
			values.put(property.getSecond(), null);
		}
		for (ICode code : this.getAllCodes(codeInstance.getId())) {
			values.put(code, null);
		}
		for (Iterator<ICode> iterator = values.keySet().iterator(); iterator
				.hasNext();) {
			ICode code = iterator.next();
			IDimension dimension = this.getDimension(code.getUri());
			if (dimension != null) {
				String value = this.getDimensionValue(codeInstance.getUri(),
						code);
				values.put(code, new Pair<>(dimension, value));
			} else {
				iterator.remove();
			}
		}
		return values;
	}

	@Override
	public List<Triple<Integer, ICode, IDimension>> getPropertyTree(ICode code) {
		List<Triple<Integer, ICode, IDimension>> dimensions = new LinkedList<Triple<Integer, ICode, IDimension>>();
		for (Pair<Integer, ICode> property : IteratorUtils.dfs(code,
				property -> this.getProperties(property).toArray(new ICode[0]))) {
			IDimension dimension = this.getDimension(property.getSecond()
					.getUri());
			dimensions.add(new Triple<Integer, ICode, IDimension>(property
					.getFirst(), property.getSecond(), dimension));
		}
		return dimensions;
	}

	@Override
	public Pair<Pair<IDimension, String>, Pair<IDimension, String>> getDimensionValue(
			IRelationInstance relationInstance) {
		List<ICodeInstance> codeInstances = this
				.getAllInstances(relationInstance.getPhenomenon());

		IDimension fromDimension = null;
		String fromDimensionValue = null;
		IDimension toDimension = null;
		String toDimensionValue = null;

		ICodeInstance[] fromCodeInstance = codeInstances
				.stream()
				.filter(i -> i.getCode().getUri()
						.equals(relationInstance.getRelation().getFrom()))
				.toArray(ICodeInstance[]::new);
		ICodeInstance[] toCodeInstance = codeInstances
				.stream()
				.filter(i -> i.getCode().getUri()
						.equals(relationInstance.getRelation().getTo()))
				.toArray(ICodeInstance[]::new);

		if (fromCodeInstance.length == 1) {
			fromDimension = this.getDimension(fromCodeInstance[0].getCode()
					.getUri());
			fromDimensionValue = this
					.getDimensionValue(fromCodeInstance[0].getUri(),
							fromCodeInstance[0].getCode());
		}

		if (toCodeInstance.length == 1) {
			toDimension = this.getDimension(toCodeInstance[0].getCode()
					.getUri());
			toDimensionValue = this.getDimensionValue(
					toCodeInstance[0].getUri(), toCodeInstance[0].getCode());
		}

		return new Pair<>(new Pair<>(fromDimension, fromDimensionValue),
				new Pair<>(toDimension, toDimensionValue));
	}

	@Override
	public Pair<Set<String>, Set<String>> getDimensionValues(
			Collection<IRelationInstance> relationInstances) {
		List<URI> phenomena = relationInstances.stream()
				.map(r -> r.getPhenomenon()).collect(Collectors.toList());
		List<URI> froms = relationInstances.stream()
				.map(r -> r.getRelation().getFrom())
				.collect(Collectors.toList());
		List<URI> tos = relationInstances.stream()
				.map(r -> r.getRelation().getTo()).collect(Collectors.toList());
		Set<ICodeInstance> codeInstances = this.codeStore.getCodeInstanceView()
				.getAllCodeInstancesPhenomena(phenomena);

		Set<String> fromDimensionValues = new HashSet<>();
		Set<String> toDimensionValues = new HashSet<>();

		ICodeInstance[] fromCodeInstances = codeInstances.stream()
				.filter(i -> froms.contains(i.getCode().getUri()))
				.toArray(ICodeInstance[]::new);
		ICodeInstance[] toCodeInstances = codeInstances.stream()
				.filter(i -> tos.contains(i.getCode().getUri()))
				.toArray(ICodeInstance[]::new);

		for (ICodeInstance fromCodeInstance : fromCodeInstances) {
			String fromDimensionValue = this.getDimensionValue(
					fromCodeInstance.getUri(), fromCodeInstance.getCode());
			if (fromDimensionValue != null
					&& !fromDimensionValues.contains(fromDimensionValue)) {
				fromDimensionValues.add(fromDimensionValue);
			}
		}

		for (ICodeInstance toCodeInstance : toCodeInstances) {
			String toDimensionValue = this.getDimensionValue(
					toCodeInstance.getUri(), toCodeInstance.getCode());
			if (toDimensionValue != null
					&& !toDimensionValues.contains(toDimensionValue)) {
				toDimensionValues.add(toDimensionValue);
			}
		}

		return new Pair<>(fromDimensionValues, toDimensionValues);
	}

	@Override
	public List<ICode> getProperties(ICode code) {
		Assert.isNotNull(code);
		List<ICode> properties = new ArrayList<ICode>();
		for (URI uri : this.codeStore.getProperties(code.getUri())) {
			ICode property = (ICode) this.codeLocatorProvider.getObject(uri,
					null);
			properties.add(property);
		}
		return properties;
	}

	@Override
	public List<ICode> getEffectiveProperties(ICode code) {
		List<ICode> properties = new ArrayList<ICode>();
		List<ICode> codes = new ArrayList<>();
		codes.add(code);
		codes.addAll(this.codeStore.getCodeHierarchyView().getAncestors(code));
		for (ICode ascendant : codes) {
			List<ICode> currProps = this.getProperties(ascendant);
			for (ICode currProp : currProps) {
				if (!properties.contains(currProp)) {
					properties.add(currProp);
				}
			}
		}
		return properties;
	}

	@Override
	public void setProperties(ICode code, List<ICode> properties)
			throws CodeStoreWriteException {
		List<URI> existing = this.codeStore.getProperties(code.getUri());
		List<URI> newUris = new ArrayList<URI>();
		List<URI> uris = new ArrayList<URI>();
		for (ICode property : properties) {
			URI uri = property.getUri();
			uris.add(uri);
			if (!existing.contains(uri)) {
				newUris.add(uri);
			}
		}

		if (uris.equals(existing)) {
			return;
		}

		@SuppressWarnings("unchecked")
		List<URI> removed = ListUtils.subtract(existing, uris);
		this.codeStore.setProperties(code.getUri(), uris);
		this.codeServiceListenerNotifier.propertiesChanged(code.getUri(),
				newUris, removed);
		this.codeStore.save();
	}

	@Override
	public void addProperty(ICode code, ICode property)
			throws CodeStoreWriteException {
		Assert.isNotNull(code);
		Assert.isNotNull(property);
		List<ICode> properties = this.getProperties(code);
		properties.add(property);
		List<URI> uris = new ArrayList<URI>();
		for (ICode propertyCode : properties) {
			uris.add(propertyCode.getUri());
		}
		uris.add(property.getUri());
		this.codeStore.setProperties(code.getUri(), uris);
		this.codeServiceListenerNotifier.propertiesChanged(code.getUri(),
				Arrays.asList(property.getUri()), new LinkedList<URI>());
		this.codeStore.save();
	}

	@Override
	public void removeProperty(ICode code, ICode property)
			throws CodeStoreWriteException {
		Assert.isNotNull(code);
		Assert.isNotNull(property);
		List<ICode> properties = this.getProperties(code);
		properties.add(property);
		List<URI> uris = new ArrayList<URI>();
		for (ICode propertyCode : properties) {
			uris.add(propertyCode.getUri());
		}
		uris.add(property.getUri());
		this.codeStore.setProperties(code.getUri(), uris);
		this.codeServiceListenerNotifier.propertiesChanged(code.getUri(),
				new LinkedList<URI>(), Arrays.asList(property.getUri()));
		this.codeStore.save();
	}

	private static final String AXIAL_CODING_MODEL_TYPE = "acm";

	@Override
	public List<URI> getAxialCodingModels() throws CodeStoreReadException {
		return this.codeStore.getRaw(AXIAL_CODING_MODEL_TYPE);
	}

	@Override
	public IAxialCodingModel getAxialCodingModel(URI uri)
			throws CodeStoreReadException {
		String json = this.codeStore.getRaw(AXIAL_CODING_MODEL_TYPE, uri);
		if (json != null) {
			return new JointJSAxialCodingModel(uri, json);
		}
		return null;
	}

	@Override
	public void addAxialCodingModel(IAxialCodingModel axialCodingModel)
			throws CodeStoreWriteException {
		Assert.isNotNull(axialCodingModel.getUri());
		LocatorService.INSTANCE.uncache(axialCodingModel.getUri());
		try {
			boolean update = this
					.getAxialCodingModel(axialCodingModel.getUri()) != null;
			this.codeStore.setRaw(AXIAL_CODING_MODEL_TYPE,
					axialCodingModel.getUri(), axialCodingModel.serialize());
			if (update) {
				this.codeServiceListenerNotifier
						.axialCodingModelUpdated(axialCodingModel.getUri());
			} else {
				this.codeServiceListenerNotifier
						.axialCodingModelAdded(axialCodingModel.getUri());
			}
		} catch (CodeStoreReadException e) {
			throw new CodeStoreWriteException(e);
		}
	}

	@Override
	public Future<IAxialCodingModel> createAxialCodingModelFrom(URI uri,
			String title) {
		return ExecUtils.nonUISyncExec(() -> {
			JointJSAxialCodingModel acm = new JointJSAxialCodingModel(
					AxialCodingModelLocatorProvider
							.createUniqueAxialCodingModelURI(),
					"{ \"cells\": [] }");
			acm.setOrigin(uri);
			acm.setTitle(title);

			ExecUtils.syncExec(() -> {
				CodeService.this.addAxialCodingModel(acm);
				return null;
			});

			return CodeService.this.updateAxialCodingModelFrom(acm.getUri())
					.get();
		});
	}

	@Override
	public Future<IAxialCodingModel> updateAxialCodingModelFrom(URI acmUri) {
		return ExecUtils
				.nonUISyncExec((Callable<IAxialCodingModel>) () -> {
					Pair<Shell, AxialCodingComposite> ui = ExecUtils
							.asyncExec(
									() -> {
										Shell shell = new Shell();
										AxialCodingComposite axialCodingComposite = new AxialCodingComposite(
												shell, SWT.NONE, false);
										return new Pair<>(shell,
												axialCodingComposite);
									}).get();

					ui.getSecond().open(acmUri).get();

					IAxialCodingModel acm = this.updateAxialCodingModelFrom(
							ui.getSecond(), acmUri).get();

					ExecUtils.asyncExec(() -> ui.getFirst().dispose());

					return acm;
				});
	}

	@Override
	public Future<IAxialCodingModel> updateAxialCodingModelFrom(
			AxialCodingComposite axialCodingComposite, URI acmUri) {
		return ExecUtils
				.nonUISyncExec((Callable<IAxialCodingModel>) () -> {
					IAxialCodingModel acm = this.getAxialCodingModel(acmUri);

					Set<IRelation> relations = new HashSet<>();
					Set<URI> neededElements = new HashSet<>();
					if (acm.getOrigin() != null) {
						Class<? extends ILocatable> type = LocatorService.INSTANCE
								.getType(acm.getOrigin());

						if (type == ICode.class) {
							relations = this.getAllRelations();
							neededElements = getRelatedElements(
									acm.getOrigin(), relations);
						} else if (type == IRelation.class) {
							IRelation relation = LocatorService.INSTANCE
									.resolve(acm.getOrigin(), IRelation.class,
											null).get();
							relations = this.getAllRelations();
							neededElements = getRelatedElements(
									relation.getFrom(), relations);
						} else if (type == IRelationInstance.class) {
							IRelationInstance relationInstance = LocatorService.INSTANCE
									.resolve(acm.getOrigin(),
											IRelationInstance.class, null)
									.get();
							relations = this
									.getRelationsByIdentifier(relationInstance
											.getPhenomenon());
							neededElements = getRelatedElements(
									relationInstance.getRelation().getFrom(),
									relations);
						} else {
							relations = this.getRelationsByIdentifier(acm
									.getOrigin());
							neededElements = new HashSet<>();
							for (IRelation relation : relations) {
								neededElements.add(relation.getFrom());
								neededElements.add(relation.getTo());
							}
						}
					}

					Set<URI> neededRelations = this
							.filterExplicitRelations(filterConnectingRelations(
									neededElements, relations));

					this.updateAxialCodingModelFrom(axialCodingComposite,
							neededElements, neededRelations).get();

					acm = new JointJSAxialCodingModel(acmUri,
							axialCodingComposite.getJointjs().getModel());

					return acm;
				});
	}

	@Override
	public Future<Void> updateAxialCodingRelationsFrom(
			AxialCodingComposite axialCodingComposite) {
		return ExecUtils.nonUISyncExec((Callable<Void>) () -> {
			Set<URI> existingElements = new HashSet<URI>(axialCodingComposite
					.getElements().get());
			Set<URI> relations = filterConnectingRelations(existingElements,
					this.getAllRelations());

			relations = this.filterExplicitRelations(relations);

			this.updateAxialCodingModelFrom(axialCodingComposite,
					existingElements, relations).get();

			return null;
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public Future<Void> updateAxialCodingModelFrom(
			final AxialCodingComposite acm, final Set<URI> elements,
			final Set<URI> relations) {
		return ExecUtils.nonUISyncExec((Callable<Void>) () -> {

			List<URI> existingElements = acm.getElements().get();
			List<URI> existingRelations = acm.getRelations().get();

			List<URI> remove = new ArrayList<>();
			remove.addAll(ListUtils.subtract(existingElements, new ArrayList<>(
					elements)));
			remove.addAll(ListUtils.subtract(existingRelations,
					new ArrayList<>(relations)));
			acm.remove(remove);

			List<Pair<URI, Point>> elementsToBeCreated = new ArrayList<>();
			for (Object elementToBeCreated : ListUtils.subtract(
					new ArrayList<>(elements), existingElements)) {
				elementsToBeCreated.add(new Pair<>((URI) elementToBeCreated,
						new Point(0, 0)));
			}
			acm.createElements(elementsToBeCreated);

			List<URI> relationsToBeCreated = new ArrayList<>();
			for (Object relationToBeCreated : ListUtils.subtract(
					new ArrayList<>(relations), existingRelations)) {
				relationsToBeCreated.add((URI) relationToBeCreated);
			}
			acm.createRelations(relationsToBeCreated);

			acm.refresh().get();
			acm.getJointjs().save().get();

			return null;

		});
	}

	/**
	 * Returns the elements that contained in the {@link IRelation}s that are
	 * related without any gaps.
	 *
	 * @param code
	 * @param relations
	 * @return
	 */
	private static Set<URI> getRelatedElements(URI element,
			Collection<IRelation> relations) {
		Set<URI> codes = new HashSet<>();
		if (element == null) {
			return codes;
		}

		codes.add(element);
		boolean codesAdded = true;
		while (codesAdded) {
			codesAdded = false;
			for (IRelation relation : relations) {
				if (codes.contains(relation.getFrom())
						&& !codes.contains(relation.getTo())) {
					codes.add(relation.getTo());
					codesAdded = true;
				}
				if (codes.contains(relation.getTo())
						&& !codes.contains(relation.getFrom())) {
					codes.add(relation.getFrom());
					codesAdded = true;
				}
			}
		}
		return codes;
	}

	/**
	 * Returns the {@link IRelation} that have related elements contained in the
	 * given elements.
	 *
	 * @param elements
	 * @param relations
	 * @return
	 */
	private static Set<URI> filterConnectingRelations(Collection<URI> elements,
			Collection<IRelation> relations) {
		return relations
				.stream()
				.filter(r -> elements.contains(r.getFrom())
						&& elements.contains(r.getTo())).map(r -> r.getUri())
				.collect(Collectors.toSet());
	}

	/**
	 * Returns the {@link IRelation} that have related elements contained in the
	 * given elements.
	 *
	 * @param elements
	 * @param relations
	 * @return
	 */
	private Set<URI> filterExplicitRelations(Collection<URI> relations) {
		return relations
				.stream()
				.filter(r -> this.codeStore.getRelationHierarchyView()
						.getRelationByRelationUri(r) instanceof Relation)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeAxialCodingModel(URI uri) throws CodeStoreWriteException {
		Assert.isNotNull(uri);
		LocatorService.INSTANCE.uncache(uri);
		this.codeStore.setRaw(AXIAL_CODING_MODEL_TYPE, uri, null);
		this.codeServiceListenerNotifier.axialCodingModelRemoved(uri);
	}

	@Override
	public void reattachAndSave(URI src, URI dest) throws CodeServiceException {
		if (src == null || dest == null) {
			throw new CodeServiceException(new IllegalArgumentException(
					"Arguments must not be null"));
		}

		if (LocatorService.INSTANCE != null) {
			LocatorService.INSTANCE.uncache(src);
		}

		// FIXME: codeInstance memos are lost this way (see call hierarchy)
		Set<ICode> codes = this.getExplicitCodes(src);
		this.removeCodes(codes, src);
		this.addCodes(codes, new HashSet<>(Arrays.asList(dest)));

		String memo = this.loadMemo(src);
		this.setMemo(src, null);
		this.setMemo(dest, memo);
		try {
			this.codeStore.save();
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}
	}

	@Override
	public void reassign(List<ICodeInstance> sourceCodeInstances,
			ICode targetCode) {
		for (ICodeInstance sourceCodeInstance : sourceCodeInstances) {
			if (sourceCodeInstance.getCode().equals(targetCode)) {
				continue;
			}

			try {
				URI oldCodeInstanceUri = sourceCodeInstance.getUri();
				String memo = loadMemo(oldCodeInstanceUri);

				URI phenomenon = sourceCodeInstance.getId();
				this.deleteCodeInstance(sourceCodeInstance);

				URI newCodeInstanceUri = this.addCode(targetCode, phenomenon);
				this.setMemo(newCodeInstanceUri, memo);
				this.setMemo(oldCodeInstanceUri, null);
			} catch (CodeServiceException e) {
				LOGGER.error(e);
			}
		}
	}

	@Override
	public void mergeMemos(Set<URI> uris, URI target)
			throws CodeServiceException {
		Assert.isLegal(uris.size() > 0);
		Assert.isLegal(target != null);
		StringBuilder sb = new StringBuilder();
		List<URI> merge = new ArrayList<>();
		for (URI uri : uris) {
			if (isMemo(uri)) {
				merge.add(uri);
			}
		}
		for (URI uri : merge) {
			if (merge.size() > 1) {
				sb.append("<p>------------------------ MERGE START source: "
						+ uri.toString() + "</p>");
			}
			sb.append(loadMemo(uri));
			if (merge.size() > 1) {
				sb.append("<p>------------------------ MERGE END</p>");
			}
		}
		setMemo(target, sb.toString());
	}

	@Override
	public IRelation merge(Set<IRelation> relations, IRelation target)
			throws CodeServiceException {
		Assert.isLegal(relations.size() > 0);

		List<String> names = new ArrayList<>(relations.size());
		if (target != null) {
			names.add(target.getName());
		}
		URI from = relations.iterator().next().getFrom();
		URI to = relations.iterator().next().getTo();
		for (IRelation relation : relations) {
			if (!names.contains(relation.getName())) {
				names.add(relation.getName());
			}
			if (from != null && !from.equals(relation.getFrom())) {
				from = null;
			}
			if (to != null && !to.equals(relation.getTo())) {
				to = null;
			}
		}

		if (from == null) {
			throw new CodeServiceException(
					"The relations to be merged don't share the same "
							+ EndPoint.FROM);
		}
		if (to == null) {
			throw new CodeServiceException(
					"The relations to be merged don't share the same "
							+ EndPoint.TO);
		}

		String name = StringUtils.join(names, " | ");
		try {
			if (target == null) {
				target = createRelation(from, to, name);
			} else {
				renameRelation(target, name);
			}
		} catch (CodeStoreWriteException | RelationDoesNotExistException e) {
			throw new CodeServiceException(e);
		}

		Set<URI> memoUris = relations.stream().map(r -> r.getUri())
				.collect(Collectors.toSet());
		memoUris.add(target.getUri());
		mergeMemos(memoUris, target.getUri());

		for (IRelation relation : relations) {
			if (relation.equals(target)) {
				continue;
			}
			reassign(getAllRelationInstances(relation), target);
			try {
				deleteRelation(relation);
			} catch (CodeStoreWriteException | RelationDoesNotExistException e) {
				throw new CodeServiceException(e);
			}
		}

		return target;
	}

	@Override
	public void reassign(Set<IRelationInstance> sourceRelationInstances,
			IRelation targetRelation) {
		for (IRelationInstance sourceRelationInstance : sourceRelationInstances) {
			if (sourceRelationInstance.getRelation().equals(targetRelation)) {
				continue;
			}

			try {
				URI oldRelationInstanceUri = sourceRelationInstance.getUri();
				String memo = loadMemo(oldRelationInstanceUri);

				URI phenomenon = sourceRelationInstance.getPhenomenon();
				this.deleteRelationInstance(sourceRelationInstance);

				IRelationInstance newRelationInstance = this
						.createRelationInstance(phenomenon, targetRelation);
				setMemo(newRelationInstance.getUri(), memo);
				setMemo(oldRelationInstanceUri, null);
			} catch (CodeServiceException | CodeStoreWriteException
					| RelationInstanceDoesNotExistException
					| RelationDoesNotExistException e) {
				LOGGER.error(e);
			}
		}
	}
}
