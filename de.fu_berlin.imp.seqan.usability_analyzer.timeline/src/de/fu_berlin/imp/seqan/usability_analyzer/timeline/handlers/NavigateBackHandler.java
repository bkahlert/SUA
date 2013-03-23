package de.fu_berlin.imp.seqan.usability_analyzer.timeline.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.InformationPresentingTimeline;

public class NavigateBackHandler extends AbstractNavigateHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(NavigateBackHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("BACK");
		InformationPresentingTimeline timeline = this.getTimeline();
		Object predecessor = timeline
				.getPredecessor(InformationControlManagerUtils
						.getCurrentInput());
		if (predecessor instanceof ILocatable) {
			this.navigateTo((ILocatable) predecessor);
		}
		return null;
	}
}
