package de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;

public interface IDataLoadProvider {

	public String getLoaderJobName(
			List<? extends IBaseDataContainer> baseDataProviders);

	public String getUnloaderJobName(
			List<? extends IBaseDataContainer> baseDataProviders);

	public IDataContainer load(
			List<? extends IBaseDataContainer> baseDataProviders,
			IProgressMonitor progressMonitor);

	public void unload(IProgressMonitor progressMonitor);

}
