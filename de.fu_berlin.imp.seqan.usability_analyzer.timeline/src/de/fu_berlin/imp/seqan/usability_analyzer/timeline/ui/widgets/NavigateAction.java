package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import org.eclipse.jface.action.Action;

import com.bkahlert.devel.nebula.utils.information.InformationControl;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public abstract class NavigateAction extends Action {
	private final InformationPresentingTimeline informationPresentingTimeline;
	private final InformationControl<?> informationControl;

	public NavigateAction(
			InformationPresentingTimeline informationPresentingTimeline,
			InformationControl<?> informationControl) {
		this.informationPresentingTimeline = informationPresentingTimeline;
		this.informationControl = informationControl;
	}

	public void navigateTo(ILocatable locatable) {
		if (locatable instanceof HasDateRange) {
			TimeZoneDateRange range = ((HasDateRange) locatable).getDateRange();
			if (range != null && range.getStartDate() != null) {
				this.informationPresentingTimeline.setCenterVisibleDate(range
						.getStartDate().getCalendar());
				this.informationControl.setInput(locatable);
				this.informationControl.layout();
			}
		}
	}

}