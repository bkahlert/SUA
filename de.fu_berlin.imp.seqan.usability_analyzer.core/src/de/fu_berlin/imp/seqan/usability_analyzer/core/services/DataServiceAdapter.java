package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;

public class DataServiceAdapter implements IDataServiceListener {

	@Override
	public void dataDirectoriesAdded(
			List<? extends IBaseDataContainer> dataContainers) {
		return;
	}

	@Override
	public void dataDirectoriesRemoved(
			List<? extends IBaseDataContainer> dataContainers) {
		return;
	}

	@Override
	public void dataDirectoriesLoaded(
			List<? extends IBaseDataContainer> dataContainers) {
		return;
	}

	@Override
	public void dataDirectoriesUnloaded(
			List<? extends IBaseDataContainer> dataContainers) {
		return;
	}

}
