package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

public class DiffLoader implements IDataLoadProvider {

	@Override
	public String getLoaderJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Loading diff files from "
				+ StringUtils.join(dataResourceContainers, ", ") + "...";
	}

	@Override
	public String getUnloaderJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Unloading diff files from "
				+ StringUtils.join(dataResourceContainers, ", ") + "...";
	}

	@Override
	public IDataContainer load(
			List<? extends IBaseDataContainer> dataResourceContainers,
			IProgressMonitor progressMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
		DiffContainer diffContainer = new DiffContainer(dataResourceContainers);
		diffContainer.scan(subMonitor);
		subMonitor.done();
		Activator.getDefault().setDiffDataDirectory(diffContainer);
		return diffContainer;
	}

	@Override
	public void unload(IProgressMonitor progressMonitor) {
	}

}
