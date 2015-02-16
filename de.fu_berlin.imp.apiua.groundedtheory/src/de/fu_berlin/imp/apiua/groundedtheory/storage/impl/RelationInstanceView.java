package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bkahlert.nebula.lang.SetHashMap;
import com.bkahlert.nebula.utils.DataView;
import com.bkahlert.nebula.utils.IDirtiable;
import com.bkahlert.nebula.utils.Pair;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ProposedRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ProposedRelationInstance;

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
	private Set<ProposedRelationInstance> proposedRelationInstancesReadOnly;
	private Set<IRelationInstance> allRelationInstancesReadOnly;

	private SetHashMap<URI, IRelationInstance> explicitRelationInstancesByRelationInstancePhenomenon;
	private SetHashMap<URI, IRelationInstance> allRelationInstancesByRelationInstancePhenomenon;
	private SetHashMap<IIdentifier, IRelationInstance> explicitRelationInstancesByRelationInstanceIdentifier;
	private SetHashMap<IIdentifier, IRelationInstance> allRelationInstancesByRelationInstanceIdentifier;
	private SetHashMap<URI, IRelationInstance> explicitRelationInstancesByRelationInstanceRelation;
	private SetHashMap<URI, ImplicitRelationInstance> implicitRelationInstancesByRelationInstanceRelation;
	private SetHashMap<Pair<URI, URI>, ProposedRelationInstance> proposedRelationInstancesByFromTo;
	private SetHashMap<URI, IRelationInstance> allRelationInstancesByRelationInstanceRelation;

	private Map<URI, IRelationInstance> allRelationInstancesByRelationInstanceUri;

	private SetHashMap<URI, IRelationInstance> explicitRelationInstancesStartingFrom;
	private SetHashMap<URI, IRelationInstance> explicitRelationInstancesEndingAt;

	private SetHashMap<URI, IRelationInstance> allRelationInstancesStartingFrom;
	private SetHashMap<URI, IRelationInstance> allRelationInstancesEndingAt;

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

		this.explicitRelationInstancesByRelationInstancePhenomenon = new SetHashMap<>();
		this.explicitRelationInstancesByRelationInstanceIdentifier = new SetHashMap<>();
		this.explicitRelationInstancesByRelationInstanceRelation = new SetHashMap<>();
		this.allRelationInstancesByRelationInstanceRelation = new SetHashMap<>();

		this.explicitRelationInstancesStartingFrom = new SetHashMap<>();
		this.explicitRelationInstancesEndingAt = new SetHashMap<>();

		for (IRelationInstance relationInstance : this.explicitRelationInstancesReadOnly) {
			IRelation relation = relationInstance.getRelation();

			this.explicitRelationInstancesByRelationInstancePhenomenon.addTo(
					relationInstance.getPhenomenon(), relationInstance);
			this.explicitRelationInstancesByRelationInstanceIdentifier.addTo(
					URIUtils.getIdentifier(relationInstance.getPhenomenon()),
					relationInstance);
			this.explicitRelationInstancesByRelationInstanceRelation.addTo(
					relation.getUri(), relationInstance);
			this.allRelationInstancesByRelationInstanceRelation.addTo(
					relation.getUri(), relationInstance);

			this.explicitRelationInstancesStartingFrom.addTo(
					relation.getFrom(), relationInstance);
			this.explicitRelationInstancesEndingAt.addTo(relation.getTo(),
					relationInstance);
		}

		HashSet<ImplicitRelationInstance> implicitRelationInstances = new HashSet<>();
		HashSet<ProposedRelationInstance> proposedRelationInstances = new HashSet<>();
		HashSet<IRelationInstance> allRelationInstances = new HashSet<>(
				this.explicitRelationInstancesReadOnly);

		this.implicitRelationInstancesByRelationInstanceRelation = new SetHashMap<>();
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

					this.implicitRelationInstancesByRelationInstanceRelation
							.addTo(implicitRelation.getUri(),
									implicitRelationInstance);
					this.implicitRelationInstancesByRelationInstanceRelation
							.addTo(parentRelation.getUri(),
									implicitRelationInstance);
					this.allRelationInstancesByRelationInstanceRelation
							.addTo(implicitRelation.getUri(),
									implicitRelationInstance);
					this.allRelationInstancesByRelationInstanceRelation.addTo(
							parentRelation.getUri(), implicitRelationInstance);
				}
			}
		}

		this.proposedRelationInstancesByFromTo = new SetHashMap<>();
		for (ProposedRelation proposedRelation : this.relationHierarchyView
				.getProposedRelations()) {
			for (IRelationInstance relationInstance : this
					.getAllRelationInstancesByRelation(proposedRelation
							.getExplicitRelation())) {
				ProposedRelationInstance proposedRelationInstance = new ProposedRelationInstance(
						relationInstance, proposedRelation);
				proposedRelationInstances.add(proposedRelationInstance);
				allRelationInstances.add(proposedRelationInstance);

				this.proposedRelationInstancesByFromTo.addTo(new Pair<>(
						proposedRelation.getFrom(), proposedRelation.getTo()),
						proposedRelationInstance);

				this.allRelationInstancesByRelationInstanceRelation.addTo(
						proposedRelation.getUri(), proposedRelationInstance);
			}
		}

		this.implicitRelationInstancesReadOnly = Collections
				.unmodifiableSet(implicitRelationInstances);
		this.proposedRelationInstancesReadOnly = Collections
				.unmodifiableSet(proposedRelationInstances);
		this.allRelationInstancesReadOnly = Collections
				.unmodifiableSet(allRelationInstances);

		this.allRelationInstancesByRelationInstancePhenomenon = new SetHashMap<>();
		this.allRelationInstancesByRelationInstanceIdentifier = new SetHashMap<>();
		this.allRelationInstancesByRelationInstanceUri = new HashMap<>();

		this.allRelationInstancesStartingFrom = new SetHashMap<>();
		this.allRelationInstancesEndingAt = new SetHashMap<>();

		for (IRelationInstance relationInstance : allRelationInstances) {
			IRelation relation = relationInstance.getRelation();

			this.allRelationInstancesByRelationInstancePhenomenon.addTo(
					relationInstance.getPhenomenon(), relationInstance);

			this.allRelationInstancesByRelationInstanceIdentifier.addTo(
					URIUtils.getIdentifier(relationInstance.getPhenomenon()),
					relationInstance);

			this.allRelationInstancesByRelationInstanceUri.put(
					relationInstance.getUri(), relationInstance);

			this.allRelationInstancesStartingFrom.addTo(relation.getFrom(),
					relationInstance);
			this.allRelationInstancesEndingAt.addTo(relation.getTo(),
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

	public Set<ProposedRelationInstance> getProposedRelationInstances() {
		this.checkAndRefresh();
		return this.proposedRelationInstancesReadOnly;
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
		return Collections
				.unmodifiableSet(this.explicitRelationInstancesByRelationInstancePhenomenon
						.get(uri));
	}

	public Set<IRelationInstance> getAllRelationInstancesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.allRelationInstancesByRelationInstancePhenomenon
						.get(uri));
	}

	public Set<IRelationInstance> getExplicitRelationInstancesByIdentifier(
			IIdentifier id) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.explicitRelationInstancesByRelationInstanceIdentifier
						.get(id));
	}

	public Set<IRelationInstance> getAllRelationInstancesByIdentifier(
			IIdentifier id) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.explicitRelationInstancesByRelationInstanceIdentifier
						.get(id));
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
		return Collections
				.unmodifiableSet(this.explicitRelationInstancesByRelationInstanceRelation
						.get(uri));
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
		return Collections
				.unmodifiableSet(this.implicitRelationInstancesByRelationInstanceRelation
						.get(uri));
	}

	public Set<ProposedRelationInstance> getProposedRelationInstances(URI from,
			URI to) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.proposedRelationInstancesByFromTo
						.get(new Pair<>(from, to)));
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
		return Collections
				.unmodifiableSet(this.allRelationInstancesByRelationInstanceRelation
						.get(uri));
	}

	/**
	 * Returns the explicit and implicit {@link IRelationInstance}s with the
	 * given {@link IRelation} as their phenomenon.
	 *
	 * @param uri
	 * @return
	 */
	public Set<IRelationInstance> getAllRelationInstancesByRelation(
			IRelation relation) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this
				.getAllRelationInstancesByRelation(relation.getUri()));
	}

	public Set<IRelationInstance> getExplicitRelationInstancesStartingFrom(
			URI uri) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.explicitRelationInstancesStartingFrom
						.get(uri));
	}

	public Set<IRelationInstance> getAllRelationInstancesStartingFrom(URI uri) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.allRelationInstancesStartingFrom.get(uri));
	}

	public Set<IRelationInstance> getExplicitRelationInstancesEndingAt(URI uri) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.explicitRelationInstancesEndingAt
						.get(uri));
	}

	public Set<IRelationInstance> getAllRelationInstancesEndingAt(URI uri) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.allRelationInstancesEndingAt
				.get(uri));
	}

}
