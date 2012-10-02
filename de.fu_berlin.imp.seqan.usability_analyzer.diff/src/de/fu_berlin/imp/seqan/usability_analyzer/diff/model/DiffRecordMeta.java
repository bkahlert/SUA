package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.text.SimpleDateFormat;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DiffRecordMeta implements HasDateRange {
	private static String getNameFromLine(String line) {
		String name = line.substring(4).split("\t")[0];
		if (name.substring(0, "./".length()).equals("./")) {
			name = name.substring("./".length());
		}
		return name;
	}

	/**
	 * Extracts the date & time potion of the string. Unfortunately the date
	 * can't be parsed using {@link SimpleDateFormat}.
	 * 
	 * @param line
	 * @return
	 */
	private static TimeZoneDate getDateFromLine(String line) {
		// e.g. 2011-09-13 12:08:40.000000000 +0200
		String nanosecondsDateString = line.substring(4).split("\t")[1];
		// e.g. 2011-09-13T12:08:40.000000000+0200
		String noWhitespaces = nanosecondsDateString.replaceFirst(" ", "T")
				.replaceFirst(" ", "");
		// e.g. 2011-09-13T12:08:40.000000000+02:00
		String iso8601 = noWhitespaces.substring(0, noWhitespaces.length() - 2)
				+ ":" + noWhitespaces.substring(noWhitespaces.length() - 2);
		return new TimeZoneDate(iso8601);
	}

	private String fromFileName;
	private TimeZoneDate fromFileDate;
	private String toFileName;
	private TimeZoneDate toFileDate;

	private TimeZoneDateRange dateRange;

	public DiffRecordMeta(String fromFileLine, String toFileLine) {
		this.fromFileName = getNameFromLine(fromFileLine);
		this.fromFileDate = getDateFromLine(fromFileLine);
		this.toFileName = getNameFromLine(toFileLine);
		this.toFileDate = getDateFromLine(toFileLine);

		TimeZoneDate startDate = DateUtil.isUnixTimeStart(fromFileDate
				.getDate()) ? null : fromFileDate;
		TimeZoneDate endDate = DateUtil.isUnixTimeStart(toFileDate.getDate()) ? null
				: toFileDate;
		this.dateRange = new TimeZoneDateRange(startDate, endDate);
	}

	public String getFromFileName() {
		return fromFileName;
	}

	public TimeZoneDate getFromFileDate() {
		return fromFileDate;
	}

	public String getToFileName() {
		return toFileName;
	}

	public TimeZoneDate getToFileDate() {
		return toFileDate;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.dateRange;
	}
}
