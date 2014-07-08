package de.fu_berlin.imp.apiua.core.model.data;

import java.util.Iterator;
import java.util.List;

/**
 * This is an abstract container that contains {@link IData}.
 * <p>
 * Directories / folders can be considered data containers which container other
 * containers and atomic data objects (e.g. files).
 * 
 * @author bkahlert
 * 
 */
public interface IDataContainer {

	/**
	 * Root of this {@link IDataContainer}.
	 * <p>
	 * On UNIX file systems this is normally /.<br/>
	 * On Windows this can be a drive like c:.
	 * 
	 * @return null if no base exists or if this {@link IDataContainer} already
	 *         is a {@link IBaseDataContainer}.
	 */
	public IBaseDataContainer getBaseDataContainer();

	/**
	 * Returns this {@link IDataContainer}'s parent {@link IDataContainer}.
	 * <p>
	 * On file systems this corresponds to the parent directory name.
	 * 
	 * @return
	 */
	public IDataContainer getParentDataContainer();

	/**
	 * Returns the name of this {@link IDataContainer}.
	 * <p>
	 * On file systems this corresponds to the file name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Returns the {@link IData} which matches the given name.
	 * <p>
	 * On file systems this corresponds to a returned file with a given name.
	 * 
	 * @param name
	 * @return
	 */
	public IData getResource(String name);

	/**
	 * Returns a list of all found {@link IData}.
	 * <p>
	 * On file systems this corresponds to a list of files.
	 * 
	 * @return
	 */
	public List<IData> getResources();

	/**
	 * Returns the child {@link IDataContainer} with the given name.
	 * <p>
	 * On file systems this corresponds to a directory with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public IDataContainer getSubContainer(String name);

	/**
	 * Returns the a list of all found {@link IDataContainer}s.
	 * <p>
	 * On file systems this corresponds to a list of sub directories.
	 * 
	 * @return
	 */
	public List<IDataContainer> getSubContainers();

	/**
	 * Iterates through this and all nested {@link IDataContainers}s.
	 * 
	 * @return
	 */
	public Iterator<IDataContainer> listSubContainersDeep();

	/**
	 * Iterates through all {@link IData}s by looking in all nested sub
	 * containers.
	 * 
	 * @return
	 */
	public Iterator<IData> listDatasDeep();

}
