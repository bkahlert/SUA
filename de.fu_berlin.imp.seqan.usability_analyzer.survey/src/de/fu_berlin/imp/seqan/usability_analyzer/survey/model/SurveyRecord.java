package de.fu_berlin.imp.seqan.usability_analyzer.survey.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;

public class SurveyRecord {
	private Logger logger = Logger.getLogger(SurveyRecord.class);

	private static final String KEY_DATE = "Completed";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private Map<String, String> entries;
	private TimeZoneDate date;

	public SurveyRecord(String[] keys, String[] values) throws IOException {
		this.entries = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			this.entries.put(keys[i], (i < values.length) ? values[i] : null);
		}
		scanRecord();
	}

	private void scanRecord() {
		if (this.entries.containsKey(KEY_DATE)) {
			try {
				Date date = DATE_FORMAT.parse(this.entries.get(KEY_DATE));
				TimeZone timeZone;
				try {
					timeZone = new SUACorePreferenceUtil().getDefaultTimeZone();
				} catch (Exception e) {
					timeZone = TimeZone.getDefault();
				}
				this.date = new TimeZoneDate(date, timeZone);
			} catch (ParseException e) {
				logger.warn(
						"Could not parse date from "
								+ SurveyRecord.class.getSimpleName(), e);
			}
		}
	}

	public TimeZoneDate getDate() {
		return this.date;
	}

	public Token getToken() {
		if (this.entries.containsKey("Token")) {
			return new Token(this.entries.get("Token"));
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyRecord other = (SurveyRecord) obj;
		if (getToken() == null) {
			if (other.getToken() != null)
				return false;
		} else if (!getToken().equals(other.getToken()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String separator = "; ";
		StringBuilder sb = new StringBuilder();
		for (String key : this.entries.keySet()) {
			sb.append(key + "=" + this.entries.get(key));
			sb.append(separator);
		}
		sb.setLength(sb.length() - separator.length());
		return sb.toString();
	}

}
