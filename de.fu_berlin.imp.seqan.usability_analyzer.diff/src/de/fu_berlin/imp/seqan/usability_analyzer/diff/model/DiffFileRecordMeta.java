package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DiffFileRecordMeta implements HasDateRange {
	private static DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.S Z");

	private static String getNameFromLine(String line) {
		return line.substring(4).split("\t")[0];
	}

	/**
	 * Extracts the date & time potion of the string. Unfortunately the date
	 * can't be parsed using {@link SimpleDateFormat}.
	 * 
	 * @param line
	 * @return
	 */
	private static Date getDateFromLine(String line) {
		String nanosecondsDateString = line.substring(4).split("\t")[1];
		Object[] millisecondsDate = DateUtil
				.nanoDateStringToMilliDateString(nanosecondsDateString);
		try {
			Date date = dateFormat.parse((String) millisecondsDate[0]);
			if ((Boolean) millisecondsDate[1])
				date.setTime(date.getTime() + 1);
			return date;
		} catch (ParseException e) {
			return null;
		}
	}

	private String fromFileName;
	private Date fromFileDate;
	private String toFileName;
	private Date toFileDate;

	private DateRange dateRange;

	public DiffFileRecordMeta(String fromFileLine, String toFileLine) {
		this.fromFileName = getNameFromLine(fromFileLine);
		this.fromFileDate = getDateFromLine(fromFileLine);
		this.toFileName = getNameFromLine(toFileLine);
		this.toFileDate = getDateFromLine(toFileLine);
		this.dateRange = new DateRange(
				DateUtil.isUnixTimeStart(fromFileDate) ? null : fromFileDate,
				toFileDate);
	}

	public String getFromFileName() {
		return fromFileName;
	}

	public Date getFromFileDate() {
		return fromFileDate;
	}

	public String getToFileName() {
		return toFileName;
	}

	public Date getToFileDate() {
		return toFileDate;
	}

	@Override
	public DateRange getDateRange() {
		return this.dateRange;
	}
}
