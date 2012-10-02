package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.DataDirectoryDialog;

public class ChangeDataDirectories extends AbstractHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IDataService dataService = (IDataService) PlatformUI
				.getWorkbench().getService(IDataService.class);

		List<? extends IBaseDataContainer> dataResourceContainers = dataService
				.getDataDirectories();

		List<FileDataContainer> fileDataContainers = ArrayUtils
				.getAdaptableObjects(dataResourceContainers.toArray(),
						FileDataContainer.class);

		List<File> directories = new ArrayList<File>();
		for (FileDataContainer fileDataContainer : fileDataContainers)
			directories.add(fileDataContainer.getFile());

		DataDirectoryDialog dataDirectoryDialog = new DataDirectoryDialog(
				HandlerUtil.getActiveShell(event), directories);
		if (dataDirectoryDialog.open() == Window.OK) {
			List<IDataContainer> newDataDirectories = new ArrayList<IDataContainer>();
			for (File directory : dataDirectoryDialog.getDirectories()) {
				newDataDirectories.add(new FileBaseDataContainer(directory));
			}
			dataService.removeDataDirectories(ListUtils.subtract(
					fileDataContainers, newDataDirectories));
			dataService.addDataDirectories(ListUtils.subtract(
					newDataDirectories, fileDataContainers));
			dataService.setActiveDataDirectories(dataDirectoryDialog
					.getSelectedDataDirectories());

			/**
			 * ExecutorUtil.nonUIAsyncExec(new Runnable() {
			 * 
			 * @Override public void run() { dataDirectoriesService
			 *           .setActiveDataDirectory(dataDirectory); } });
			 */
		}
		return null;
	}
}
