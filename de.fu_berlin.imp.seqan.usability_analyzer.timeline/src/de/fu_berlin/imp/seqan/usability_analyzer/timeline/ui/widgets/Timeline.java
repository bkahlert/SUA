package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.timeline.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.HotZone;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.SelectionTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;

public class Timeline extends SelectionTimeline {

	private final Logger logger = Logger.getLogger(Timeline.class);

	private List<ITimelineBand> bands;
	private List<ITimelineEvent> sortedEvents;
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
			public void doubleClicked(ITimelineEvent timelineEvent) {
				TimelineDetailDialog detailDialog = new TimelineDetailDialog(
						null, Timeline.this);
				detailDialog.setBlockOnOpen(false);
				detailDialog.open();
				detailDialog.load(timelineEvent);
			}
		});
		this.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					if (((StructuredSelection) event.getSelection()).size() > 0) {
						System.err.println(event);
					}
				}
			}
		});
		new BrowserFunction(this.getBrowser(), "click_dummy");
	}

	/**
	 * Displays a {@link Doclog}'s content.
	 * <p>
	 * Hint: This method may be called from a non-UI thread. The relatively
	 * time-consuming JSON conversion is done asynchronously making this method
	 * return immediately.
	 * 
	 * @param bands
	 * @param title
	 * @param monitor
	 * @return
	 */
	public Future<Job> show(final List<ITimelineBand> bands,
			final String title, final TimeZoneDate centerStart,
			final TimeZoneDateRange highlightedRange) {
		this.bands = bands;
		this.sortedEvents = extractSortedEvents(bands);
		return ExecutorUtil.nonUIAsyncExec(new Callable<Job>() {
			@Override
			public Job call() {
				Job loader = new Job("Showing timeline...") {
					@Override
					protected IStatus run(IProgressMonitor progressMonitor) {
						final SubMonitor monitor = SubMonitor
								.convert(progressMonitor);
						monitor.beginTask(title + "...", 1 + 30 * bands.size());

						IOptions options = getTimelineOptions(title,
								centerStart, highlightedRange);

						monitor.worked(1);

						ITimelineInput input = new TimelineInput(options, bands);
						Timeline.super.show(input,
								monitor.newChild(30 * bands.size()));
						monitor.done();

						return Status.OK_STATUS;
					}
				};
				loader.schedule();
				return loader;
			}
		});
	}

	private static List<ITimelineEvent> extractSortedEvents(
			List<ITimelineBand> bands) {
		List<ITimelineEvent> events = new ArrayList<ITimelineEvent>();
		for (ITimelineBand band : bands)
			events.addAll(band.getEvents());
		Collections.sort(events, new Comparator<ITimelineEvent>() {
			@Override
			public int compare(ITimelineEvent o1, ITimelineEvent o2) {
				if (o1 == null)
					return -1;
				TimeZoneDateRange r1 = new TimeZoneDateRange(
						o1.getStart() != null ? new TimeZoneDate(o1.getStart())
								: null, o1.getEnd() != null ? new TimeZoneDate(
								o1.getEnd()) : null);
				TimeZoneDateRange r2 = new TimeZoneDateRange(
						o2.getStart() != null ? new TimeZoneDate(o2.getStart())
								: null, o2.getEnd() != null ? new TimeZoneDate(
								o2.getEnd()) : null);
				return r1.compareTo(r2);
			}
		});
		return events;
	}

	/**
	 * Centers the {@link Timeline} in a way that the given
	 * {@link TimeZoneDateRange} is in focus.
	 * 
	 * @param minMaxDateRange
	 */
	public void center(TimeZoneDateRange minMaxDateRange) {
		if (minMaxDateRange.getStartDate() != null)
			this.setCenterVisibleDate(minMaxDateRange.getStartDate()
					.getCalendar());
		else if (minMaxDateRange.getEndDate() != null)
			this.setCenterVisibleDate(minMaxDateRange.getEndDate()
					.getCalendar());
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

	public List<ITimelineBand> getBands() {
		return this.bands;
	}

	public List<TimeZoneDateRange> getHighlightedDateRanges() {
		return this.highlightedDateRanges;
	}

	@Override
	public String toString() {
		return Timeline.class.getSimpleName() + "(" + this.getData() + ")";
	}

	public static IOptions getTimelineOptions(final String title,
			TimeZoneDate centerStartDate, TimeZoneDateRange dateRange) {
		IOptions options = new Options();
		if (title != null)
			options.put("title", title);

		if (centerStartDate != null)
			options.put("centerStart",
					centerStartDate.clone().addMilliseconds(-10000l)
							.toISO8601());
		/*
		 * TODO fix timeline javascript to support timeline_start and
		 * timeline_end
		 */
		// if (dateRange.getStartDate() != null)
		// options.put("timeline_start",
		// TimelineJsonGenerator.formatDate(dateRange.getStartDate()));
		// if (dateRange.getEndDate() != null)
		// options.put("timeline_end",
		// TimelineJsonGenerator.formatDate(dateRange.getEndDate()));
		IDataSetInfo dataSetInfo = ((IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class)).getActiveDataDirectories()
				.get(0).getInfo(); // TODO
									// support
									// multiple
									// directories

		options.put("tape_impreciseOpacity", 50);

		options.put("icon_width", 16);

		options.put("show_bubble", "click_dummy");
		options.put("show_bubble_field", "image");

		options.put("hotZones", new IHotZone[] { new HotZone(dataSetInfo
				.getDateRange().getStartDate().toISO8601(), dataSetInfo
				.getDateRange().getEndDate().toISO8601()) });

		if (dateRange == null)
			dateRange = dataSetInfo.getDateRange();

		Calendar decoratorStart = dateRange.getStartDate() != null ? dateRange
				.getStartDate().getCalendar() : null;
		Calendar decoratorEnd = dateRange.getEndDate() != null ? dateRange
				.getEndDate().getCalendar() : null;

		options.put("decorators", new IDecorator[] { new Decorator(
				decoratorStart, dataSetInfo.getName(), decoratorEnd,
				dataSetInfo.getName()) });

		if (dateRange.getStartDate() != null) {
			options.put("timeZone",
					(dateRange.getStartDate().getLocalTime() - dateRange
							.getStartDate().getTime()) / 3600000l); // e.g. 2
																	// or
																	// -5.5
		}

		return options;
	}

	/**
	 * Returns the {@link ITimelineEvent} that is the closest one to the given
	 * event that starts later.
	 * 
	 * @param timelineEvent
	 * @return
	 */
	public ITimelineEvent getSuccessor(ITimelineEvent timelineEvent) {
		int pos = this.sortedEvents.indexOf(timelineEvent);
		if (this.sortedEvents.size() > pos + 1)
			return this.sortedEvents.get(pos + 1);
		else
			return null;
	}

	/**
	 * Returns the {@link ITimelineEvent} that is the closest one to the given
	 * event that starts earlier.
	 * 
	 * @param timelineEvent
	 * @return
	 */
	public ITimelineEvent getPredecessor(ITimelineEvent timelineEvent) {
		int pos = this.sortedEvents.indexOf(timelineEvent);
		if (pos - 1 >= 0)
			return this.sortedEvents.get(pos - 1);
		else
			return null;
	}
}
