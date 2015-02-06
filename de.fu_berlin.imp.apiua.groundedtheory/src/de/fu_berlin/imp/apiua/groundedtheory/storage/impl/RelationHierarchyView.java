package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bkahlert.nebula.utils.DataView;
import com.bkahlert.nebula.utils.IDirtiable;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelation;

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
	private Set<IRelation> allRelationsReadOnly;

	private Map<URI, IRelation> relationMappings;
	private Map<URI, Set<ImplicitRelation>> implicitRelationMappings;
	private Map<URI, Set<IRelation>> allRelationMappings;

	private Map<URI, Set<IRelation>> explicitRelationsStartingFrom;
	private Map<URI, Set<IRelation>> explicitRelationsEndingAt;
	private Map<URI, Set<IRelation>> allRelationsStartingFrom;
	private Map<URI, Set<IRelation>> allRelationsEndingAt;

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
		HashSet<IRelation> allRelations = new HashSet<>();

		this.implicitRelationMappings = new HashMap<>();
		this.allRelationMappings = new HashMap<>();

		this.explicitRelationsStartingFrom = new HashMap<>();
		this.explicitRelationsEndingAt = new HashMap<>();

		for (IRelation relation : this.explicitRelationsReadOnly) {
			allRelations.add(relation);

			if (!this.allRelationMappings.containsKey(relation.getUri())) {
				this.allRelationMappings.put(relation.getUri(),
						new LinkedHashSet<>());
			}
			this.allRelationMappings.get(relation.getUri()).add(relation);

			if (!this.explicitRelationsStartingFrom.containsKey(relation
					.getFrom())) {
				this.explicitRelationsStartingFrom.put(relation.getFrom(),
						new HashSet<>());
			}
			this.explicitRelationsStartingFrom.get(relation.getFrom()).add(
					relation);

			if (!this.explicitRelationsEndingAt.containsKey(relation.getTo())) {
				this.explicitRelationsEndingAt.put(relation.getTo(),
						new HashSet<>());
			}
			this.explicitRelationsEndingAt.get(relation.getTo()).add(relation);

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
						.filter(r -> ((fromAncestors.contains(r.getFrom()) && toAncestors
								.contains(r.getTo()))
								|| (relation.getFrom().equals(r.getFrom()) && toAncestors
										.contains(r.getTo())) || (fromAncestors
								.contains(r.getFrom()) && relation.getTo()
								.equals(r.getTo())))
								&& relation.getName().equals(r.getName()))
						.forEach(
								topRelation -> {
									ImplicitRelation implicitRelation = new ImplicitRelation(
											relation, topRelation);
									implicitRelations.add(implicitRelation);
									allRelations.add(implicitRelation);

									if (!this.implicitRelationMappings
											.containsKey(topRelation.getUri())) {
										this.implicitRelationMappings.put(
												topRelation.getUri(),
												new LinkedHashSet<>());
									}
									this.implicitRelationMappings.get(
											topRelation.getUri()).add(
											implicitRelation);

									if (!this.allRelationMappings
											.containsKey(topRelation.getUri())) {
										this.allRelationMappings.put(
												topRelation.getUri(),
												new LinkedHashSet<>());
									}
									this.allRelationMappings.get(
											topRelation.getUri()).add(
											implicitRelation);
								});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		this.implicitRelationsReadOnly = Collections
				.unmodifiableSet(implicitRelations);
		this.allRelationsReadOnly = Collections.unmodifiableSet(allRelations);

		this.relationMappings = new HashMap<>();
		this.allRelationsStartingFrom = new HashMap<>();
		this.allRelationsEndingAt = new HashMap<>();
		for (IRelation relation : allRelations) {
			this.relationMappings.put(relation.getUri(), relation);

			if (!this.allRelationsStartingFrom.containsKey(relation.getFrom())) {
				this.allRelationsStartingFrom.put(relation.getFrom(),
						new HashSet<>());
			}
			this.allRelationsStartingFrom.get(relation.getFrom()).add(relation);

			if (!this.allRelationsEndingAt.containsKey(relation.getTo())) {
				this.allRelationsEndingAt
						.put(relation.getTo(), new HashSet<>());
			}
			this.allRelationsEndingAt.get(relation.getTo()).add(relation);
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
		Set<ImplicitRelation> implicitRelations = this.implicitRelationMappings
				.get(uri);
		return implicitRelations != null ? Collections
				.unmodifiableSet(implicitRelations) : Collections.emptySet();
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
		return this.getImplicitRelationsByRelationUri(relation.getUri());
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
		Set<IRelation> allRelations = this.allRelationMappings.get(uri);
		return allRelations != null ? Collections.unmodifiableSet(allRelations)
				: Collections.emptySet();
	}

	/**
	 * Returns the given {@link IRelation} and all {@link ImplicitRelation}s
	 * describing it.
	 *
	 * @param uri
	 * @return
	 */
	public Set<IRelation> getAllRelationsByRelation(IRelation relation) {
		return this.getAllRelationsByRelationUri(relation.getUri());
	}

	public Set<IRelation> getExplicitRelationsStartingFrom(URI from) {
		this.checkAndRefresh();
		Set<IRelation> relations = this.explicitRelationsStartingFrom.get(from);
		return relations != null ? Collections.unmodifiableSet(relations)
				: Collections.emptySet();
	}

	public Set<IRelation> getExplicitRelationsEndingAt(URI to) {
		this.checkAndRefresh();
		Set<IRelation> relations = this.explicitRelationsEndingAt.get(to);
		return relations != null ? Collections.unmodifiableSet(relations)
				: Collections.emptySet();
	}

	public Set<IRelation> getAllRelationsStartingFrom(URI uri) {
		this.checkAndRefresh();
		Set<IRelation> relations = this.allRelationsStartingFrom.get(uri);
		return relations != null ? Collections.unmodifiableSet(relations)
				: Collections.emptySet();
	}

	public Set<IRelation> getAllRelationsEndingAt(URI uri) {
		this.checkAndRefresh();
		Set<IRelation> relations = this.allRelationsEndingAt.get(uri);
		return relations != null ? Collections.unmodifiableSet(relations)
				: Collections.emptySet();
	}

}