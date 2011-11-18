package de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
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

					DoclogRecord doclogRecord = doclogFile.getDoclogRecords()
							.get(doclogRecordIndex);
					Dimension screenshotSize = null;
					try {
						screenshotSize = doclogRecord.getScreenshot()
								.getImageSize();
					} catch (IOException e) {
						logger.warn("Screenshot for " + doclogRecord
								+ " not computable", e);
					}
					DoclogDetailDialog doclogDetailDialog = new DoclogDetailDialog(
							null, new Point(screenshotSize.width,
									screenshotSize.height), doclogRecord);
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

		HashMap<String, String> options = new HashMap<String, String>();
		if (this.title != null)
			options.put("title", this.title);

		DateRange dateRange = doclogFile.getDateRange();
		if (dateRange.getStartDate() != null)
			options.put("start_date",
					JsonUtils.formatDate(dateRange.getStartDate()));
		// if (dateRange.getStartDate() != null)
		// options.put("timeline_start",
		// JsonUtils.formatDate(dateRange.getStartDate()));
		// if (dateRange.getEndDate() != null)
		// options.put("timeline_end",
		// JsonUtils.formatDate(dateRange.getEndDate()));
		options.put("show_bubble", DOCLOG_CLICK_HANDLER_NAME);
		options.put("show_bubble_field", DOCLOG_CLICK_HANDLER_PARAM_FIELD);
		options.put(", arg1)

		String json = JsonUtils.generateJSON(doclogFile, options, false);
		System.err.println(json);
		super.show(json);
	}
}
