package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

public class MultiSourceTimelineViewer
		extends
		com.bkahlert.devel.nebula.viewer.timeline.impl.MultiSourceTimelineViewer {

	public MultiSourceTimelineViewer(ITimeline timeline) {
		super(timeline);
	}

	public void setProviders(List<ITimelineBandProvider> bandProviders) {
		List<ProviderGroup> providers = new ArrayList<ProviderGroup>();
		for (ITimelineBandProvider bandProvider : bandProviders) {
			providers.add(new ProviderGroup(bandProvider.getContentProvider(),
					bandProvider.getBandLabelProvider(), bandProvider
							.getEventLabelProvider()));
		}
		super.setProviders(providers.toArray(new ProviderGroup[0]));
	}

}
