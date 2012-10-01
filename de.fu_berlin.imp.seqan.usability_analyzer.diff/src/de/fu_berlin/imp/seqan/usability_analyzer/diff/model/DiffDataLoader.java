package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

public class DiffDataLoader implements IDataLoadProvider {

	@Override
	public String getJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Loading diffs from "
				+ StringUtils.join(dataResourceContainers, ", ") + "...";
	}

	@Override
	public IDataContainer load(
			List<? extends IBaseDataContainer> dataResourceContainers,
			IProgressMonitor progressMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
		DiffDataDirectory diffDataDirectory = new DiffDataDirectory(
				dataResourceContainers);
		diffDataDirectory.scan(subMonitor);
		subMonitor.done();
		Activator.getDefault().setDiffDataDirectory(diffDataDirectory);
		return diffDataDirectory;
	}

}
