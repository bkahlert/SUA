package de.fu_berlin.imp.apiua.groundedtheory;

import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;

public class LocatorService {

	public static ILocatorService INSTANCE;

	static {
		INSTANCE = (ILocatorService) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getService(ILocatorService.class);
	}

}
