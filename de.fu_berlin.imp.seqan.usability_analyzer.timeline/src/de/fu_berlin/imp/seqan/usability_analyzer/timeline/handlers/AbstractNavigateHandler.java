package de.fu_berlin.imp.seqan.usability_analyzer.timeline.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.InformationPresentingTimeline;

public abstract class AbstractNavigateHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractNavigateHandler.class);

	protected IIdentifier getIdentifier() {
		Object input = InformationControlManagerUtils.getCurrentInput();
		if (input instanceof HasIdentifier) {
			return ((HasIdentifier) input).getIdentifier();
		} else {
			return null;
		}
	}

	protected TimelineView getTimelineView() {
		for (IViewReference viewReference : PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences()) {
			IWorkbenchPart part = viewReference.getPart(false);
			if (part instanceof TimelineView) {
				return (TimelineView) part;
			}
		}
		return null;
	}

	protected InformationPresentingTimeline getTimeline() {
		TimelineView view = this.getTimelineView();
		if (view == null) {
			return null;
		}
		IIdentifier identifier = this.getIdentifier();
		if (identifier != null) {
			return view.getTimeline(identifier);
		} else {
			return null;
		}
	}

	protected void navigateTo(ILocatable locatable) {
		if (locatable instanceof HasDateRange) {
			TimeZoneDateRange range = ((HasDateRange) locatable).getDateRange();
			if (range != null) {
				TimeZoneDate timeZoneDate;
				if (range.getStartDate() != null) {
					timeZoneDate = range.getStartDate();
				} else if (range.getEndDate() != null) {
					timeZoneDate = range.getEndDate();
				} else {
					return;
				}

				TimelineView view = this.getTimelineView();
				if (view != null) {
					InformationPresentingTimeline timeline = this.getTimeline();
					if (timeline != null) {
						timeline.setCenterVisibleDate(timeZoneDate
								.getCalendar());
					}
				}
				InformationControlManagerUtils.getCurrentManager(
						ILocatable.class).setInformation(locatable);
			}
		}
	}

}
