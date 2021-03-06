package de.fu_berlin.imp.apiua.core.model.data.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

/**
 * Instances of this class wrap a list of {@link IDataContainer}s and displays
 * them as one.
 * 
 * @author bkahlert
 * 
 */
public class AggregatedDataContainer implements IDataContainer {

	private List<? extends IDataContainer> containers;

	public AggregatedDataContainer(List<? extends IDataContainer> containers) {
		this.containers = containers;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (IDataContainer container : this.containers) {
			builder.append(container.toString());
			builder.append(", ");
		}
		if (builder.length() >= 2) {
			builder.setLength(builder.length() - 2);
		}
		return builder.toString();
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		List<IBaseDataContainer> baseDataContainers = new ArrayList<IBaseDataContainer>();
		for (IDataContainer dataContainer : this.containers) {
			IBaseDataContainer baseDataContainer = dataContainer
					.getBaseDataContainer();
			if (baseDataContainer != null) {
				baseDataContainers.add(baseDataContainer);
			}
		}
		return new AggregatedBaseDataContainer(baseDataContainers);
	}

	@Override
	public IDataContainer getParentDataContainer() {
		List<IDataContainer> parentDataContainers = new ArrayList<IDataContainer>();
		for (IDataContainer dataContainer : this.containers) {
			IDataContainer parentDataContainer = dataContainer
					.getParentDataContainer();
			if (parentDataContainer != null) {
				parentDataContainers.add(parentDataContainer);
			}
		}
		return new AggregatedDataContainer(parentDataContainers);
	}

	@Override
	public String getName() {
		return this.toString();
	}

	@Override
	public IData getResource(String name) {
		for (IDataContainer container : this.containers) {
			IData resource = container.getResource(name);
			if (resource != null) {
				return resource;
			}
		}
		return null;
	}

	@Override
	public List<IData> getResources() {
		List<IData> resources = new ArrayList<IData>();
		for (IDataContainer container : this.containers) {
			resources.addAll(container.getResources());
		}
		return resources;
	}

	@Override
	public IDataContainer getSubContainer(String name) {
		List<IDataContainer> containers = new ArrayList<IDataContainer>();
		for (IDataContainer container : this.containers) {
			containers.add(container.getSubContainer(name));
		}
		return new AggregatedDataContainer(containers);
	}

	@Override
	public List<IDataContainer> getSubContainers() {
		List<IDataContainer> containers = new ArrayList<IDataContainer>();
		for (IDataContainer container : this.containers) {
			containers.addAll(container.getSubContainers());
		}
		return containers;
	}

	@Override
	public Iterator<IDataContainer> listSubContainersDeep() {
		final List<IDataContainer> subContainers = new ArrayList<IDataContainer>();
		for (IDataContainer container : this.containers) {
			for (Iterator<IDataContainer> it = container
					.listSubContainersDeep(); it.hasNext();) {
				subContainers.add(it.next());
			}
		}
		return subContainers.iterator();
	}

	@Override
	public Iterator<IData> listDatasDeep() {
		final List<IData> datas = new ArrayList<IData>();
		for (IDataContainer container : this.containers) {
			for (Iterator<IData> it = container.listDatasDeep(); it.hasNext();) {
				datas.add(it.next());
			}
		}
		return datas.iterator();
	}

}
