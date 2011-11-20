package de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
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

	private class CustomFunction extends BrowserFunction {
		CustomFunction(Browser browser, String name) {
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
				logger.error(CustomFunction.class.getSimpleName()
						+ " call parameters are invalid");
			}
			return null;
		}
	}

	private String title = null;

	private DoclogFile doclogFile;

	public DoclogTimeline(Composite parent, int style) {
		super(parent, style);
		new CustomFunction(browser, DOCLOG_CLICK_HANDLER_NAME);
	}

	public DoclogTimeline(Composite parent, int style, String title) {
		this(parent, style);
		this.title = title;
	}

	public void show(DoclogFile doclogFile) {
		this.doclogFile = doclogFile;

		HashMap<String, Object> options = new HashMap<String, Object>();
		if (this.title != null)
			options.put("title", this.title);

		LocalDateRange dateRange = doclogFile.getDateRange();
		if (dateRange.getStartDate() != null)
			options.put("centerStart", dateRange.getStartDate().clone()
					.addMilliseconds(-10000l).toISO8601());
		// if (dateRange.getStartDate() != null)
		// options.put("timeline_start",
		// JsonUtils.formatDate(dateRange.getStartDate()));
		// if (dateRange.getEndDate() != null)
		// options.put("timeline_end",
		// JsonUtils.formatDate(dateRange.getEndDate()));
		DataSetInfo dataSetInfo = Activator.getDefault().getDataSetInfo();

		options.put("show_bubble", DOCLOG_CLICK_HANDLER_NAME);
		options.put("show_bubble_field", DOCLOG_CLICK_HANDLER_PARAM_FIELD);
		options.put("zones", new Timeline.Zone[] { new Timeline.Zone(
				dataSetInfo.getStartDate().toISO8601(), dataSetInfo
						.getEndDate().toISO8601()) });
		options.put(
				"decorators",
				new Timeline.Decorator[] { new Timeline.Decorator(dateRange
						.getStartDate().toISO8601(), dataSetInfo.getName(),
						dateRange.getEndDate().toISO8601(), dataSetInfo
								.getName()) });

		LinkedList<DoclogRecord> filteredDoclogRecords = new LinkedList<DoclogRecord>();
		for (DoclogRecord doclogRecord : doclogFile.getDoclogRecords()) {
			if (doclogRecord.getAction() == DoclogAction.UNLOAD)
				continue;
			if (doclogRecord.getUrl().contains("dddoc/html_devel/INDEX_"))
				continue;
			filteredDoclogRecords.add(doclogRecord);
		}
		String json = JsonUtils.generateJSON(filteredDoclogRecords, options,
				false);
		super.show(json);
	}

	public void highlight(List<LocalDateRange> dateRanges) {
		ArrayList<Decorator> decorators = new ArrayList<Timeline.Decorator>(
				dateRanges.size());
		for (LocalDateRange dateRange : dateRanges) {
			if (dateRange.getStartDate() != null
					&& dateRange.getEndDate() != null)
				decorators.add(new Decorator(dateRange.getStartDate()
						.toISO8601(), dateRange.getEndDate().toISO8601()));
		}
		applyDecorators(JsonUtils.jsonDecoratorList(decorators, false));
	}
}
