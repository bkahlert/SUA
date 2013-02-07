package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class Timeline extends
		com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline {

	private final Logger logger = Logger.getLogger(Timeline.class);

	private List<TimeZoneDateRange> highlightedDateRanges;

	public Timeline(Composite parent, int style) {
		super(parent, style);
		try {
			this.injectCssFile(getFileUrl(Timeline.class, "style.css"));
		} catch (IOException e) {
			logger.error("Could not find style.css", e);
		}

		this.addTimelineListener(new TimelineAdapter() {
			@Override
			public void doubleClicked(TimelineEvent event) {
				TimelineDetailDialog detailDialog = new TimelineDetailDialog(
						null, Timeline.this);
				detailDialog.setBlockOnOpen(false);
				detailDialog.open();
				detailDialog.load(event.getSource());
			}
		});
	}

	/**
	 * Highlights the {@link Doclog}'s parts that fall in the given
	 * {@link TimeZoneDateRange}s.
	 * <p>
	 * Hint: This method may be called from a non-UI thread. The relatively
	 * time-consuming JSON conversion is done asynchronously making this method
	 * return immediately.
	 * 
	 * @param dateRanges
	 */
	public void highlight(final List<TimeZoneDateRange> dateRanges) {
		this.highlightedDateRanges = dateRanges;
		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<IDecorator> decorators = new ArrayList<IDecorator>(
						dateRanges.size());
				for (TimeZoneDateRange dateRange : dateRanges) {
					if (dateRange.getStartDate() != null
							&& dateRange.getEndDate() != null)
						decorators.add((IDecorator) new Decorator(dateRange
								.getStartDate().toISO8601(), dateRange
								.getEndDate().toISO8601()));
				}
				final String decoratorJSON = TimelineJsonGenerator.toJson(
						decorators, false);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						applyDecorators(decoratorJSON);
					}
				});
			}
		}).start();
	}

	public List<TimeZoneDateRange> getHighlightedDateRanges() {
		return this.highlightedDateRanges;
	}

	@Override
	public String toString() {
		return Timeline.class.getSimpleName() + "(" + this.getData() + ")";
	}

}
