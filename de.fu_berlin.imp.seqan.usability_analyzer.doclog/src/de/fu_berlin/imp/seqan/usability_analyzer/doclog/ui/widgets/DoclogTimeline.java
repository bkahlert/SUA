package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.JsonUtils;

public class DoclogTimeline extends Timeline {

	private final Logger logger = Logger.getLogger(DoclogTimeline.class);

	private static String DOCLOG_CLICK_HANDLER_NAME = "sua_doclog_click";
	private static String DOCLOG_CLICK_HANDLER_PARAM_FIELD = "image";
	private static Pattern DOCLOG_INDEX_PATTERN = Pattern
			.compile("^sua://doclog/(\\d*)$");

	private class DoclogClickHandler extends BrowserFunction {
		DoclogClickHandler(Browser browser, String name) {
			super(browser, name);
		}

		public Object function(Object[] arguments) {
			if (arguments.length == 1 && arguments[0] instanceof String) {
				Matcher matcher = DOCLOG_INDEX_PATTERN
						.matcher((String) arguments[0]);
				if (matcher.matches() && matcher.groupCount() == 1) {
					int doclogRecordIndex = Integer.parseInt(matcher.group(1));

					DoclogRecord selectedDoclogRecord = null;
					for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
						if (System.identityHashCode(doclogRecord) == doclogRecordIndex) {
							selectedDoclogRecord = doclogRecord;
						}
					}
					if (selectedDoclogRecord == null) {
						logger.fatal("Could not determine the passed "
								+ DoclogRecord.class.getSimpleName() + " index");
					}

					new DoclogDetailDialog(null, DoclogTimeline.this,
							selectedDoclogRecord).open();
				} else {
					logger.error("Could not determine the passed "
							+ DoclogRecord.class.getSimpleName() + " index");
				}
			} else {
				logger.error(DoclogClickHandler.class.getSimpleName()
						+ " call parameters are invalid");
			}
			return null;
		}
	}

	private Doclog doclog;

	private List<TimeZoneDateRange> highlightedDateRanges;

	public DoclogTimeline(Composite parent, int style) {
		super(parent, style);
		new DoclogClickHandler(browser, DOCLOG_CLICK_HANDLER_NAME);
	}

	/**
	 * Displays a {@link Doclog}'s content.
	 * <p>
	 * Hint: This method may be called from a non-UI thread. The relatively
	 * time-consuming JSON conversion is done asynchronously making this method
	 * return immediately.
	 * 
	 * @param doclog
	 * @param title
	 * @param monitor
	 * @return
	 */
	public Future<Job> show(final Doclog doclog, final String title) {
		this.doclog = doclog;
		return ExecutorUtil.nonUIAsyncExec(new Callable<Job>() {
			@Override
			public Job call() {
				Job loader = new Job("Showing timeline...") {
					@Override
					protected IStatus run(IProgressMonitor progressMonitor) {
						final SubMonitor monitor = SubMonitor
								.convert(progressMonitor);
						monitor.beginTask(title + "...", 2 + 2 * doclog
								.getDoclogRecords().size());
						HashMap<String, Object> options = new HashMap<String, Object>();
						if (title != null)
							options.put("title", title);

						TimeZoneDateRange dateRange = doclog.getDateRange();
						if (dateRange.getStartDate() != null)
							options.put("centerStart", dateRange.getStartDate()
									.clone().addMilliseconds(-10000l)
									.toISO8601());
						/*
						 * TODO fix timeline javascript to support
						 * timeline_start and timeline_end
						 */
						// if (dateRange.getStartDate() != null)
						// options.put("timeline_start",
						// JsonUtils.formatDate(dateRange.getStartDate()));
						// if (dateRange.getEndDate() != null)
						// options.put("timeline_end",
						// JsonUtils.formatDate(dateRange.getEndDate()));
						IDataSetInfo dataSetInfo = ((IDataService) PlatformUI
								.getWorkbench().getService(IDataService.class))
								.getActiveDataDirectories().get(0).getInfo(); // TODO
																				// support
																				// multiple
																				// directories

						options.put("show_bubble", DOCLOG_CLICK_HANDLER_NAME);
						options.put("show_bubble_field",
								DOCLOG_CLICK_HANDLER_PARAM_FIELD);
						options.put("zones",
								new Timeline.Zone[] { new Timeline.Zone(
										dataSetInfo.getDateRange()
												.getStartDate().toISO8601(),
										dataSetInfo.getDateRange().getEndDate()
												.toISO8601()) });
						options.put(
								"decorators",
								new Timeline.Decorator[] { new Timeline.Decorator(
										dateRange.getStartDate().toISO8601(),
										dataSetInfo.getName(), dateRange
												.getEndDate().toISO8601(),
										dataSetInfo.getName()) });

						monitor.worked(1);

						LinkedList<DoclogRecord> filteredDoclogRecords = new LinkedList<DoclogRecord>();
						for (DoclogRecord doclogRecord : doclog
								.getDoclogRecords()) {
							if (doclogRecord.getAction() == DoclogAction.UNLOAD)
								continue;
							if (doclogRecord.getUrl().contains(
									"dddoc/html_devel/INDEX_"))
								continue;
							filteredDoclogRecords.add(doclogRecord);
						}

						monitor.worked(1);

						final String json = JsonUtils.generateJSON(
								filteredDoclogRecords, options, false, monitor
										.newChild(doclog.getDoclogRecords()
												.size()));

						DoclogTimeline.super.show(json);
						monitor.worked(doclog.getDoclogRecords().size());
						monitor.done();

						return Status.OK_STATUS;
					}
				};
				loader.schedule();
				return loader;
			}
		});
	}

	/**
	 * Centers the {@link DoclogTimeline} in a way that the given
	 * {@link TimeZoneDateRange} is in focus.
	 * 
	 * @param minMaxDateRange
	 */
	public void center(TimeZoneDateRange minMaxDateRange) {
		if (minMaxDateRange.getStartDate() != null)
			this.setCenterVisibleDate(minMaxDateRange.getStartDate()
					.toISO8601());
		else if (minMaxDateRange.getEndDate() != null)
			this.setCenterVisibleDate(minMaxDateRange.getEndDate().toISO8601());
	}

	public void center(Set<DoclogRecord> doclogRecords) {
		center(TimeZoneDateRange.calculateOuterDateRange(doclogRecords
				.toArray(new DoclogRecord[0])));
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
				ArrayList<Decorator> decorators = new ArrayList<Timeline.Decorator>(
						dateRanges.size());
				for (TimeZoneDateRange dateRange : dateRanges) {
					if (dateRange.getStartDate() != null
							&& dateRange.getEndDate() != null)
						decorators.add(new Decorator(dateRange.getStartDate()
								.toISO8601(), dateRange.getEndDate()
								.toISO8601()));
				}
				final String decoratorJSON = JsonUtils.jsonDecoratorList(
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

/**
	 * Highlights the provided {@link Doclog}.
	 * <p>
	 * This method internally uses {@link #highlight(List)
	 * 
	 * @param doclogRecords
	 */
	public void highlight(Set<DoclogRecord> doclogRecords) {
		List<TimeZoneDateRange> dateRanges = new LinkedList<TimeZoneDateRange>();
		for (DoclogRecord doclogRecord : doclogRecords) {
			dateRanges.add(doclogRecord.getDateRange());
		}
		this.highlight(dateRanges);
	}

	public Doclog getDoclogFile() {
		return this.doclog;
	}

	public List<TimeZoneDateRange> getHighlightedDateRanges() {
		return this.highlightedDateRanges;
	}

	@Override
	public String toString() {
		return DoclogTimeline.class.getSimpleName() + "(" + this.getData()
				+ ")";
	}
}
