package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.viewer.timeline.impl.MinimalTimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.impl.TimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class HighlightableTimelineGroupViewer<TIMELINEGROUP extends TimelineGroup<TIMELINE>, TIMELINE extends ITimeline, INPUT>
		extends TimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT> {

	public HighlightableTimelineGroupViewer(
			TIMELINEGROUP timelineGroup,
			ITimelineProviderFactory<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProviderFactory) {
		super(timelineGroup, timelineProviderFactory);
	}

	/**
	 * Centers the given dates.
	 * 
	 * @param dates
	 * @param monitor
	 */
	public void center(Map<Object, TimeZoneDate> dates, IProgressMonitor monitor) {
		Map<Object, Calendar> calendars = new HashMap<Object, Calendar>();
		for (Object key : dates.keySet()) {
			TimeZoneDate date = dates.get(key);
			if (date != null) {
				calendars.put(key, date.getCalendar());
			}
		}
		this.setCenterVisibleDate(calendars, monitor);
	}

	/**
	 * Highlights the given date ranges.
	 * 
	 * @param groupedRanges
	 * @param progressMonitor
	 */
	public void highlight(Map<IIdentifier, TimeZoneDateRange[]> groupedRanges,
			IProgressMonitor monitor) {

		Map<Object, IDecorator[]> groupedDecorators = new HashMap<Object, IDecorator[]>();

		for (final Object key : groupedRanges.keySet()) {

			final TimeZoneDateRange[] dateRanges = groupedRanges.get(key);

			List<IDecorator> decorators = new ArrayList<IDecorator>(
					dateRanges.length);
			for (TimeZoneDateRange dateRange : dateRanges) {
				if (dateRange.getStartDate() == null
						&& dateRange.getEndDate() == null) {
					continue;
				}
				decorators.add(new Decorator(
						dateRange.getStartDate() != null ? dateRange
								.getStartDate().getCalendar() : null, dateRange
								.getEndDate() != null ? dateRange.getEndDate()
								.getCalendar() : null));
			}
			groupedDecorators.put(key, decorators.toArray(new IDecorator[0]));
		}
		this.setDecorators(groupedDecorators, monitor);
	}

}
