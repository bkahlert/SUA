package de.fu_berlin.imp.apiua.core.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.services.IDataService;

public class ExportHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ExportHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);

		dataService.export();

		return null;
	}
}