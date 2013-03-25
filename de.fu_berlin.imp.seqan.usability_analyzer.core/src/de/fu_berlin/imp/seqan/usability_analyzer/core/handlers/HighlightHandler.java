package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class HighlightHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(HighlightHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object input = InformationControlManagerUtils.getCurrentInput();
		if (input instanceof HasDateRange) {
			IHighlightService highlightService = (IHighlightService) PlatformUI
					.getWorkbench().getService(IHighlightService.class);
			if (highlightService != null) {
				final TimeZoneDateRange range = ((HasDateRange) input)
						.getDateRange();
				if (range != null) {
					highlightService.highlight(HighlightHandler.this, range,
							true);
				}
			}
		}

		return null;
	}
}