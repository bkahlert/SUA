package de.fu_berlin.imp.seqan.usability_analyzer.survey.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;

public class SurveyRecord {
	private Logger logger = Logger.getLogger(SurveyRecord.class);

	private static final String KEY_DATE = "Completed";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private Map<String, String> surveyRecords;
	private Date date;

	public SurveyRecord(String[] keys, String[] values) throws IOException {
		this.surveyRecords = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			this.surveyRecords.put(keys[i], (i < values.length) ? values[i]
					: null);
		}
		scanRecord();
	}

	private void scanRecord() {
		if (this.surveyRecords.containsKey(KEY_DATE)) {
			try {
				this.date = DATE_FORMAT.parse(this.surveyRecords.get(KEY_DATE));
			} catch (ParseException e) {
				logger.warn(
						"Could not parse date from "
								+ SurveyRecord.class.getSimpleName(), e);
			}
		}
	}

	public Date getDate() {
		return this.date;
	}

	public Token getToken() {
		if (this.surveyRecords.containsKey("Token")) {
			return new Token(this.surveyRecords.get("Token"));
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((surveyRecords == null) ? 0 : surveyRecords.hashCode());
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

}
