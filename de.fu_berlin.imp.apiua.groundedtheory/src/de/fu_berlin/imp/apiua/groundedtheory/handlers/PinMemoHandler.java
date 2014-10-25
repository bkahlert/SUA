package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.utils.WorkbenchUtils;

import de.fu_berlin.imp.apiua.groundedtheory.views.PinnableMemoView;

public class PinMemoHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(OpenCodeStoreHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		boolean pin = !oldValue;
		List<PinnableMemoView> pinnableMemoViews = WorkbenchUtils
				.getViews(PinnableMemoView.class);
		for (PinnableMemoView pinnableMemoView : pinnableMemoViews) {
			pinnableMemoView.setPin(pin);
		}
		return null;
	}

}
