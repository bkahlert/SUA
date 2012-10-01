package de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;

public interface IDataLoadProvider {

	public String getJobName(
			List<? extends IBaseDataContainer> dataResourceContainers);

	public IDataContainer load(
			List<? extends IBaseDataContainer> dataResourceContainers,
			IProgressMonitor progressMonitor);

}
