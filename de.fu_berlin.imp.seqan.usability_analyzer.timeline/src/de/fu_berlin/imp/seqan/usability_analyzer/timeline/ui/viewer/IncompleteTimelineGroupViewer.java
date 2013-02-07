package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.viewer.timelineGroup.impl.TimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

public class IncompleteTimelineGroupViewer<TIMELINEGROUP extends ITimelineGroup<? extends ITimeline>>
		extends TimelineGroupViewer<TIMELINEGROUP> {

	public IncompleteTimelineGroupViewer(TIMELINEGROUP timelineGroup) {
		super(timelineGroup);
	}

	@Override
	public Object getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInput(Object input) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

}
