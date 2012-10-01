package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

public class DoclogDataLoader implements IDataLoadProvider {

	@Override
	public String getJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Loading doclogs from "
				+ StringUtils.join(dataResourceContainers, ", ") + "...";
	}

	@Override
	public IDataContainer load(
			List<? extends IBaseDataContainer> dataResourceContainers,
			IProgressMonitor progressMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
		DoclogDataDirectory doclogDataDirectory = new DoclogDataDirectory(
				dataResourceContainers);
		doclogDataDirectory.scan(subMonitor);
		subMonitor.done();
		Activator.getDefault().setDoclogDataDirectory(doclogDataDirectory);
		return doclogDataDirectory;
	}

}
