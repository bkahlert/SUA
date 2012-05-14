package de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
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
					for (DoclogRecord doclogRecord : doclogFile
							.getDoclogRecords()) {
						if (System.identityHashCode(doclogRecord) == doclogRecordIndex) {
							selectedDoclogRecord = doclogRecord;
						}
					}
					if (selectedDoclogRecord == null) {
						logger.fatal("Could not determine the passed "
								+ DoclogRecord.class.getSimpleName() + " index");
					}

					Dimension screenshotSize = null;
					try {
						screenshotSize = selectedDoclogRecord.getScreenshot()
								.getImageSize();
					} catch (IOException e) {
						logger.warn("Screenshot for " + selectedDoclogRecord
								+ " not computable", e);
					}
					DoclogDetailDialog doclogDetailDialog = new DoclogDetailDialog(
							null, new Point(screenshotSize.width,
									screenshotSize.height),
							selectedDoclogRecord);
					doclogDetailDialog.open();
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

	private DoclogFile doclogFile;

	public DoclogTimeline(Composite parent, int style) {
		super(parent, style);
		new DoclogClickHandler(browser, DOCLOG_CLICK_HANDLER_NAME);
	}

	/**
	 * Displays a {@link DoclogFile}'s content.
	 * <p>
	 * Hint: This method may be called from a non-UI thread. The relatively
	 * time-consuming JSON conversion is done asynchronously making this method
	 * return immediately.
	 * 
	 * @param doclogFile
	 * @param title
	 */
	public void show(final DoclogFile doclogFile, final String title) {
		this.doclogFile = doclogFile;

		new Thread(new Runnable() {
			@Override
			public void run() {
				HashMap<String, Object> options = new HashMap<String, Object>();
				if (title != null)
					options.put("title", title);

				TimeZoneDateRange dateRange = doclogFile.getDateRange();
				if (dateRange.getStartDate() != null)
					options.put("centerStart", dateRange.getStartDate().clone()
							.addMilliseconds(-10000l).toISO8601());
				/*
				 * TODO fix timeline javascript to support timeline_start and
				 * timeline_end
				 */
				// if (dateRange.getStartDate() != null)
				// options.put("timeline_start",
				// JsonUtils.formatDate(dateRange.getStartDate()));
				// if (dateRange.getEndDate() != null)
				// options.put("timeline_end",
				// JsonUtils.formatDate(dateRange.getEndDate()));
				DataSetInfo dataSetInfo = Activator.getDefault()
						.getDataSetInfo();

				options.put("show_bubble", DOCLOG_CLICK_HANDLER_NAME);
				options.put("show_bubble_field",
						DOCLOG_CLICK_HANDLER_PARAM_FIELD);
				options.put("zones", new Timeline.Zone[] { new Timeline.Zone(
						dataSetInfo.getStartDate().toISO8601(), dataSetInfo
								.getEndDate().toISO8601()) });
				options.put("decorators",
						new Timeline.Decorator[] { new Timeline.Decorator(
								dateRange.getStartDate().toISO8601(),
								dataSetInfo.getName(), dateRange.getEndDate()
										.toISO8601(), dataSetInfo.getName()) });

				LinkedList<DoclogRecord> filteredDoclogRecords = new LinkedList<DoclogRecord>();
				for (DoclogRecord doclogRecord : doclogFile.getDoclogRecords()) {
					if (doclogRecord.getAction() == DoclogAction.UNLOAD)
						continue;
					if (doclogRecord.getUrl().contains(
							"dddoc/html_devel/INDEX_"))
						continue;
					filteredDoclogRecords.add(doclogRecord);
				}
				final String json = JsonUtils.generateJSON(
						filteredDoclogRecords, options, false);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						DoclogTimeline.super.show(json);
					}
				});
			}
		}).start();
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
	 * Highlights the {@link DoclogFile}'s parts that fall in the given
	 * {@link TimeZoneDateRange}s.
	 * <p>
	 * Hint: This method may be called from a non-UI thread. The relatively
	 * time-consuming JSON conversion is done asynchronously making this method
	 * return immediately.
	 * 
	 * @param dateRanges
	 */
	public void highlight(final List<TimeZoneDateRange> dateRanges) {
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
	 * Highlights the provided {@link DoclogFile}.
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
}
