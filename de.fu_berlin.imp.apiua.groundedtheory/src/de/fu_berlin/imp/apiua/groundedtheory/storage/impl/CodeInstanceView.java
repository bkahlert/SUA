package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bkahlert.nebula.lang.ListHashMap;
import com.bkahlert.nebula.lang.SetHashMap;
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
	private ListHashMap<URI, ICodeInstance> explicitCodeInstancesByCodeInstancePhenomenon;
	private ListHashMap<URI, ICodeInstance> explicitCodeInstancesByCodeInstanceCode;
	private Set<URI> explicitlyCodedPhenomenonsReadOnly;

	private Map<URI, ICodeInstance> allCodeInstanceByCodeInstanceUri;
	private SetHashMap<URI, ICode> allCodesByCodeInstancePhenomenon;
	private ListHashMap<URI, ICodeInstance> allCodeInstancesByCodeInstancePhenomenon;
	private ListHashMap<URI, ICodeInstance> allCodeInstancesByCodeInstanceCode;

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
		this.explicitCodeInstancesByCodeInstancePhenomenon = new ListHashMap<>();
		this.explicitCodeInstancesByCodeInstanceCode = new ListHashMap<>();
		Set<URI> explicitlyCodedPhenomenons = new HashSet<>();
		for (ICodeInstance codeInstance : this.explicitCodeInstancesReadOnly) {
			this.explicitCodeInstanceByCodeInstanceId.put(
					codeInstance.getCodeInstanceID(), codeInstance);

			this.explicitCodeInstancesByCodeInstancePhenomenon.addTo(
					codeInstance.getId(), codeInstance);
			this.explicitCodeInstancesByCodeInstanceCode.addTo(codeInstance
					.getCode().getUri(), codeInstance);

			if (!explicitlyCodedPhenomenons.contains(codeInstance.getId())) {
				explicitlyCodedPhenomenons.add(codeInstance.getId());
			}
		}
		this.explicitlyCodedPhenomenonsReadOnly = Collections
				.unmodifiableSet(explicitlyCodedPhenomenons);

		this.allCodeInstanceByCodeInstanceUri = new HashMap<>();
		this.allCodesByCodeInstancePhenomenon = new SetHashMap<>();
		this.allCodeInstancesByCodeInstancePhenomenon = new ListHashMap<>();
		this.allCodeInstancesByCodeInstanceCode = new ListHashMap<>();
		for (ICodeInstance codeInstance : this.allCodeInstancesReadOnly) {
			this.allCodeInstanceByCodeInstanceUri.put(codeInstance.getUri(),
					codeInstance);
			this.allCodesByCodeInstancePhenomenon.addTo(codeInstance.getId(),
					codeInstance.getCode());
			this.allCodeInstancesByCodeInstancePhenomenon.addTo(
					codeInstance.getId(), codeInstance);
			this.allCodeInstancesByCodeInstanceCode.addTo(codeInstance
					.getCode().getUri(), codeInstance);
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
		this.checkAndRefresh();
		return this.explicitlyCodedPhenomenonsReadOnly;
	}

	public ICodeInstance getById(long id) {
		this.checkAndRefresh();
		return this.explicitCodeInstanceByCodeInstanceId.get(id);
	}

	public ICodeInstance getByUri(URI uri) {
		this.checkAndRefresh();
		return this.allCodeInstanceByCodeInstanceUri.get(uri);
	}

	public Set<ICode> getCodesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableSet(this.allCodesByCodeInstancePhenomenon.get(uri));
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
		return Collections
				.unmodifiableList(this.explicitCodeInstancesByCodeInstancePhenomenon
						.get(uri));
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
			explicitCodeInstances
					.addAll(this.explicitCodeInstancesByCodeInstancePhenomenon
							.get(uri));
		}
		return Collections.unmodifiableSet(explicitCodeInstances);
	}

	/**
	 * Returns all {@link ICodeInstance}s using one of the given phenomenon.
	 *
	 * @param uri
	 * @return
	 */
	public List<ICodeInstance> getAllCodeInstancesByPhenomenon(URI uri) {
		this.checkAndRefresh();
		return Collections
				.unmodifiableList(this.allCodeInstancesByCodeInstancePhenomenon
						.get(uri));
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
			allCodeInstances
					.addAll(this.allCodeInstancesByCodeInstancePhenomenon
							.get(uri));
		}
		return Collections.unmodifiableSet(allCodeInstances);
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
			explicitCodeInstances
					.addAll(this.explicitCodeInstancesByCodeInstanceCode
							.get(uri));
		}
		return Collections.unmodifiableSet(explicitCodeInstances);
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
			explicitCodeInstances
					.addAll(this.explicitCodeInstancesByCodeInstanceCode
							.get(code.getUri()));
		}
		return Collections.unmodifiableSet(explicitCodeInstances);
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
			allCodeInstances.addAll(this.allCodeInstancesByCodeInstanceCode
					.get(uri));
		}
		return Collections.unmodifiableSet(allCodeInstances);
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
			allCodeInstances.addAll(this.allCodeInstancesByCodeInstanceCode
					.get(code.getUri()));
		}
		return Collections.unmodifiableSet(allCodeInstances);
	}

}
