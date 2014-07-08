package de.fu_berlin.imp.apiua.entity.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;

import de.fu_berlin.imp.apiua.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;
import de.fu_berlin.imp.apiua.diff.model.DiffContainer;
import de.fu_berlin.imp.apiua.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.apiua.entity.Activator;
import de.fu_berlin.imp.apiua.survey.model.SurveyContainer;

public class EntityDataLoader implements IDataLoadProvider {

	private static Logger LOGGER = Logger.getLogger(EntityDataLoader.class);

	@Override
	public String getLoaderJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Extracting entities from diffs and doclogs...";
	}

	@Override
	public String getUnloaderJobName(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		return "Unloading entities...";
	}

	@Override
	public IDataContainer load(
			List<? extends IBaseDataContainer> baseDataContainers,
			IProgressMonitor progressMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor);

		DiffContainer diffContainer = de.fu_berlin.imp.apiua.diff.Activator
				.getDefault().getDiffDataContainer();
		if (diffContainer == null) {
			LOGGER.error("Could not match entities since no "
					+ DiffContainer.class.getSimpleName() + " could be found.");
			return null;
		}

		DoclogDataContainer doclogDataContainer = de.fu_berlin.imp.apiua.doclog.Activator
				.getDefault().getDoclogContainer();
		if (doclogDataContainer == null) {
			LOGGER.error("Could not match entities since no "
					+ DoclogDataContainer.class.getSimpleName()
					+ " could be found.");
			return null;
		}

		SurveyContainer surveyContainer = de.fu_berlin.imp.apiua.survey.Activator
				.getDefault().getSurveyContainer();
		if (surveyContainer == null) {
			LOGGER.error("No survey found.");
		}

		try {
			EntityDataContainer entityDataContainer = new EntityDataContainer(
					baseDataContainers, diffContainer, doclogDataContainer,
					surveyContainer);
			entityDataContainer.scan(subMonitor);
			Activator.getDefault().setLoadedData(entityDataContainer);
			return entityDataContainer;
		} catch (EntityDataException e) {
			ErrorDialog
					.openError(
							null,
							"Data Source Directory",
							"The data directory " + baseDataContainers
									+ " can't be opened.",
							new Status(
									IStatus.ERROR,
									Activator.PLUGIN_ID,
									"The provided directory could not be read. Please check the configuration.",
									e));

		}
		return null;
	}

	@Override
	public void unload(IProgressMonitor progressMonitor) {
	}
}
