package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;

public interface IDataDirectoriesServiceListener {
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
	 * This method is called when the currently active
	 * {@link IBaseDataContainer} has changed.
	 * 
	 * @param dataContainers
	 * @return the process directory
	 */
	public void activeDataDirectoriesChanged(
			List<? extends IBaseDataContainer> dataContainers);

}
