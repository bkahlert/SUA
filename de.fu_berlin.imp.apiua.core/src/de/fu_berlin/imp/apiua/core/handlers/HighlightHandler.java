package de.fu_berlin.imp.apiua.core.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.services.IHighlightService;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;

public class HighlightHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(HighlightHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IHighlightService highlightService = (IHighlightService) PlatformUI
				.getWorkbench().getService(IHighlightService.class);
		if (InformationControlManagerUtils.getCurrentInput() instanceof HasDateRange) {
			HasDateRange input = (HasDateRange) InformationControlManagerUtils
					.getCurrentInput();
			if (highlightService != null) {
				final TimeZoneDateRange range = input.getDateRange();
				if (range != null) {
					highlightService.highlight(HighlightHandler.this,
							range.getCalendarRange(), true);
				}
			}
		} else {
			highlightService.highlight(HandlerUtil.getActivePart(event),
					SelectionUtils.getSelection(), true);
		}

		return null;
	}
}