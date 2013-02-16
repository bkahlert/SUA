package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;

/**
 * Instances of this class are timelines in which you can double click on an
 * item to get detailed information about it.
 * 
 * @author bkahlert
 * 
 */
public class BrowsableTimeline extends Timeline {

	private final Logger logger = Logger.getLogger(BrowsableTimeline.class);

	public BrowsableTimeline(Composite parent, int style) {
		super(parent, style);
		try {
			this.injectCssFile(getFileUrl(BrowsableTimeline.class, "style.css"));
		} catch (IOException e) {
			logger.error("Could not find style.css", e);
		}

		this.addTimelineListener(new TimelineAdapter() {
			@Override
			public void doubleClicked(TimelineEvent event) {
				TimelineDetailDialog detailDialog = new TimelineDetailDialog(
						null, BrowsableTimeline.this);
				detailDialog.setBlockOnOpen(false);
				detailDialog.open();
				detailDialog.load(event.getSource());
			}
		});
	}

	@Override
	public String toString() {
		return BrowsableTimeline.class.getSimpleName() + "(" + this.getData()
				+ ")";
	}

}
