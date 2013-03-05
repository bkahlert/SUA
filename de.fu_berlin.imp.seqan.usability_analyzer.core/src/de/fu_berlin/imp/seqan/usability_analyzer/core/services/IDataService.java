package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;

/**
 * Instances of this class can be part of an {@link IWorkSession}.
 * 
 * @author bkahlert
 * 
 */
public interface IDataService {

	public void addDataServiceListener(
			IDataServiceListener dataServiceListener);

	public void removeDataServiceListener(
			IDataServiceListener dataServiceListener);

	/**
	 * Returns the currently active {@link IBaseDataContainer}s.
	 * 
	 * @return
	 */
	public List<? extends IBaseDataContainer> getActiveDataDirectories();

	/**
	 * Closes the currently active {@link IBaseDataContainer}s and opens the
	 * given ones.
	 * 
	 * @param list
	 */
	public void loadDataDirectories(List<? extends IBaseDataContainer> list);

	/**
	 * Returns the currently registered {@link IBaseDataContainer}s.
	 * 
	 * @return
	 */
	public List<? extends IBaseDataContainer> getDataDirectories();

	/**
	 * Adds a new {@link IBaseDataContainer} to the registered ones.
	 * 
	 * @param dataContainers
	 */
	public void addDataDirectories(
			List<? extends IBaseDataContainer> dataContainers);

	/**
	 * Removes a {@link IBaseDataContainer} from the registered ones.
	 * 
	 * @param dataContainers
	 */
	public void removeDataDirectories(
			List<? extends IBaseDataContainer> dataContainers);

	/**
	 * This method is called when the whole workbench is shutdown.
	 * <p>
	 * This method should be used to dispose resources, close handles and clean
	 * everything up.
	 */
	public void unloadData();

}
