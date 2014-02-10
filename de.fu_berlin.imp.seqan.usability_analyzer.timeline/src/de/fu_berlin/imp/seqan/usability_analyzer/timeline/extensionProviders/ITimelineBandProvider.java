package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.viewer.timeline.impl.AbstractTimelineGroupViewer;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

/**
 * Instances of this class provide the data needed to display custom data in the
 * timeline.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineBandProvider<TIMELINEGROUPVIEWER extends AbstractTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT> {
	public ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> getContentProvider();

	public ITimelineBandLabelProvider getBandLabelProvider();

	public ITimelineEventLabelProvider getEventLabelProvider();
}
