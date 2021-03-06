package de.fu_berlin.imp.apiua.timeline.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;

import de.fu_berlin.imp.apiua.core.model.HasIdentifier;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.timeline.ui.views.TimelineView;
import de.fu_berlin.imp.apiua.timeline.ui.widgets.InformationPresentingTimeline;

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
		List<TimelineView> timelineViews = WorkbenchUtils
				.getViews(TimelineView.class);
		return timelineViews.size() > 0 ? timelineViews.get(0) : null;
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
