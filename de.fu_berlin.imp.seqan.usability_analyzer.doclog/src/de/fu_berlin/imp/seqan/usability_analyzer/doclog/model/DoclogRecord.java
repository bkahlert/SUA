package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;
import org.olat.core.util.URIHelper;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DoclogRecord implements Comparable<DoclogRecord>, HasDateRange {
	/*
	 * [^\\t] selects everything but a tabulator
	 */
	public static final String PATTERN = "([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})"
			+ "\\t([^\\t]+?)(-([^\\t]+?))?"
			+ "\\t([^\\t]+)"
			+ "\\t([^\\t]+)\\t([^\\t]+)"
			+ "\\t(\\d+)\\t(\\d+)\\t(\\d+)\\t(\\d+)";

	private DoclogFile doclogFile;
	private String rawContent;

	private String url;
	private String ip;
	private String proxyIp;
	private DoclogAction action;
	private String actionParameter;
	private Date date;
	private Point scrollPosition;
	private Point windowDimensions;

	private DoclogScreenshot screenshot;
	private Long millisecondsPassed;

	public DoclogRecord(DoclogFile doclogFile, String line)
			throws DataSourceInvalidException {
		this.doclogFile = doclogFile;
		this.rawContent = line;

		Matcher matcher = Pattern.compile(PATTERN).matcher(line);
		if (matcher.find()) {
			this.url = cleanUrl(matcher.group(10));
			if (this.url == null)
				throw new DataSourceInvalidException("The url is invalid");
			this.ip = matcher.group(11);
			this.proxyIp = matcher.group(12);

			this.action = DoclogAction.getByString(matcher.group(7));
			this.actionParameter = matcher.group(9);

			this.date = DateUtil.getDate(Integer.valueOf(matcher.group(1)),
					Integer.valueOf(matcher.group(2)) - 1,
					Integer.valueOf(matcher.group(3)),
					Integer.valueOf(matcher.group(4)),
					Integer.valueOf(matcher.group(5)),
					Integer.valueOf(matcher.group(6)));

			this.scrollPosition = new Point(
					Integer.parseInt(matcher.group(13)),
					Integer.parseInt(matcher.group(14)));
			this.windowDimensions = new Point(Integer.parseInt(matcher
					.group(15)), Integer.parseInt(matcher.group(16)));

			try {
				if (doclogFile != null)
					this.screenshot = new DoclogScreenshot(this);
			} catch (UnsupportedEncodingException e) {
				throw new DataSourceInvalidException(
						"The doclog contains an invalid URL that can not be mapped to a file.",
						e);
			}
		} else {
			throw new DataSourceInvalidException(
					"The doclog line didn't not match to the expected format.");
		}
	}

	protected static String cleanUrl(String url) {
		try {
			String noAngleBrackets = url.replace("<", "%3C")
					.replace(">", "%3E");
			String noWhitespaces = noAngleBrackets.replace(" ", "%20");
			String onlyOneSharp = noWhitespaces.replaceFirst("#", "^^^")
					.replaceAll("#", "%23").replaceFirst("\\^\\^\\^", "#");
			String noId = new URIHelper(onlyOneSharp).removeParameter("id")
					.toString();
			return noId;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DoclogFile getDoclogPath() {
		return this.doclogFile;
	}

	public String getUrl() {
		return this.url;
	}

	public String getRawUrl() {
		return this.url;
	}

	public String getIp() {
		return ip;
	}

	public String getProxyIp() {
		return (proxyIp != null && !proxyIp.equals("-")) ? proxyIp : null;
	}

	public DoclogAction getAction() {
		return action;
	}

	public String getActionParameter() {
		return actionParameter;
	}

	Date getDate() {
		return date;
	}

	public Point getScrollPosition() {
		return scrollPosition;
	}

	public Point getWindowDimensions() {
		return windowDimensions;
	}

	public void setScreenshot(File screenshotFile) throws IOException {
		this.screenshot.setFile(screenshotFile);
	}

	public DoclogScreenshot getScreenshot() {
		return this.screenshot;
	}

	void setMillisecondsPassed(Long millisecondsPassed) {
		this.millisecondsPassed = millisecondsPassed;
	}

	public DateRange getDateRange() {
		if (this.date == null)
			return null;

		long start = this.date.getTime();
		return new DateRange(start, start
				+ ((this.millisecondsPassed != null) ? this.millisecondsPassed
						: 0));
	}

	@Override
	public int compareTo(DoclogRecord doclogRecord) {
		if (this.getDate() == null) {
			if (doclogRecord.getDate() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (doclogRecord.getDate() == null) {
			return 1;
		} else {
			return this.getDate().compareTo(doclogRecord.getDate());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.rawContent;
	}
}
