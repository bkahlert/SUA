package de.fu_berlin.imp.seqan.usability_analyzer.timeline.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView;

public class NavigateBackHandler extends AbstractNavigateHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(NavigateBackHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		TimelineView view = this.getTimelineView();
		// Object predecessor = this.informationPresentingTimeline
		// .getPredecessor(this.element);
		// if (predecessor instanceof ILocatable) {
		// this.navigateTo((ILocatable) predecessor);
		// }
		return null;
	}
}
