package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.util.List;

public interface IDataContainer {

	public IBaseDataContainer getBaseDataContainer();

	public String getName();

	public IData getResource(String name);

	public List<IData> getResources();

	public IDataContainer getSubContainer(String name);

	public List<IDataContainer> getSubContainers();

}
