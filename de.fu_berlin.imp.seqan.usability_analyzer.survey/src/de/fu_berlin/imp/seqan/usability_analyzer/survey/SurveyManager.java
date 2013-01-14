package de.fu_berlin.imp.seqan.usability_analyzer.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import au.com.bytecode.opencsv.CSVReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecordList;

/**
 * An instance of this class is responsible for managing one survey represented
 * as CSV file.
 * 
 * @author bkahlert
 * 
 */
public class SurveyManager {

	private static final Logger LOGGER = Logger.getLogger(SurveyManager.class);

	public static final int HEADING_LINE = 0;

	private IData data;
	private SurveyRecordList surveyRecords;

	public SurveyManager(IData data) {
		this.data = data;
	}

	public void scanRecords(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor);
		this.surveyRecords = new SurveyRecordList();
		DataReader dataReader = new DataReader(this.data);
		CSVReader csvReader = new CSVReader(dataReader, ';', '"');

		try {
			String[] headings = new String[0];
			String[] values;
			while ((values = csvReader.readNext()) != null) {
				progress.setWorkRemaining(50);
				if (values.length > 0 && values[0].equals("id")) {
					headings = values;
				} else {
					if (values.length != headings.length) {
						LOGGER.error("Survey record field count does not match the number of headings\nHeadings: "
								+ headings.length
								+ "; Record fields: "
								+ values.length);
					} else {
						SurveyRecord surveyRecord = new SurveyRecord(headings,
								values);
						if (surveyRecord.getToken() == null) {
							LOGGER.warn("Survey record without token discovered. ("
									+ surveyRecord.toString().substring(0, 100)
									+ "...)");
						}
						this.surveyRecords.add(surveyRecord);
					}
				}
				progress.worked(1);
			}
		} catch (IOException e) {
			LOGGER.error("Error reading CSV line in " + this.data, e);
		}

		try {
			csvReader.close();
		} catch (IOException e) {
			LOGGER.error("Error closing " + CSVReader.class.getSimpleName()
					+ " for " + this.data, e);
		}
		dataReader.close();
	}

	public SurveyRecordList getSurveyRecords() {
		return surveyRecords;
	}

	public SurveyRecord getSurveyRecord(ID id) {
		for (SurveyRecord surveyRecord : this.surveyRecords) {
			ID surveyRecordId = surveyRecord.getID();
			if (surveyRecordId != null && surveyRecordId.equals(id)) {
				return surveyRecord;
			}
		}
		return null;
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
				LOGGER.error("Survey CSV contains duplicate token");
			} else {
				tokens.add(surveyRecord.getToken());
			}
		}
		return tokens;
	}
}
