package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, date, hourOfDay, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
