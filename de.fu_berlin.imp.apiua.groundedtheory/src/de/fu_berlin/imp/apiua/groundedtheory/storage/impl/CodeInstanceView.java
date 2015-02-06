package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bkahlert.nebula.utils.DataView;
import com.bkahlert.nebula.utils.IDirtiable;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

public class CodeInstanceView extends DataView {

	private CodeHierarchyView codeHierarchyView;
	private Set<ICodeInstance> explicitCodeInstances;
	private Set<ICodeInstance> explicitCodeInstancesReadOnly;

	private Set<ImplicitCodeInstance> implicitCodeInstancesReadOnly;
	private Set<ICodeInstance> allCodeInstancesReadOnly;

	private Map<Long, ICodeInstance> explicitCodeInstanceByCodeInstanceId;
	private Map<URI, ICodeInstance> explicitCodeInstanceByCodeInstanceUri;
	private Map<URI, List<ICodeInstance>> explicitCodeInstancesByCodeInstancePhenomenon;
	private Map<URI, List<ICodeInstance>> explicitCodeInstancesByCodeInstanceCode;
	private Map<URI, List<ICode>> explicitCodesByCodeInstancePhenomenon;
	private Set<URI> explicitlyCodedPhenomenonsReadOnly;

	private Map<URI, List<ICodeInstance>> allCodeInstancesByCodeInstancePhenomenon;
	private Map<URI, List<ICodeInstance>> allCodeInstancesByCodeInstanceCode;

	public CodeInstanceView(CodeHierarchyView codeHierarchyView,
			Set<ICodeInstance> explicitCodeInstances, IDirtiable... dirtiables) {
		super(codeHierarchyView, dirtiables);
		this.codeHierarchyView = codeHierarchyView;
		this.explicitCodeInstances = explicitCodeInstances;
	}

	@Override
	protected void refresh() {
		this.explicitCodeInstancesReadOnly = Collections
				.unmodifiableSet(new HashSet<>(this.explicitCodeInstances));

		this.refreshImplicit();
		this.refreshMapping();
	}

	private void refreshImplicit() {
		HashSet<ImplicitCodeInstance> implicitCodeInstances = new HashSet<>();
		HashSet<ICodeInstance> allCodeInstances = new HashSet<>();

		allCodeInstances.addAll(this.codeHierarchyView
				.getExplicitIsACodeInstances());
		for (IsACodeInstance isACodeInstance : this.codeHierarchyView
				.getExplicitIsACodeInstances()) {
			this.processCodeInstance(isACodeInstance, implicitCodeInstances,
					allCodeInstances);
		}

		allCodeInstances.addAll(this.explicitCodeInstancesReadOnly);
		for (ICodeInstance codeInstance : this.explicitCodeInstancesReadOnly) {
			this.processCodeInstance(codeInstance, implicitCodeInstances,
					allCodeInstances);
		}

		this.implicitCodeInstancesReadOnly = Collections
				.unmodifiableSet(implicitCodeInstances);
		this.allCodeInstancesReadOnly = Collections
				.unmodifiableSet(allCodeInstances);
	}

	private void processCodeInstance(ICodeInstance codeInstance,
			HashSet<ImplicitCodeInstance> implicitCodeInstances,
			HashSet<ICodeInstance> allCodeInstances) {
		ICode code = codeInstance.getCode();
		while (true) {
			code = this.codeHierarchyView.getParent(code);
			if (code == null) {
				break;
			}
			ImplicitCodeInstance implicitCodeInstance = new ImplicitCodeInstance(
					codeInstance, code);
			implicitCodeInstances.add(implicitCodeInstance);
			allCodeInstances.add(implicitCodeInstance);
		}
	}

	private void refreshMapping() {
		this.explicitCodeInstanceByCodeInstanceId = new HashMap<>();
		this.explicitCodeInstanceByCodeInstanceUri = new HashMap<>();
		this.explicitCodeInstancesByCodeInstancePhenomenon = new HashMap<>();
		this.explicitCodeInstancesByCodeInstanceCode = new HashMap<>();
		this.explicitCodesByCodeInstancePhenomenon = new HashMap<>();
		Set<URI> explicitlyCodedPhenomenons = new HashSet<>();
		for (ICodeInstance codeInstance : this.explicitCodeInstancesReadOnly) {
			this.explicitCodeInstanceByCodeInstanceId.put(
					codeInstance.getCodeInstanceID(), codeInstance);
			this.explicitCodeInstanceByCodeInstanceUri.put(
					codeInstance.getUri(), codeInstance);

			if (!this.explicitCodeInstancesByCodeInstancePhenomenon
					.containsKey(codeInstance.getId())) {
				this.explicitCodeInstancesByCodeInstancePhenomenon.put(
						codeInstance.getId(), new LinkedList<>());
			}
			this.explicitCodeInstancesByCodeInstancePhenomenon.get(
					codeInstance.getId()).add(codeInstance);

			if (!this.explicitCodeInstancesByCodeInstanceCode
					.containsKey(codeInstance.getCode().getUri())) {
				this.explicitCodeInstancesByCodeInstanceCode.put(codeInstance
						.getCode().getUri(), new LinkedList<>());
			}
			this.explicitCodeInstancesByCodeInstanceCode.get(
					codeInstance.getCode().getUri()).add(codeInstance);

			if (!this.explicitCodesByCodeInstancePhenomenon
					.containsKey(codeInstance.getId())) {
				this.explicitCodesByCodeInstancePhenomenon.put(
						codeInstance.getId(), new LinkedList<>());
			}
			this.explicitCodesByCodeInstancePhenomenon
					.get(codeInstance.getId()).add(codeInstance.getCode());

			if (!explicitlyCodedPhenomenons.contains(codeInstance.getId())) {
				explicitlyCodedPhenomenons.add(codeInstance.getId());
			}
		}
		this.explicitlyCodedPhenomenonsReadOnly = Collections
				.unmodifiableSet(explicitlyCodedPhenomenons);

		this.allCodeInstancesByCodeInstancePhenomenon = new HashMap<>();
		this.allCodeInstancesByCodeInstanceCode = new HashMap<>();
		for (ICodeInstance codeInstance : this.allCodeInstancesReadOnly) {
			if (!this.allCodeInstancesByCodeInstancePhenomenon
					.containsKey(codeInstance.getId())) {
				this.allCodeInstancesByCodeInstancePhenomenon.put(
						codeInstance.getId(), new LinkedList<>());
			}
			this.allCodeInstancesByCodeInstancePhenomenon.get(
					codeInstance.getId()).add(codeInstance);

			if (!this.allCodeInstancesByCodeInstanceCode
					.containsKey(codeInstance.getCode().getUri())) {
				this.allCodeInstancesByCodeInstanceCode.put(codeInstance
						.getCode().getUri(), new LinkedList<>());
			}
			this.allCodeInstancesByCodeInstanceCode.get(
					codeInstance.getCode().getUri()).add(codeInstance);
		}
	}

	public Set<ICodeInstance> getExplicitCodeInstances() {
		this.checkAndRefresh();
		return this.explicitCodeInstancesReadOnly;
	}

	public Set<ImplicitCodeInstance> getImplicitCodeInstances() {
		this.checkAndRefresh();
		return this.implicitCodeInstancesReadOnly;
	}

	public Set<ICodeInstance> getAllCodeInstances() {
		this.checkAndRefresh();
		return this.allCodeInstancesReadOnly;
	}

	public Set<URI> getCodedPhenomenons() {
		return this.explicitlyCodedPhenomenonsReadOnly;
	}

	public ICodeInstance getById(long id) {
		this.checkAndRefresh();
		return this.explicitCodeInstanceByCodeInstanceId.get(id);
	}

	public ICodeInstance getByUri(URI uri) {
		this.checkAndRefresh();
		return this.explicitCodeInstanceByCodeInstanceUri.get(uri);
	}

	public List<ICode> getCodesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		List<ICode> codes = this.explicitCodesByCodeInstancePhenomenon.get(uri);
		return codes != null ? Collections.unmodifiableList(codes)
				: Collections.emptyList();
	}

	/**
	 * Returns all explicit {@link ICodeInstance}s using one of the given
	 * phenomenon.
	 *
	 * @param uri
	 * @return
	 */
	public List<ICodeInstance> getExplicitCodeInstancesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		List<ICodeInstance> codeInstances = this.explicitCodeInstancesByCodeInstancePhenomenon
				.get(uri);
		return codeInstances != null ? Collections
				.unmodifiableList(codeInstances) : Collections.emptyList();
	}

	/**
	 * Returns all explicit {@link ICodeInstance}s using one of the given
	 * phenomena.
	 *
	 * @param uris
	 * @return
	 */
	public Set<ICodeInstance> getExplicitCodeInstancesPhenomena(
			Collection<URI> uris) {
		this.checkAndRefresh();
		Set<ICodeInstance> explicitCodeInstances = new HashSet<>();
		for (URI uri : uris) {
			List<ICodeInstance> codeInstances = this.explicitCodeInstancesByCodeInstancePhenomenon
					.get(uri);
			if (codeInstances != null) {
				explicitCodeInstances.addAll(codeInstances);
			}
		}
		return explicitCodeInstances != null ? Collections
				.unmodifiableSet(explicitCodeInstances) : Collections
				.emptySet();
	}

	/**
	 * Returns all {@link ICodeInstance}s using one of the given phenomenon.
	 *
	 * @param uri
	 * @return
	 */
	public List<ICodeInstance> getAllCodeInstancesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		List<ICodeInstance> codeInstances = this.allCodeInstancesByCodeInstancePhenomenon
				.get(uri);
		return codeInstances != null ? Collections
				.unmodifiableList(codeInstances) : Collections.emptyList();
	}

	/**
	 * Returns all {@link ICodeInstance}s using one of the given phenomena.
	 *
	 * @param uris
	 * @return
	 */
	public Set<ICodeInstance> getAllCodeInstancesPhenomena(Collection<URI> uris) {
		this.checkAndRefresh();
		Set<ICodeInstance> allCodeInstances = new HashSet<>();
		for (URI uri : uris) {
			List<ICodeInstance> codeInstances = this.allCodeInstancesByCodeInstancePhenomenon
					.get(uri);
			if (codeInstances != null) {
				allCodeInstances.addAll(codeInstances);
			}
		}
		return allCodeInstances != null ? Collections
				.unmodifiableSet(allCodeInstances) : Collections.emptySet();
	}

	/**
	 * Returns all explicit {@link ICodeInstance}s using one of the given
	 * {@link ICode}.
	 *
	 * @param uris
	 * @return
	 */
	public Set<ICodeInstance> getExplicitCodeInstancesByCode(
			Collection<URI> uris) {
		this.checkAndRefresh();
		Set<ICodeInstance> explicitCodeInstances = new HashSet<>();
		for (URI uri : uris) {
			List<ICodeInstance> codeInstances = this.explicitCodeInstancesByCodeInstanceCode
					.get(uri);
			if (codeInstances != null) {
				explicitCodeInstances.addAll(codeInstances);
			}
		}
		return explicitCodeInstances != null ? Collections
				.unmodifiableSet(explicitCodeInstances) : Collections
				.emptySet();
	}

	/**
	 * Returns all explicit {@link ICodeInstance}s using one of the given
	 * {@link ICode}.
	 *
	 * @param codes
	 * @return
	 */
	public Set<ICodeInstance> getExplicitCodeInstancesByCode(List<ICode> codes) {
		this.checkAndRefresh();
		Set<ICodeInstance> explicitCodeInstances = new HashSet<>();
		for (ICode code : codes) {
			List<ICodeInstance> codeInstances = this.explicitCodeInstancesByCodeInstanceCode
					.get(code.getUri());
			if (codeInstances != null) {
				explicitCodeInstances.addAll(codeInstances);
			}
		}
		return explicitCodeInstances != null ? Collections
				.unmodifiableSet(explicitCodeInstances) : Collections
				.emptySet();
	}

	/**
	 * Returns all {@link ICodeInstance}s using one of the given {@link ICode}.
	 *
	 * @param uris
	 * @return
	 */
	public Set<ICodeInstance> getAllCodeInstancesByCode(Collection<URI> uris) {
		this.checkAndRefresh();
		Set<ICodeInstance> allCodeInstances = new HashSet<>();
		for (URI uri : uris) {
			List<ICodeInstance> codeInstances = this.allCodeInstancesByCodeInstanceCode
					.get(uri);
			if (codeInstances != null) {
				allCodeInstances.addAll(codeInstances);
			}
		}
		return allCodeInstances != null ? Collections
				.unmodifiableSet(allCodeInstances) : Collections.emptySet();
	}

	/**
	 * Returns all {@link ICodeInstance}s using one of the given {@link ICode}s.
	 *
	 * @param codes
	 * @return
	 */
	public Set<ICodeInstance> getAllCodeInstancesByCode(List<ICode> codes) {
		this.checkAndRefresh();
		Set<ICodeInstance> allCodeInstances = new HashSet<>();
		for (ICode code : codes) {
			List<ICodeInstance> codeInstances = this.allCodeInstancesByCodeInstanceCode
					.get(code.getUri());
			if (codeInstances != null) {
				allCodeInstances.addAll(codeInstances);
			}
		}
		return allCodeInstances != null ? Collections
				.unmodifiableSet(allCodeInstances) : Collections.emptySet();
	}

}
