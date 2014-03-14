package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;

public class LocatorService {

	public static ILocatorService INSTANCE;

	static {
		INSTANCE = (ILocatorService) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getService(ILocatorService.class);
	}

}
