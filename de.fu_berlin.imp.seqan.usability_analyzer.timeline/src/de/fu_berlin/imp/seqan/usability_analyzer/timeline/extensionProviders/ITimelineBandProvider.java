package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;

/**
 * Instances of this class provide the data needed to display custom data in the
 * timeline.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineBandProvider {
	public ITimelineContentProvider getContentProvider();

	public ITimelineBandLabelProvider getBandLabelProvider();

	public ITimelineEventLabelProvider getEventLabelProvider();
}
