package de.fu_berlin.imp.seqan.usability_analyzer.survey;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecordList;

public class SurveyRecordManager extends DataSourceManager {

	private Logger logger = Logger.getLogger(SurveyRecordManager.class);

	public static final int HEADING_LINE = 0;

	private File surveyCSV;
	private SurveyRecordList surveyRecords;

	public SurveyRecordManager(File surveyCSV)
			throws DataSourceInvalidException {
		super(surveyCSV);

		this.surveyCSV = surveyCSV;
	}

	public void scanRecords() {
		this.surveyRecords = new SurveyRecordList();
		try {
			CSVReader reader = new CSVReader(new FileReader(surveyCSV));
			String[] headings = null;
			String[] values;
			int line = 0;
			while ((values = reader.readNext()) != null) {
				if (line == HEADING_LINE) {
					headings = values;
				} else {
					if (values.length != headings.length) {
						if (values.length == 0 || values.length == 1)
							continue;
						else
							logger.error("Survey record field count does not match the number of headings\nHeadings: "
									+ headings.length
									+ "; Record fields: "
									+ values.length);
					} else {
						if (values.length > 0) {
							SurveyRecord surveyRecord = new SurveyRecord(
									headings, values);
							if (surveyRecord.getToken() == null) {
								logger.warn("Survey record without token discovered. The record will be ignored.\n"
										+ surveyRecord);
							} else {
								this.surveyRecords.add(surveyRecord);
							}
						}
					}
				}
				line++;
			}
			reader.close();
		} catch (Exception e) {// Catch exception if any
			logger.error("Could not open survey file");
		}
	}

	public SurveyRecordList getSurveyRecords() {
		return surveyRecords;
	}

	public SurveyRecord getSurveyRecord(Token token) {
		for (SurveyRecord surveyRecord : this.surveyRecords) {
			if (surveyRecord.getToken().equals(token)) {
				return surveyRecord;
			}
		}
		return null;
	}

	public List<Token> getTokens() {
		List<Token> tokens = new ArrayList<Token>();
		for (SurveyRecord surveyRecord : this.surveyRecords) {
			if (tokens.contains(surveyRecord.getToken())) {
				logger.error("Survey CSV contains duplicate token");
			} else {
				tokens.add(surveyRecord.getToken());
			}
		}
		return tokens;
	}
}
