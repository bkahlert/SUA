package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.olat.core.util.URIHelper;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DoclogRecord implements Comparable<DoclogRecord>, HasDateRange,
		ILocatable, HasIdentifier {

	private static final long serialVersionUID = -8279575943640177616L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(DoclogRecord.class);

	// [^\\t] selects everything but a tabulator
	// line #1: date + optional milliseconds + optional time zone
	public static final Pattern PATTERN = Pattern
			.compile("([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})[-:]([\\d]{2})[-:]([\\d]{2})(\\.[\\d]{3})?(([\\+-][\\d]{2}):?([\\d]{2}))?"
					+ "\\t([^\\t]+?)(-([^\\t]+?))?" // action + param
					+ "\\t([^\\t]+)" // url
					+ "\\t([^\\t]+)\\t([^\\t]+)" // ip + proxy ip
					+ "\\t(-?\\d+)\\t(-?\\d+)\\t(-?\\d+)\\t(-?\\d+)"); // scroll
																		// x,y +

	// window w,h

	public static TimeZoneDate getDate(String line) {
		TimeZoneDate date = null;
		Matcher matcher = PATTERN.matcher(line);
		if (matcher.find()) {
			if (matcher.group(8) != null) {
				// Date contains time zone
				date = new TimeZoneDate(matcher.group(1) + "-"
						+ matcher.group(2) + "-" + matcher.group(3) + "T"
						+ matcher.group(4) + ":" + matcher.group(5) + ":"
						+ matcher.group(6) + matcher.group(9) + ":"
						+ matcher.group(10));
				// Add milliseconds
				if (matcher.group(7) != null) {
					date.setTime(date.getTime()
							+ Integer.parseInt(matcher.group(7).substring(1)));
				}
			} else {
				// Date does not contain a time zone
				Date rawDate = DateUtil.getDate(
						Integer.valueOf(matcher.group(1)),
						Integer.valueOf(matcher.group(2)) - 1,
						Integer.valueOf(matcher.group(3)),
						Integer.valueOf(matcher.group(4)),
						Integer.valueOf(matcher.group(5)),
						Integer.valueOf(matcher.group(6)));
				// Add milliseconds
				if (matcher.group(7) != null) {
					rawDate.setTime(rawDate.getTime()
							+ Integer.parseInt(matcher.group(7).substring(1)));
				}

				TimeZone timeZone;
				try {
					timeZone = new SUACorePreferenceUtil().getDefaultTimeZone();
				} catch (NoClassDefFoundError e) {
					timeZone = TimeZone.getDefault();
				}
				rawDate.setTime(rawDate.getTime()
						- timeZone.getOffset(rawDate.getTime()));
				date = new TimeZoneDate(rawDate, timeZone);
			}
		}
		return date;
	}

	private static int MAX_SHORT_URL_LENGTH = 30;
	private static String SHORT_URL_SHORTENER = "...";

	private URI uri;
	private final Doclog doclog;
	private final String rawContent;

	private String url;
	private String ip;
	private String proxyIp;
	private DoclogAction action;
	private String actionParameter;
	private TimeZoneDate date;
	private Point scrollPosition;
	private Point windowDimensions;
	private Rectangle bounds; // made up from scrollPos and windowDimensions

	private DoclogScreenshot screenshot;
	private Long millisecondsPassed;

	public DoclogRecord(Doclog doclog, String line)
			throws DataSourceInvalidException {
		this.doclog = doclog;
		this.rawContent = line;

		Matcher matcher = PATTERN.matcher(line);
		if (matcher.find()) {
			this.url = cleanUrl(matcher.group(14));
			if (this.url == null) {
				throw new DataSourceInvalidException("The url is invalid");
			}
			this.ip = matcher.group(15);
			this.proxyIp = matcher.group(16);

			this.action = DoclogAction.getByString(matcher.group(11));
			this.actionParameter = matcher.group(13);

			this.date = getDate(line);

			this.scrollPosition = new Point(
					Integer.parseInt(matcher.group(17)),
					Integer.parseInt(matcher.group(18)));
			this.windowDimensions = new Point(Integer.parseInt(matcher
					.group(19)), Integer.parseInt(matcher.group(20)));

			this.bounds = new Rectangle(this.scrollPosition.x,
					this.scrollPosition.y, this.windowDimensions.x,
					this.windowDimensions.y);

			try {
				if (doclog != null) {
					this.screenshot = new DoclogScreenshot(this);
				}
			} catch (UnsupportedEncodingException e) {
				throw new DataSourceInvalidException(
						"The doclog contains an invalid URL that can not be mapped to a file.",
						e);
			}
		} else {
			throw new DataSourceInvalidException(
					"The doclog line did not match to the expected format:\n"
							+ line);
		}
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.doclog.getIdentifier();
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			try {
				this.uri = new URI(this.getDoclog().getUri().toString() + "/"
						+ URLEncoder.encode(this.rawContent, "UTF-8"));
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + DoclogRecord.class, e);
			}
		}
		return this.uri;
	}

	public String getRawContent() {
		return this.rawContent;
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

	public Doclog getDoclog() {
		return this.doclog;
	}

	public String getUrl() {
		return this.url;
	}

	public String getRawUrl() {
		return this.url;
	}

	public String getShortUrl() {
		String protocollessUrl = this.getUrl().replaceAll("\\w*://", "");
		if (protocollessUrl.length() > MAX_SHORT_URL_LENGTH) {
			int startLength = (int) Math
					.round(((double) MAX_SHORT_URL_LENGTH - SHORT_URL_SHORTENER
							.length()) * 0.3);
			int endLength = MAX_SHORT_URL_LENGTH - SHORT_URL_SHORTENER.length()
					- startLength;
			return protocollessUrl.substring(0, startLength)
					+ SHORT_URL_SHORTENER
					+ protocollessUrl.substring(protocollessUrl.length()
							- endLength);
		} else {
			return protocollessUrl;
		}
	}

	public String getIp() {
		return this.ip;
	}

	public String getProxyIp() {
		return (this.proxyIp != null && !this.proxyIp.equals("-")) ? this.proxyIp
				: null;
	}

	public DoclogAction getAction() {
		return this.action;
	}

	public String getActionParameter() {
		return this.actionParameter;
	}

	void setActionParameter(String actionParameter) {
		this.actionParameter = actionParameter;
	}

	TimeZoneDate getDate() {
		return this.date;
	}

	public Point getScrollPosition() {
		return this.scrollPosition;
	}

	public Point getWindowDimensions() {
		return this.windowDimensions;
	}

	public Rectangle getBounds() {
		return this.bounds;
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

	@Override
	public TimeZoneDateRange getDateRange() {
		if (this.date == null) {
			return null;
		}

		TimeZoneDate endDate = this.date.clone();
		if (this.millisecondsPassed != null) {
			endDate.addMilliseconds(this.millisecondsPassed);
		}
		return new TimeZoneDateRange(this.date, endDate);
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
