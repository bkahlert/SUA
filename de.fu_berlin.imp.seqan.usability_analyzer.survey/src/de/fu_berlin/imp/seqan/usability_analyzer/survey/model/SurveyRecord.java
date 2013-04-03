package de.fu_berlin.imp.seqan.usability_analyzer.survey.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;

public class SurveyRecord {
	private Logger logger = Logger.getLogger(SurveyRecord.class);

	private static final String KEY_DATE = "Completed";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private ArrayList<String> keys;
	private ArrayList<String> values;
	private TimeZoneDate date;

	public SurveyRecord(String[] keys, String[] values) throws IOException {
		assert keys != null;
		assert values != null;
		assert keys.length == values.length;
		this.keys = new ArrayList<String>(Arrays.asList(keys));
		this.values = new ArrayList<String>(Arrays.asList(values));
		this.scanRecord();
	}

	private void scanRecord() {
		if (this.keys.contains(KEY_DATE)) {
			try {
				String dateString = this.values
						.get(this.keys.indexOf(KEY_DATE));
				if (dateString != null && !dateString.isEmpty()) {
					Date date = DATE_FORMAT.parse(dateString);
					TimeZone timeZone;
					try {
						timeZone = new SUACorePreferenceUtil()
								.getDefaultTimeZone();
					} catch (Exception e) {
						timeZone = TimeZone.getDefault();
					}
					this.date = new TimeZoneDate(date, timeZone);
				} else {
					this.date = null;
				}
			} catch (ParseException e) {
				this.logger.warn("Could not parse date from "
						+ SurveyRecord.class.getSimpleName(), e);
			}
		}
	}

	/**
	 * Returns a list of all keys of this {@link SurveyRecord}.
	 * 
	 * @return
	 */
	public List<String> getKeys() {
		return Collections.unmodifiableList(this.keys);
	}

	/**
	 * Returns a named field.
	 * 
	 * @param key
	 * @return null if key is not present
	 */
	public String getField(String key) {
		int idx = this.keys.indexOf(key);
		if (idx >= 0) {
			return this.values.get(idx);
		} else {
			return null;
		}
	}

	public TimeZoneDate getDate() {
		return this.date;
	}

	public ID getID() {
		if (this.keys.contains("id")) {
			String id = this.values.get(this.keys.indexOf("id"));
			try {
				return new ID(id);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public Token getToken() {
		if (this.keys.contains("Token")) {
			return new Token(this.values.get(this.keys.indexOf("Token")));
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.keys == null) ? 0 : this.keys.hashCode());
		result = prime * result
				+ ((this.values == null) ? 0 : this.values.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SurveyRecord)) {
			return false;
		}
		SurveyRecord other = (SurveyRecord) obj;
		if (this.keys == null) {
			if (other.keys != null) {
				return false;
			}
		} else if (!this.keys.equals(other.keys)) {
			return false;
		}
		if (this.values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!this.values.equals(other.values)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String separator = "; ";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.keys.size(); i++) {
			sb.append(this.keys.get(i) + "=" + this.values.get(i));
			sb.append(separator);
		}
		sb.setLength(sb.length() - separator.length());
		return sb.toString();
	}

}
