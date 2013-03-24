package de.fu_berlin.imp.seqan.usability_analyzer.timeline.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.InformationPresentingTimeline;

public class NavigateForwardHandler extends AbstractNavigateHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(NavigateForwardHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InformationPresentingTimeline timeline = this.getTimeline();
		if (timeline == null) {
			return null;
		}
		Object successor = timeline.getSuccessor(InformationControlManagerUtils
				.getCurrentInput());
		if (successor instanceof ILocatable) {
			this.navigateTo((ILocatable) successor);
		}
		return null;
	}

}
