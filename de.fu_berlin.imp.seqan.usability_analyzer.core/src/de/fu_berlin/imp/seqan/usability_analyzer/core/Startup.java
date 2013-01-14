package de.fu_berlin.imp.seqan.usability_analyzer.core;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);
		dataService.setActiveDataDirectories(dataService
				.getActiveDataDirectories());
	}

}
