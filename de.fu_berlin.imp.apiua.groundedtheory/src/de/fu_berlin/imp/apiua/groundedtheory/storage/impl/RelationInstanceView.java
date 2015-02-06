package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.bkahlert.nebula.utils.DataView;
import com.bkahlert.nebula.utils.IDirtiable;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelationInstance;

/**
 * {@link DataView} that makes parent-child relations available through
 * {@link ICodeInstance}s.
 * <p>
 * e.g. if A is the parent of B, a {@link ICodeInstance} stating that B was
 * coded with A is generated.
 *
 * @author bkahlert
 *
 */
public class RelationInstanceView extends DataView {

	private RelationHierarchyView relationHierarchyView;
	private Set<IRelationInstance> explicitRelationInstances;

	private Set<IRelationInstance> explicitRelationInstancesReadOnly;
	private Set<ImplicitRelationInstance> implicitRelationInstancesReadOnly;
	private Set<IRelationInstance> allRelationInstancesReadOnly;

	private Map<URI, Set<IRelationInstance>> explicitRelationInstancesByRelationInstancePhenomenon;
	private Map<URI, Set<IRelationInstance>> allRelationInstancesByRelationInstancePhenomenon;
	private Map<IIdentifier, Set<IRelationInstance>> explicitRelationInstancesByRelationInstanceIdentifier;
	private Map<IIdentifier, Set<IRelationInstance>> allRelationInstancesByRelationInstanceIdentifier;
	private Map<URI, Set<IRelationInstance>> explicitRelationInstancesByRelationInstanceRelation;
	private Map<URI, Set<ImplicitRelationInstance>> implicitRelationInstancesByRelationInstanceRelation;
	private Map<URI, Set<IRelationInstance>> allRelationInstancesByRelationInstanceRelation;

	private Map<URI, IRelationInstance> allRelationInstancesByRelationInstanceUri;

	private Map<URI, Set<IRelationInstance>> explicitRelationInstancesStartingFrom;
	private Map<URI, Set<IRelationInstance>> allRelationInstancesStartingFrom;
	private Map<URI, Set<IRelationInstance>> explicitRelationInstancesEndingAt;
	private Map<URI, Set<IRelationInstance>> allRelationInstancesEndingAt;

	public RelationInstanceView(RelationHierarchyView relationHierarchyView,
			Set<IRelationInstance> explicitRelationInstances,
			IDirtiable... dirtiables) {
		super(relationHierarchyView, dirtiables);
		this.relationHierarchyView = relationHierarchyView;
		this.explicitRelationInstances = explicitRelationInstances;

	}

	@Override
	protected void refresh() {
		this.explicitRelationInstancesReadOnly = Collections
				.unmodifiableSet(new HashSet<>(this.explicitRelationInstances));

		this.explicitRelationInstancesByRelationInstancePhenomenon = new HashMap<>();
		this.explicitRelationInstancesByRelationInstanceIdentifier = new HashMap<>();
		this.explicitRelationInstancesByRelationInstanceRelation = new HashMap<>();
		this.allRelationInstancesByRelationInstanceRelation = new HashMap<>();

		this.explicitRelationInstancesStartingFrom = new HashMap<>();
		this.explicitRelationInstancesEndingAt = new HashMap<>();

		for (IRelationInstance relationInstance : this.explicitRelationInstancesReadOnly) {
			IRelation relation = relationInstance.getRelation();

			if (!this.explicitRelationInstancesByRelationInstancePhenomenon
					.containsKey(relationInstance.getPhenomenon())) {
				this.explicitRelationInstancesByRelationInstancePhenomenon
						.put(relationInstance.getPhenomenon(),
								new LinkedHashSet<>());
			}
			this.explicitRelationInstancesByRelationInstancePhenomenon.get(
					relationInstance.getPhenomenon()).add(relationInstance);

			IIdentifier id = URIUtils.getIdentifier(relationInstance
					.getPhenomenon());
			if (!this.explicitRelationInstancesByRelationInstanceIdentifier
					.containsKey(id)) {
				this.explicitRelationInstancesByRelationInstanceIdentifier.put(
						id, new LinkedHashSet<>());
			}
			this.explicitRelationInstancesByRelationInstanceIdentifier.get(id)
					.add(relationInstance);

			if (!this.explicitRelationInstancesByRelationInstanceRelation
					.containsKey(relation.getUri())) {
				this.explicitRelationInstancesByRelationInstanceRelation.put(
						relation.getUri(), new LinkedHashSet<>());
			}
			this.explicitRelationInstancesByRelationInstanceRelation.get(
					relation.getUri()).add(relationInstance);

			if (!this.allRelationInstancesByRelationInstanceRelation
					.containsKey(relation.getUri())) {
				this.allRelationInstancesByRelationInstanceRelation.put(
						relation.getUri(), new LinkedHashSet<>());
			}
			this.allRelationInstancesByRelationInstanceRelation.get(
					relation.getUri()).add(relationInstance);

			if (!this.explicitRelationInstancesStartingFrom
					.containsKey(relation.getFrom())) {
				this.explicitRelationInstancesStartingFrom.put(
						relation.getFrom(), new LinkedHashSet<>());
			}
			this.explicitRelationInstancesStartingFrom.get(relation.getFrom())
					.add(relationInstance);

			if (!this.explicitRelationInstancesEndingAt.containsKey(relation
					.getTo())) {
				this.explicitRelationInstancesEndingAt.put(relation.getTo(),
						new LinkedHashSet<>());
			}
			this.explicitRelationInstancesEndingAt.get(relation.getTo()).add(
					relationInstance);
		}

		HashSet<ImplicitRelationInstance> implicitRelationInstances = new HashSet<>();
		HashSet<IRelationInstance> allRelationInstances = new HashSet<>(
				this.explicitRelationInstancesReadOnly);
		this.implicitRelationInstancesByRelationInstanceRelation = new HashMap<>();
		for (IRelation parentRelation : this.relationHierarchyView
				.getExplicitRelations()) {
			for (ImplicitRelation implicitRelation : this.relationHierarchyView
					.getImplicitRelationsByRelationUri(parentRelation)) {
				IRelation explicitRelation = implicitRelation
						.getExplicitRelation();
				for (IRelationInstance relationInstance : this
						.getExplicitRelationInstancesByRelation(explicitRelation
								.getUri())) {
					ImplicitRelationInstance implicitRelationInstance = new ImplicitRelationInstance(
							relationInstance, implicitRelation);
					implicitRelationInstances.add(implicitRelationInstance);
					allRelationInstances.add(implicitRelationInstance);

					if (!this.implicitRelationInstancesByRelationInstanceRelation
							.containsKey(parentRelation.getUri())) {
						this.implicitRelationInstancesByRelationInstanceRelation
								.put(parentRelation.getUri(), new HashSet<>());
					}
					this.implicitRelationInstancesByRelationInstanceRelation
							.get(parentRelation.getUri()).add(
									implicitRelationInstance);

					if (!this.allRelationInstancesByRelationInstanceRelation
							.containsKey(parentRelation.getUri())) {
						this.allRelationInstancesByRelationInstanceRelation
								.put(parentRelation.getUri(), new HashSet<>());
					}
					this.allRelationInstancesByRelationInstanceRelation.get(
							parentRelation.getUri()).add(
							implicitRelationInstance);
				}
			}
		}
		this.implicitRelationInstancesReadOnly = Collections
				.unmodifiableSet(implicitRelationInstances);
		this.allRelationInstancesReadOnly = Collections
				.unmodifiableSet(allRelationInstances);

		this.allRelationInstancesByRelationInstancePhenomenon = new HashMap<>();
		this.allRelationInstancesByRelationInstanceIdentifier = new HashMap<>();
		this.allRelationInstancesByRelationInstanceUri = new HashMap<>();

		this.allRelationInstancesStartingFrom = new HashMap<>();
		this.allRelationInstancesEndingAt = new HashMap<>();

		for (IRelationInstance relationInstance : allRelationInstances) {
			IRelation relation = relationInstance.getRelation();

			if (!this.allRelationInstancesByRelationInstancePhenomenon
					.containsKey(relationInstance.getPhenomenon())) {
				this.allRelationInstancesByRelationInstancePhenomenon
						.put(relationInstance.getPhenomenon(),
								new LinkedHashSet<>());
			}
			this.allRelationInstancesByRelationInstancePhenomenon.get(
					relationInstance.getPhenomenon()).add(relationInstance);

			IIdentifier id = URIUtils.getIdentifier(relationInstance
					.getPhenomenon());
			if (!this.allRelationInstancesByRelationInstanceIdentifier
					.containsKey(id)) {
				this.allRelationInstancesByRelationInstanceIdentifier.put(id,
						new LinkedHashSet<>());
			}
			this.allRelationInstancesByRelationInstanceIdentifier.get(id).add(
					relationInstance);

			this.allRelationInstancesByRelationInstanceUri.put(
					relationInstance.getUri(), relationInstance);

			if (!this.allRelationInstancesStartingFrom.containsKey(relation
					.getFrom())) {
				this.allRelationInstancesStartingFrom.put(relation.getFrom(),
						new LinkedHashSet<>());
			}
			this.allRelationInstancesStartingFrom.get(relation.getFrom()).add(
					relationInstance);

			if (!this.allRelationInstancesEndingAt
					.containsKey(relation.getTo())) {
				this.allRelationInstancesEndingAt.put(relation.getTo(),
						new LinkedHashSet<>());
			}
			this.allRelationInstancesEndingAt.get(relation.getTo()).add(
					relationInstance);
		}
	}

	public Set<IRelationInstance> getExplicitRelationInstances() {
		this.checkAndRefresh();
		return this.explicitRelationInstancesReadOnly;
	}

	public Set<ImplicitRelationInstance> getImplicitRelationInstances() {
		this.checkAndRefresh();
		return this.implicitRelationInstancesReadOnly;
	}

	public Set<IRelationInstance> getAllRelationInstances() {
		this.checkAndRefresh();
		return this.allRelationInstancesReadOnly;
	}

	public IRelationInstance getRelationInstancesByRelationInstanceUri(URI uri) {
		this.checkAndRefresh();
		return this.allRelationInstancesByRelationInstanceUri.get(uri);
	}

	public Set<IRelationInstance> getExplicitRelationInstancesByPhenomenon(
			URI uri) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.explicitRelationInstancesByRelationInstancePhenomenon
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getAllRelationInstancesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.allRelationInstancesByRelationInstancePhenomenon
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getExplicitRelationInstancesByIdentifier(
			IIdentifier id) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.explicitRelationInstancesByRelationInstanceIdentifier
				.get(id);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getAllRelationInstancesByIdentifier(
			IIdentifier id) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.explicitRelationInstancesByRelationInstanceIdentifier
				.get(id);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	/**
	 * If the given {@link IRelation} is an {@link ImplicitRelation} its
	 * explicit {@link IRelation} is returned. Otherwise the {@link IRelation}
	 * itself.
	 * 
	 * @param uri
	 * @return
	 */
	private IRelation getExplicitRelation(URI uri) {
		IRelation relation = this.relationHierarchyView
				.getRelationByRelationUri(uri);
		if (relation instanceof ImplicitRelation) {
			relation = ((ImplicitRelation) relation).getExplicitRelation();
		}
		return relation;
	}

	/**
	 * Returns the explicit {@link IRelationInstance}s with the given
	 * {@link URI} as their phenomenon.
	 *
	 * @param uri
	 * @return
	 */
	public Set<IRelationInstance> getExplicitRelationInstancesByRelation(URI uri) {
		this.checkAndRefresh();
		uri = this.getExplicitRelation(uri).getUri();
		Set<IRelationInstance> relationInstances = this.explicitRelationInstancesByRelationInstanceRelation
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	/**
	 * Returns the {@link ImplicitRelationInstance}s of the given top
	 * {@link IRelation}. That is, {@link IRelationInstance}s of
	 * {@link ImplicitRelation}s that partially ground a more general
	 * {@link IRelation}.
	 *
	 * @param uri
	 * @return
	 */
	public Set<ImplicitRelationInstance> getImplicitRelationInstancesByRelation(
			URI uri) {
		this.checkAndRefresh();
		uri = this.getExplicitRelation(uri).getUri();
		Set<ImplicitRelationInstance> relationInstances = this.implicitRelationInstancesByRelationInstanceRelation
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	/**
	 * Returns the explicit and implicit {@link IRelationInstance}s with the
	 * given {@link IRelation} as their phenomenon.
	 *
	 * @param uri
	 * @return
	 */
	public Set<IRelationInstance> getAllRelationInstancesByRelation(URI uri) {
		this.checkAndRefresh();
		uri = this.getExplicitRelation(uri).getUri();
		Set<IRelationInstance> relationInstances = this.allRelationInstancesByRelationInstanceRelation
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getExplicitRelationInstancesStartingFrom(
			URI uri) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.explicitRelationInstancesStartingFrom
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getAllRelationInstancesStartingFrom(URI uri) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.allRelationInstancesStartingFrom
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getExplicitRelationInstancesEndingAt(URI uri) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.explicitRelationInstancesEndingAt
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

	public Set<IRelationInstance> getAllRelationInstancesEndingAt(URI uri) {
		this.checkAndRefresh();
		Set<IRelationInstance> relationInstances = this.allRelationInstancesEndingAt
				.get(uri);
		return relationInstances != null ? Collections
				.unmodifiableSet(relationInstances) : Collections.emptySet();
	}

}
