package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.groundedtheory.views.AbstractMemoView;

public abstract class AbstractNavigationMemoViewHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractNavigationMemoViewHandler.class);

	protected AbstractMemoView getMemoView() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPartService().getActivePart();
		if (activePart instanceof AbstractMemoView) {
			return (AbstractMemoView) activePart;
		} else {
			return null;
		}
	}

}
