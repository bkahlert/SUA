package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bkahlert.nebula.data.TreeNode;
import com.bkahlert.nebula.lang.ListHashMap;
import com.bkahlert.nebula.utils.DataView;
import com.bkahlert.nebula.utils.IDirtiable;
import com.bkahlert.nebula.utils.Pair;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

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
public class CodeHierarchyView extends DataView {

	private List<TreeNode<ICode>> codeTrees;
	private List<TreeNode<ICode>> codeTreesReadOnly;

	private Set<ICode> codesReadOnly;
	private Map<Long, ICode> ids;
	private Map<URI, ICode> uris;
	private Map<ICode, ICode> parents;
	private ListHashMap<ICode, ICode> children;
	private ListHashMap<ICode, ICode> ancestors;
	private ListHashMap<ICode, ICode> descendents;
	private Set<IsACodeInstance> isACodeInstancesReadOnly;

	public CodeHierarchyView(List<TreeNode<ICode>> codeTrees,
			IDirtiable dirtiable, IDirtiable... dirtiables) {
		super(dirtiable, dirtiables);
		this.codeTrees = codeTrees;
	}

	private <T> List<TreeNode<T>> clone(List<TreeNode<T>> codeTrees) {
		List<TreeNode<T>> clone = new ArrayList<>(codeTrees.size());
		for (TreeNode<T> codeTree : codeTrees) {
			clone.add(new TreeNode<>(codeTree));
		}
		return clone;
	}

	@Override
	protected void refresh() {
		this.codeTreesReadOnly = Collections.unmodifiableList(this
				.clone(this.codeTrees));

		Set<ICode> codes = new LinkedHashSet<>();
		this.ids = new HashMap<>();
		this.uris = new HashMap<>();
		this.parents = new HashMap<>();
		this.children = new ListHashMap<>();
		this.ancestors = new ListHashMap<>();
		this.descendents = new ListHashMap<>();
		HashSet<IsACodeInstance> isACodeInstances = new HashSet<>();
		for (TreeNode<ICode> codeTree : this.codeTreesReadOnly) {

			for (ICode code : codeTree) {
				// codes
				codes.add(code);

				// id-code-mapping
				this.ids.put(code.getId(), code);

				this.uris.put(code.getUri(), code);
			}

			// parent relations
			for (Pair<ICode, ICode> parentRelation : codeTree
					.getParentRelations()) {
				ICode parent = parentRelation.getFirst();
				ICode child = parentRelation.getSecond();

				this.parents.put(child, parent);
				this.children.addTo(parent, child);

				if (parent != null) {
					isACodeInstances.add(new IsACodeInstance(parent, child));
				}
			}

			// ancestor/descendent relations
			for (Pair<ICode, ICode> ancestorRelation : codeTree
					.getAncestorRelations()) {
				ICode ancestor = ancestorRelation.getFirst();
				ICode code = ancestorRelation.getSecond();

				this.ancestors.addTo(code, ancestor);
				this.descendents.addTo(ancestor, code);
			}
		}

		this.codesReadOnly = Collections.unmodifiableSet(codes);
		this.isACodeInstancesReadOnly = Collections
				.unmodifiableSet(isACodeInstances);
	}

	public Set<ICode> getCodes() {
		this.checkAndRefresh();
		return this.codesReadOnly;
	}

	public ICode getCode(long id) {
		this.checkAndRefresh();
		return this.ids.get(id);
	}

	public ICode getCode(URI uri) {
		this.checkAndRefresh();
		return this.uris.get(uri);
	}

	public ICode getParent(ICode code) {
		this.checkAndRefresh();
		return this.parents.get(code);
	}

	public List<ICode> getChildren(ICode code) {
		this.checkAndRefresh();
		return Collections.unmodifiableList(this.children.get(code));
	}

	public List<ICode> getAncestors(ICode code) {
		this.checkAndRefresh();
		return Collections.unmodifiableList(this.ancestors.get(code));
	}

	public List<ICode> getDescendents(ICode code) {
		this.checkAndRefresh();
		return Collections.unmodifiableList(this.descendents.get(code));
	}

	public List<URI> getDescendents(URI code) {
		this.checkAndRefresh();
		return Collections.unmodifiableList(this.descendents
				.get(this.getCode(code)).stream().map(c -> c.getUri())
				.collect(Collectors.toList()));
	}

	public Set<IsACodeInstance> getExplicitIsACodeInstances() {
		this.checkAndRefresh();
		return this.isACodeInstancesReadOnly;
	}

}
