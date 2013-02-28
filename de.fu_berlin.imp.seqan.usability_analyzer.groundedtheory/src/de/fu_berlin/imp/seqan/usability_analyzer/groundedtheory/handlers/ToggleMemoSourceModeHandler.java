package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.MemoView;

public class ToggleMemoSourceModeHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ToggleMemoSourceModeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		boolean sourceMode = !oldValue;
		IWorkbenchPart activePart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPartService().getActivePart();
		if (activePart instanceof MemoView) {
			MemoView memoView = (MemoView) activePart;
			memoView.setSourceMode(sourceMode);
		}
		return null;
	}
}