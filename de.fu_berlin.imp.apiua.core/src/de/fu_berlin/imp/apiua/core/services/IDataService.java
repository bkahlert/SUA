package de.fu_berlin.imp.apiua.core.services;

import java.util.List;

import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;

public interface IDataService {

	public void addDataServiceListener(IDataServiceListener dataServiceListener);

	public void removeDataServiceListener(
			IDataServiceListener dataServiceListener);

	/**
	 * Returns the currently active {@link IBaseDataContainer}s.
	 * <p>
	 * Never returns <code>null</code>.
	 *
	 * @return
	 */
	public List<IBaseDataContainer> getActiveDataDirectories();

	/**
	 * Closes the currently active {@link IBaseDataContainer}s and opens the
	 * given ones.
	 *
	 * @param list
	 */
	public void loadDataDirectories(List<IBaseDataContainer> list);

	/**
	 * Returns the currently registered {@link IBaseDataContainer}s.
	 *
	 * @return
	 */
	public List<IBaseDataContainer> getDataDirectories();

	/**
	 * Adds a new {@link IBaseDataContainer} to the registered ones.
	 *
	 * @param dataContainers
	 */
	public void addDataDirectories(List<IBaseDataContainer> dataContainers);

	/**
	 * Removes a {@link IBaseDataContainer} from the registered ones.
	 *
	 * @param dataContainers
	 */
	public void removeDataDirectories(List<IBaseDataContainer> dataContainers);

	/**
	 * This method is called when the whole workbench is shutdown.
	 * <p>
	 * This method should be used to dispose resources, close handles and clean
	 * everything up.
	 */
	public void unloadData();

	/**
	 * Reopens the lastly active {@link IBaseDataContainer}.
	 */
	void restoreLastDataDirectories();

	/**
	 * Ask every plugin to export its data.
	 */
	public void export();

}
