package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;

public interface IDataServiceListener {
	/**
	 * This method is called when new {@link IBaseDataContainer}s have been
	 * added from the available ones.
	 * 
	 * @param dataContainers
	 */
	public void dataDirectoriesAdded(
			List<? extends IBaseDataContainer> dataContainers);

	/**
	 * This method is called when new {@link IBaseDataContainer}s have been
	 * removed from the available ones.
	 * 
	 * @param dataContainers
	 */
	public void dataDirectoriesRemoved(
			List<? extends IBaseDataContainer> dataContainers);

	/**
	 * This method is called when a new set of {@link IBaseDataContainer}s has
	 * been loaded.
	 * 
	 * @param dataContainers
	 */
	public void dataDirectoriesLoaded(
			List<? extends IBaseDataContainer> dataContainers);

	/**
	 * This method is called when the currently loaded set of
	 * {@link IBaseDataContainer}s has been unloaded.
	 * 
	 * @param dataContainers
	 */
	public void dataDirectoriesUnloaded(
			List<? extends IBaseDataContainer> dataContainers);

}
