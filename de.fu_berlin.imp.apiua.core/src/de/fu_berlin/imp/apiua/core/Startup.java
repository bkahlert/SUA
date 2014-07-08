package de.fu_berlin.imp.apiua.core;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IWorkSessionService;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		/*
		 * Re-open data directories.
		 */
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);
		dataService.restoreLastDataDirectories();

		/*
		 * Re-open work session.
		 */
		IWorkSessionService workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		workSessionService.restoreLastWorkSession();
	}

}
