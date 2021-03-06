package de.fu_berlin.imp.apiua.timeline.extensionProviders;

import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;

/**
 * Instances of this class provide the data needed to display custom data in the
 * timeline.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineBandProvider<INPUT> {
	public ITimelineContentProvider<INPUT> getContentProvider();

	public ITimelineBandLabelProvider getBandLabelProvider();

	public ITimelineEventLabelProvider getEventLabelProvider();
}
