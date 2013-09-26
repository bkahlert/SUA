package de.fu_berlin.imp.seqan.usability_analyzer.survey.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.Activator;

public class SurveyLoader implements IDataLoadProvider {

	@Override
	public String getLoaderJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Loading surveys from "
				+ StringUtils.join(dataResourceContainers, ", ") + "...";
	}

	@Override
	public String getUnloaderJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Unloading surveys from "
				+ StringUtils.join(dataResourceContainers, ", ") + "...";
	}

	@Override
	public IDataContainer load(
			List<? extends IBaseDataContainer> baseDataContainers,
			IProgressMonitor progressMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
		SurveyContainer surveyContainer = new SurveyContainer(
				baseDataContainers);
		surveyContainer.scan(subMonitor);
		subMonitor.done();
		Activator.getDefault().setSurveyContainer(surveyContainer);
		return surveyContainer;
	}

	@Override
	public void unload(IProgressMonitor progressMonitor) {
	}

}
