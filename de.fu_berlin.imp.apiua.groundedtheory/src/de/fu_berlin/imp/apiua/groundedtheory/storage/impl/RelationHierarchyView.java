package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bkahlert.nebula.lang.SetHashMap;
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
	private SetHashMap<URI, ImplicitRelation> implicitRelationMappings;
	private SetHashMap<URI, IRelation> allRelationMappings;

	private SetHashMap<URI, IRelation> explicitRelationsStartingFrom;
	private SetHashMap<URI, IRelation> explicitRelationsEndingAt;
	private SetHashMap<URI, IRelation> allRelationsStartingFrom;
	private SetHashMap<URI, IRelation> allRelationsEndingAt;

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

		this.implicitRelationMappings = new SetHashMap<>();
		this.allRelationMappings = new SetHashMap<>();

		this.explicitRelationsStartingFrom = new SetHashMap<>();
		this.explicitRelationsEndingAt = new SetHashMap<>();

		for (IRelation relation : this.explicitRelationsReadOnly) {
			allRelations.add(relation);

			this.allRelationMappings.addTo(relation.getUri(), relation);

			this.explicitRelationsStartingFrom.addTo(relation.getFrom(),
					relation);
			this.explicitRelationsEndingAt.addTo(relation.getTo(), relation);

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

									this.implicitRelationMappings.addTo(
											topRelation.getUri(),
											implicitRelation);

									this.allRelationMappings.addTo(
											topRelation.getUri(),
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
		this.allRelationsStartingFrom = new SetHashMap<>();
		this.allRelationsEndingAt = new SetHashMap<>();
		for (IRelation relation : allRelations) {
			this.relationMappings.put(relation.getUri(), relation);

			this.allRelationsStartingFrom.addTo(relation.getFrom(), relation);
			this.allRelationsEndingAt.addTo(relation.getTo(), relation);
		}

			}
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
		return this.implicitRelationMappings.get(uri);
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
		return this.allRelationMappings.get(uri);
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
		return this.getAllRelationsByRelationUri(relation.getUri());
	}

	public Set<IRelation> getExplicitRelationsStartingFrom(URI from) {
		this.checkAndRefresh();
		return this.explicitRelationsStartingFrom.get(from);
	}

	public Set<IRelation> getExplicitRelationsEndingAt(URI to) {
		this.checkAndRefresh();
		return this.explicitRelationsEndingAt.get(to);
	}

	public Set<IRelation> getAllRelationsStartingFrom(URI uri) {
		this.checkAndRefresh();
		return this.allRelationsStartingFrom.get(uri);
	}

	public Set<IRelation> getAllRelationsEndingAt(URI uri) {
		this.checkAndRefresh();
		return this.allRelationsEndingAt.get(uri);
	}

}