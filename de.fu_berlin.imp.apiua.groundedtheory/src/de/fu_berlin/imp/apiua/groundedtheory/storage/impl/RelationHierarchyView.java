package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bkahlert.nebula.lang.ListHashMap;
import com.bkahlert.nebula.lang.SetHashMap;
import com.bkahlert.nebula.utils.DataView;
import com.bkahlert.nebula.utils.IDirtiable;
import com.bkahlert.nebula.utils.Pair;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ProposedRelation;
import de.fu_berlin.imp.apiua.groundedtheory.views.MergedProposedRelation;

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
public class RelationHierarchyView extends DataView {

	private CodeHierarchyView codeHierarchyView;
	private Set<IRelation> explicitRelations;

	private Set<IRelation> explicitRelationsReadOnly;
	private Set<ImplicitRelation> implicitRelationsReadOnly;
	private Set<ProposedRelation> proposedRelationsReadOnly;
	private Set<MergedProposedRelation> mergedProposedRelationsReadOnly;
	private Set<IRelation> allRelationsReadOnly;

	private Map<URI, IRelation> relationMappings;
	private SetHashMap<URI, ImplicitRelation> implicitRelationMappings;
	private SetHashMap<Pair<URI, URI>, ProposedRelation> proposedRelationsMappings;
	private SetHashMap<Pair<URI, URI>, MergedProposedRelation> mergedProposedRelationsMappings;
	private SetHashMap<URI, IRelation> allRelationMappings;

	private SetHashMap<URI, IRelation> explicitRelationsStartingFrom;
	private SetHashMap<URI, IRelation> explicitRelationsEndingAt;
	private SetHashMap<Pair<URI, URI>, IRelation> explicitRelationsStartingFromEndingAt;
	private SetHashMap<URI, IRelation> allRelationsStartingFrom;
	private SetHashMap<URI, IRelation> allRelationsEndingAt;
	private SetHashMap<Pair<URI, URI>, IRelation> allRelationsStartingFromEndingAt;

	public RelationHierarchyView(CodeHierarchyView codeHierarchyView,
			Set<IRelation> explicitRelations, IDirtiable... dirtiables) {
		super(codeHierarchyView, dirtiables);
		this.codeHierarchyView = codeHierarchyView;
		this.explicitRelations = explicitRelations;
	}

	@Override
	protected void refresh() {
		this.explicitRelationsReadOnly = Collections
				.unmodifiableSet(new HashSet<>(this.explicitRelations));

		HashSet<ImplicitRelation> implicitRelations = new HashSet<>();
		HashSet<ProposedRelation> proposedRelations = new HashSet<>();
		HashSet<MergedProposedRelation> mergedProposedRelations = new HashSet<>();
		HashSet<IRelation> allRelations = new HashSet<>();

		this.implicitRelationMappings = new SetHashMap<>();
		this.proposedRelationsMappings = new SetHashMap<>();
		this.mergedProposedRelationsMappings = new SetHashMap<>();
		this.allRelationMappings = new SetHashMap<>();

		this.explicitRelationsStartingFrom = new SetHashMap<>();
		this.explicitRelationsEndingAt = new SetHashMap<>();
		this.explicitRelationsStartingFromEndingAt = new SetHashMap<>();

		for (IRelation relation : this.explicitRelationsReadOnly) {
			allRelations.add(relation);

			this.allRelationMappings.addTo(relation.getUri(), relation);

			this.explicitRelationsStartingFrom.addTo(relation.getFrom(),
					relation);
			this.explicitRelationsEndingAt.addTo(relation.getTo(), relation);
			this.explicitRelationsStartingFromEndingAt.addTo(new Pair<>(
					relation.getFrom(), relation.getTo()), relation);

			try {
				List<URI> fromAncestors = this.codeHierarchyView
						.getAncestors(
								LocatorService.INSTANCE.resolve(
										relation.getFrom(), ICode.class, null)
										.get()).stream().map(c -> c.getUri())
						.collect(Collectors.toList());
				List<URI> toAncestors = this.codeHierarchyView
						.getAncestors(
								LocatorService.INSTANCE.resolve(
										relation.getTo(), ICode.class, null)
										.get()).stream().map(c -> c.getUri())
						.collect(Collectors.toList());

				// find existing explicit relations this relation is implicit
				// for
				this.explicitRelationsReadOnly
						.stream()
						.filter(r -> (fromAncestors.contains(r.getFrom())
								&& toAncestors.contains(r.getTo())
								|| relation.getFrom().equals(r.getFrom())
								&& toAncestors.contains(r.getTo()) || fromAncestors
								.contains(r.getFrom())
								&& relation.getTo().equals(r.getTo()))
								&& relation.getName().equals(r.getName()))
						.forEach(
								topRelation -> {
									ImplicitRelation implicitRelation = new ImplicitRelation(
											relation, topRelation);
									implicitRelations.add(implicitRelation);
									allRelations.add(implicitRelation);

									this.implicitRelationMappings.addTo(
											implicitRelation.getUri(),
											implicitRelation);

									this.implicitRelationMappings.addTo(
											topRelation.getUri(),
											implicitRelation);

									this.allRelationMappings.addTo(
											implicitRelation.getUri(),
											implicitRelation);

									this.allRelationMappings.addTo(
											topRelation.getUri(),
											implicitRelation);
								});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		for (IRelation relation : this.explicitRelationsReadOnly) {
			try {
				List<URI> fromAncestors = this.codeHierarchyView
						.getAncestors(
								LocatorService.INSTANCE.resolve(
										relation.getFrom(), ICode.class, null)
										.get()).stream().map(c -> c.getUri())
						.collect(Collectors.toList());
				List<URI> toAncestors = this.codeHierarchyView
						.getAncestors(
								LocatorService.INSTANCE.resolve(
										relation.getTo(), ICode.class, null)
										.get()).stream().map(c -> c.getUri())
						.collect(Collectors.toList());

				fromAncestors.add(relation.getFrom());
				toAncestors.add(relation.getTo());

				for (URI from : fromAncestors) {
					for (URI to : toAncestors) {
						if (from.equals(to)) {
							continue;
						}

						// no proposed relations for existing relations
						if (this.explicitRelations.stream().anyMatch(
								r -> r.getFrom().equals(from)
										&& r.getTo().equals(to)
										&& r.getName().equals(
												relation.getName()))) {
							continue;
						}

						ProposedRelation proposedRelation = new ProposedRelation(
								relation, from, to);
						proposedRelations.add(proposedRelation);
						allRelations.add(proposedRelation);

						this.proposedRelationsMappings.addTo(new Pair<>(from,
								to), proposedRelation);

						this.allRelationMappings.addTo(
								proposedRelation.getUri(), proposedRelation);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// calculate merged proposed relations
		for (Pair<URI, URI> proposedEndPoints : this.proposedRelationsMappings
				.keySet()) {
			ListHashMap<String, ProposedRelation> proposedRelationsByName = new ListHashMap<>();
			for (ProposedRelation proposedRelation : this.proposedRelationsMappings
					.get(proposedEndPoints)) {
				proposedRelationsByName.addTo(proposedRelation.getName(),
						proposedRelation);
			}

			for (String name : proposedRelationsByName.keySet()) {
				List<ProposedRelation> tmp = proposedRelationsByName.get(name);
				if (tmp.size() > 0) {
					MergedProposedRelation mergedProposedRelation = new MergedProposedRelation(
							tmp);
					mergedProposedRelations.add(mergedProposedRelation);
					allRelations.add(mergedProposedRelation);
					this.mergedProposedRelationsMappings.addTo(
							proposedEndPoints, mergedProposedRelation);
				}
			}
		}

		this.implicitRelationsReadOnly = Collections
				.unmodifiableSet(implicitRelations);
		this.proposedRelationsReadOnly = Collections
				.unmodifiableSet(proposedRelations);
		this.mergedProposedRelationsReadOnly = Collections
				.unmodifiableSet(mergedProposedRelations);
		this.allRelationsReadOnly = Collections.unmodifiableSet(allRelations);

		this.relationMappings = new HashMap<>();
		this.allRelationsStartingFrom = new SetHashMap<>();
		this.allRelationsEndingAt = new SetHashMap<>();
		this.allRelationsStartingFromEndingAt = new SetHashMap<>();
		for (IRelation relation : allRelations) {
			this.relationMappings.put(relation.getUri(), relation);

			this.allRelationsStartingFrom.addTo(relation.getFrom(), relation);
			this.allRelationsEndingAt.addTo(relation.getTo(), relation);
			this.allRelationsStartingFromEndingAt.addTo(
					new Pair<>(relation.getFrom(), relation.getTo()), relation);
		}
	}

	public Set<IRelation> getExplicitRelations() {
		this.checkAndRefresh();
		return this.explicitRelationsReadOnly;
	}

	public Set<ImplicitRelation> getImplicitRelations() {
		this.checkAndRefresh();
		return this.implicitRelationsReadOnly;
	}

	public Set<ProposedRelation> getProposedRelations() {
		return this.proposedRelationsReadOnly;
	}

	public Set<MergedProposedRelation> getMergedProposedRelations() {
		return this.mergedProposedRelationsReadOnly;
	}

	public Set<IRelation> getAllRelations() {
		this.checkAndRefresh();
		return this.allRelationsReadOnly;
	}

	public IRelation getRelationByRelationUri(URI uri) {
		this.checkAndRefresh();
		return this.relationMappings.get(uri);
	}

	/**
	 * Returns all {@link ImplicitRelation}s describing the given
	 * {@link IRelation} described by the given {@link URI}.
	 *
	 * @param uri
	 * @return
	 */
	public Set<ImplicitRelation> getImplicitRelationsByRelationUri(URI uri) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.implicitRelationMappings
				.get(uri));
	}

	/**
	 * Returns all {@link ImplicitRelation}s describing the given
	 * {@link IRelation}.
	 *
	 * @param relation
	 * @return
	 */
	public Set<ImplicitRelation> getImplicitRelationsByRelationUri(
			IRelation relation) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this
				.getImplicitRelationsByRelationUri(relation.getUri()));
	}

	/**
	 * Returns all {@link ProposedRelation}s for a possible {@link IRelation}
	 * between <code>from</code> and <code>to</code>.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public Set<ProposedRelation> getProposedRelation(URI from, URI to) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.proposedRelationsMappings
				.get(new Pair<>(from, to)));
	}

	/**
	 * Returns all {@link MergedProposedRelation}s for a possible
	 * {@link IRelation} between <code>from</code> and <code>to</code>.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public Set<MergedProposedRelation> getMergedProposedRelation(URI from,
			URI to) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.mergedProposedRelationsMappings
				.get(new Pair<>(from, to)));
	}

	/**
	 * Returns all {@link ProposedRelation}s for all possible {@link IRelation}s
	 * between the <code>froms</code> and <code>tos</code>.
	 * {@link ProposedRelation}s that model an explicit {@link IRelation} are
	 * excluded.
	 *
	 * @param froms
	 * @param tos
	 * @param maxRelationsBetweenTwoElements
	 *            return max for proposed relations between two elements
	 * @return
	 */
	public Set<ProposedRelation> getProposedRelation(Collection<URI> froms,
			Collection<URI> tos, int maxRelationsBetweenTwoElements) {
		this.checkAndRefresh();
		Set<IRelation> explicitRelations = new HashSet<>();
		Set<ProposedRelation> proposedRelations = new HashSet<>();

		for (URI from : froms) {
			for (URI to : tos) {
				explicitRelations
						.addAll(this.explicitRelationsStartingFromEndingAt
								.get(new Pair<>(from, to)));
				proposedRelations.addAll(this.proposedRelationsMappings
						.get(new Pair<>(from, to)));
			}
		}

		SetHashMap<Pair<URI, URI>, ProposedRelation> proposedRelations2 = new SetHashMap<>();
		for (ProposedRelation proposedRelation : proposedRelations) {
			proposedRelations2.addTo(new Pair<>(proposedRelation.getFrom(),
					proposedRelation.getTo()), proposedRelation);
		}

		for (IRelation explicitRelation : explicitRelations) {
			for (Iterator<ProposedRelation> iterator = proposedRelations
					.iterator(); iterator.hasNext();) {
				ProposedRelation proposedRelation = iterator.next();
				if (this.isSubRelationSameFrom(explicitRelation,
						proposedRelation)
						|| this.isSubRelationSameTo(explicitRelation,
								proposedRelation)) {
					iterator.remove();
					proposedRelations2
							.removeFrom(new Pair<>(proposedRelation.getFrom(),
									proposedRelation.getTo()), proposedRelation);
				}
			}
		}

		proposedRelations = new HashSet<>();
		if (maxRelationsBetweenTwoElements >= 0) {
			for (Pair<URI, URI> fromTo : proposedRelations2.keySet()) {
				int i = 0;
				for (ProposedRelation proposedRelation2 : proposedRelations2
						.get(fromTo)) {
					if (i >= maxRelationsBetweenTwoElements) {
						break;
					}
					proposedRelations.add(proposedRelation2);
					i++;
				}
			}
		}

		return Collections.unmodifiableSet(proposedRelations);
	}

	/**
	 * Returns all {@link MergedProposedRelation}s for all possible
	 * {@link IRelation}s between the <code>froms</code> and <code>tos</code>.
	 * {@link ProposedRelation}s that model an explicit {@link IRelation} are
	 * excluded.
	 *
	 * @param froms
	 * @param tos
	 * @param maxRelationsBetweenTwoElements
	 *            return max for proposed relations between two elements
	 * @return
	 */
	public Set<MergedProposedRelation> getMergedProposedRelation(
			Collection<URI> froms, Collection<URI> tos) {
		this.checkAndRefresh();
		Set<MergedProposedRelation> mergedProposedRelations = new HashSet<>();
		for (URI from : froms) {
			for (URI to : tos) {
				mergedProposedRelations
						.addAll(this.mergedProposedRelationsMappings
								.get(new Pair<>(from, to)));
			}
		}
		return Collections.unmodifiableSet(mergedProposedRelations);
	}

	/**
	 * Returns the {@link IRelation} described by the given {@link URI} and all
	 * {@link ImplicitRelation}s belonging to it.
	 *
	 * @param uri
	 * @return
	 */
	public Set<IRelation> getAllRelationsByRelationUri(URI uri) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.allRelationMappings.get(uri));
	}

	/**
	 * Returns the given {@link IRelation} and all {@link ImplicitRelation}s
	 * describing it.
	 *
	 * @param uri
	 * @return
	 */
	public Set<IRelation> getAllRelationsByRelation(IRelation relation) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this
				.getAllRelationsByRelationUri(relation.getUri()));
	}

	public Set<IRelation> getExplicitRelationsStartingFrom(URI from) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.explicitRelationsStartingFrom
				.get(from));
	}

	public Set<IRelation> getExplicitRelationsEndingAt(URI to) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.explicitRelationsEndingAt
				.get(to));
	}

	public Set<IRelation> getAllRelationsStartingFrom(URI uri) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.allRelationsStartingFrom
				.get(uri));
	}

	public Set<IRelation> getAllRelationsEndingAt(URI uri) {
		this.checkAndRefresh();
		return Collections.unmodifiableSet(this.allRelationsEndingAt.get(uri));
	}

	/**
	 * Returns <code>true</code> if the given sub {@link IRelation} is indeed a
	 * sub {@link IRelation} of the given ancestorRelation {@link IRelation}.
	 * Both {@link IRelation}s must share the same {@link IRelation#getFrom()}.
	 *
	 * @param parentRelation
	 * @param subRelation
	 * @return
	 */
	public boolean isSubRelationSameFrom(IRelation ancestorRelation,
			IRelation subRelation) {
		if (!ancestorRelation.getName().equals(subRelation.getName())) {
			return false;
		}

		if (ancestorRelation.getFrom().equals(subRelation.getFrom())) {
			return this.codeHierarchyView.getDescendents(
					ancestorRelation.getTo()).contains(subRelation.getTo());
		}

		return false;
	}

	/**
	 * Returns <code>true</code> if the given sub {@link IRelation} is indeed a
	 * sub {@link IRelation} of the given ancestorRelation {@link IRelation}.
	 * Both {@link IRelation}s must share the same {@link IRelation#getTo()}.
	 *
	 * @param parentRelation
	 * @param subRelation
	 * @return
	 */
	public boolean isSubRelationSameTo(IRelation ancestorRelation,
			IRelation subRelation) {
		if (!ancestorRelation.getName().equals(subRelation.getName())) {
			return false;
		}

		if (ancestorRelation.getTo().equals(subRelation.getTo())) {
			return this.codeHierarchyView.getDescendents(
					ancestorRelation.getFrom()).contains(subRelation.getFrom());
		}

		return false;
	}

	/**
	 * Returns <code>true</code> if the given sub {@link IRelation} is indeed a
	 * sub {@link IRelation} of the given ancestorRelation {@link IRelation}.
	 *
	 * @param parentRelation
	 * @param subRelation
	 * @return
	 */
	public boolean isSubRelation(IRelation ancestorRelation,
			IRelation subRelation) {
		if (!ancestorRelation.getName().equals(subRelation.getName())) {
			return false;
		}

		// from and to equals
		if (ancestorRelation.getFrom().equals(subRelation.getFrom())
				&& ancestorRelation.getTo().equals(subRelation.getTo())) {
			return false;
		}

		List<URI> fromDescendants = null;
		List<URI> toDescendants = null;

		// only from equals
		if (ancestorRelation.getFrom().equals(subRelation.getFrom())) {
			toDescendants = this.codeHierarchyView
					.getDescendents(ancestorRelation.getTo());
			if (toDescendants.contains(subRelation.getTo())) {
				return true;
			}
		}
		// only to equals
		if (ancestorRelation.getTo().equals(subRelation.getTo())) {
			fromDescendants = this.codeHierarchyView
					.getDescendents(ancestorRelation.getFrom());
			if (fromDescendants.contains(subRelation.getFrom())) {
				return true;
			}
		}
		// neither from nor to equals
		if (fromDescendants == null) {
			fromDescendants = this.codeHierarchyView
					.getDescendents(ancestorRelation.getFrom());
		}
		if (toDescendants == null) {
			toDescendants = this.codeHierarchyView
					.getDescendents(ancestorRelation.getTo());
		}
		if (fromDescendants.contains(subRelation.getFrom())) {
			if (toDescendants.contains(subRelation.getTo())) {
				return true;
			}
		}
		return false;
	}

}