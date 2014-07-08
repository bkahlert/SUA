package de.fu_berlin.imp.apiua.diff.util;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;

import com.bkahlert.nebula.utils.CalendarUtils;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.identifier.ID;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.apiua.core.util.DateUtil;
import de.fu_berlin.imp.apiua.diff.model.impl.Diff;

public class DiffDataUtils {

	public static ID getId(IData diffFile) {
		ID id = null;
		if (Diff.PATTERN.matcher(diffFile.getName()).matches()) {
			Matcher matcher = Diff.PATTERN.matcher(diffFile.getName());
			if (matcher.find()) {
				id = new ID(matcher.group(1));
			}
		} else if (Diff.ZIPPED_PATTERN.matcher(diffFile.getName()).matches()) {
			Matcher matcher = Diff.ZIPPED_PATTERN.matcher(diffFile.getName());
			if (matcher.find()) {
				id = new ID(matcher.group(1));
			}
		}
		return id;
	}

	/**
	 * Returns the deprecated revision if available. If new file format found,
	 * uses the ISO8601 date.
	 * 
	 * @param data
	 * @return
	 */
	public static String getRevision(IData data) {
		String revision = "-";
		if (Diff.PATTERN.matcher(data.getName()).matches()) {
			Matcher matcher = Diff.PATTERN.matcher(data.getName());
			if (matcher.find()) {
				revision = matcher.group(2);
				try {
					Integer shortRevision = Integer.valueOf(revision);
					revision = shortRevision.toString();
				} catch (NumberFormatException e) {
				}
			}
		} else if (Diff.ZIPPED_PATTERN.matcher(data.getName()).matches()) {
			Matcher matcher = Diff.ZIPPED_PATTERN.matcher(data.getName());
			if (matcher.find()) {
				return CalendarUtils.toISO8601FileSystemCompatible(getDate(
						data, null).getCalendar());
			}
		}
		return revision;
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
		if (Diff.PATTERN.matcher(data.getName()).matches()) {
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
						} catch (Throwable e) {
							timeZone = TimeZone.getDefault();
						}
					}
					rawDate.setTime(rawDate.getTime()
							- timeZone.getOffset(rawDate.getTime()));
					date = new TimeZoneDate(rawDate, timeZone);
				}
			}
		} else if (Diff.ZIPPED_PATTERN.matcher(data.getName()).matches()) {
			Matcher matcher = Diff.ZIPPED_PATTERN.matcher(data.getName());
			if (matcher.find()) {
				if (matcher.group(10) != null) {
					// Date contains time zone
					date = new TimeZoneDate(matcher.group(3) + "-"
							+ matcher.group(4) + "-" + matcher.group(5) + "T"
							+ matcher.group(6) + ":" + matcher.group(7) + ":"
							+ matcher.group(8) + "." + matcher.group(9)
							+ matcher.group(11) + ":" + matcher.group(12));
				} else {
					// Date does not contain a time zone
					Date rawDate = DateUtil.getDate(
							Integer.valueOf(matcher.group(3)),
							Integer.valueOf(matcher.group(4)) - 1,
							Integer.valueOf(matcher.group(5)),
							Integer.valueOf(matcher.group(6)),
							Integer.valueOf(matcher.group(7)),
							Integer.valueOf(matcher.group(8)),
							Integer.valueOf(matcher.group(9)));

					TimeZone timeZone;
					if (defaultTimeZone != null) {
						timeZone = defaultTimeZone;
					} else {
						try {
							timeZone = new SUACorePreferenceUtil()
									.getDefaultTimeZone();
						} catch (Throwable e) {
							timeZone = TimeZone.getDefault();
						}
					}
					rawDate.setTime(rawDate.getTime()
							- timeZone.getOffset(rawDate.getTime()));
					date = new TimeZoneDate(rawDate, timeZone);
				}
			}
		}

		return date;
	}

	public static String getLocationHash(IData data) {
		String revision = "0000";
		if (Diff.ZIPPED_PATTERN.matcher(data.getName()).matches()) {
			Matcher matcher = Diff.ZIPPED_PATTERN.matcher(data.getName());
			if (matcher.find()) {
				revision = matcher.group(2);
			}
		}
		return revision;
	}

}
