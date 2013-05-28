package de.fu_berlin.imp.seqan.usability_analyzer.core;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);
		dataService.loadDataDirectories(dataService.getActiveDataDirectories());

		IWorkSessionService workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		workSessionService.restoreLastWorkSession();
	}

}
