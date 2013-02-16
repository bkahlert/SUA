package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timelineGroup.impl.TimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class HighlightableTimelineGroupViewer<TIMELINEGROUP extends ITimelineGroup<TIMELINE>, TIMELINE extends ITimeline>
		extends TimelineGroupViewer<TIMELINEGROUP, TIMELINE> {

	public HighlightableTimelineGroupViewer(TIMELINEGROUP timelineGroup,
			ITimelineProviderFactory<TIMELINE> timelineProviderFactory) {
		super(timelineGroup, timelineProviderFactory);
	}

	/**
	 * Highlights the given date ranges in the timelines and centers them
	 * correctly.
	 * 
	 * @param groupedDateRanges
	 * @param progressMonitor
	 */
	public void highlight(
			Map<Object, List<TimeZoneDateRange>> groupedDateRanges,
			IProgressMonitor monitor) {

		Map<Object, IDecorator[]> groupedDecorators = new HashMap<Object, IDecorator[]>();

		for (final Object key : groupedDateRanges.keySet()) {

			final List<TimeZoneDateRange> dateRanges = groupedDateRanges
					.get(key);

			List<IDecorator> decorators = new ArrayList<IDecorator>(
					dateRanges.size());
			for (TimeZoneDateRange dateRange : dateRanges) {
				if (dateRange.getStartDate() == null
						&& dateRange.getEndDate() == null)
					continue;
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
