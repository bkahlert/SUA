package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;

public class DiffDataUtils {

	public static ID getId(IData diffFile) {
		ID id = null;
		Matcher matcher = Diff.PATTERN.matcher(diffFile.getName());
		if (matcher.find())
			id = new ID(matcher.group(1));
		return id;
	}

	public static Long getRevision(IData data) {
		String revision = null;
		Matcher matcher = Diff.PATTERN.matcher(data.getName());
		if (matcher.find())
			revision = matcher.group(2);
		try {
			return Long.parseLong(revision);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Constructs a new date with an attached {@link TimeZone} based on a
	 * {@link IData}'s name. If the data's name does not contain any time zone
	 * information, the defaultTimeZone will be used.
	 * 
	 * @param data
	 * @param defaultTimeZone
	 *            if null, the preferred time zone (stored in preferences) is
	 *            used. If both information are not available the system's
	 *            default time zone is used.
	 * @return
	 */
	public static TimeZoneDate getDate(IData data, TimeZone defaultTimeZone) {
		TimeZoneDate date = null;
		Matcher matcher = Diff.PATTERN.matcher(data.getName());
		if (matcher.find()) {
			if (matcher.group(9) != null) {
				// Date contains time zone
				date = new TimeZoneDate(matcher.group(3) + "-"
						+ matcher.group(4) + "-" + matcher.group(5) + "T"
						+ matcher.group(6) + ":" + matcher.group(7) + ":"
						+ matcher.group(8) + matcher.group(10) + ":"
						+ matcher.group(11));
			} else {
				// Date does not contain a time zone
				Date rawDate = DateUtil.getDate(
						Integer.valueOf(matcher.group(3)),
						Integer.valueOf(matcher.group(4)) - 1,
						Integer.valueOf(matcher.group(5)),
						Integer.valueOf(matcher.group(6)),
						Integer.valueOf(matcher.group(7)),
						Integer.valueOf(matcher.group(8)));

				TimeZone timeZone;
				if (defaultTimeZone != null) {
					timeZone = defaultTimeZone;
				} else {
					try {
						timeZone = new SUACorePreferenceUtil()
								.getDefaultTimeZone();
					} catch (Exception e) {
						timeZone = TimeZone.getDefault();
					}
				}
				rawDate.setTime(rawDate.getTime()
						- timeZone.getOffset(rawDate.getTime()));
				date = new TimeZoneDate(rawDate, timeZone);
			}
		}
		return date;
	}

}
