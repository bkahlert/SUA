package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider;

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
