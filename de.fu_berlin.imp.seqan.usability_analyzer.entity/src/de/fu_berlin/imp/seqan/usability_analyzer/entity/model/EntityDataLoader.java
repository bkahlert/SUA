package de.fu_berlin.imp.seqan.usability_analyzer.entity.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDataLoadProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;

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

		DiffContainer diffContainer = de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator
				.getDefault().getDiffDataDirectories();
		if (diffContainer == null) {
			LOGGER.error("Could not match entities since no "
					+ DiffContainer.class.getSimpleName() + " could be found.");
			return null;
		}

		DoclogDataDirectory doclogDataDirectory = de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator
				.getDefault().getDoclogContainer();
		if (doclogDataDirectory == null) {
			LOGGER.error("Could not match entities since no "
					+ DoclogDataDirectory.class.getSimpleName()
					+ " could be found.");
			return null;
		}

		// if (!diffDataDirectory.getBaseDataContainer().equals(
		// doclogDataDirectory.getBaseDataContainer())) {
		// LOGGER.error("Could not match entities since the "
		// + DiffContainer.class.getSimpleName() + " and the "
		// + DoclogDataDirectory.class.getSimpleName()
		// + " don't handle the same resource.");
		// return null;
		// }

		try {
			EntityDataContainer entityDataContainer = new EntityDataContainer(
					baseDataContainers, diffContainer, doclogDataDirectory);
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
	public void unload(
			IProgressMonitor progressMonitor) {
	}
}
