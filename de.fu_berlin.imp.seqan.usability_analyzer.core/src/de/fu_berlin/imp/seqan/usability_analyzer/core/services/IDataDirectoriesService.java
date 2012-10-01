package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;

/**
 * Instances of this class can be part of an {@link IWorkSession}.
 * 
 * @author bkahlert
 * 
 */
public interface IDataDirectoriesService {

	public void addDataDirectoryServiceListener(
			IDataDirectoriesServiceListener dataDirectoriesServiceListener);

	public void removeDataDirectoryServiceListener(
			IDataDirectoriesServiceListener dataDirectoriesServiceListener);

	public List<? extends IBaseDataContainer> getActiveDataDirectories();

	public void setActiveDataDirectories(List<? extends IBaseDataContainer> list);

	public List<? extends IBaseDataContainer> getDataDirectories();

	public void addDataDirectories(
			List<? extends IBaseDataContainer> dataContainers);

	public void removeDataDirectories(
			List<? extends IBaseDataContainer> dataContainers);

}
