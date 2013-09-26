package de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import au.com.bytecode.opencsv.CSVReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;

/**
 * An instance of this class is responsible for managing one survey represented
 * as CSV file.
 * 
 * @author bkahlert
 * 
 */
public class XMLSurveyManager {

	private static final Logger LOGGER = Logger.getLogger(XMLSurveyManager.class);

	public static final int HEADING_LINE = 0;

	private IData data;
	private CSVSurveyRecordList surveyRecords;

	public XMLSurveyManager(IData data) {
		this.data = data;
	}

	public void scanRecords(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor);
		this.surveyRecords = new CSVSurveyRecordList();
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
						CSVSurveyRecord cSVSurveyRecord = new CSVSurveyRecord(headings,
								values);
						if (cSVSurveyRecord.getToken() == null) {
							LOGGER.warn("Survey record without token discovered. ("
									+ cSVSurveyRecord.toString().substring(0, 100)
									+ "...)");
						}
						this.surveyRecords.add(cSVSurveyRecord);
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

	public CSVSurveyRecordList getSurveyRecords() {
		return this.surveyRecords;
	}

	public CSVSurveyRecord getSurveyRecord(IIdentifier identifier) {
		Assert.isNotNull(identifier);
		for (CSVSurveyRecord cSVSurveyRecord : this.surveyRecords) {
			if (identifier.equals(cSVSurveyRecord.getID())
					|| identifier.equals(cSVSurveyRecord.getToken())) {
				return cSVSurveyRecord;
			}
		}
		return null;
	}

	public List<Token> getTokens() {
		List<Token> tokens = new ArrayList<Token>();
		for (CSVSurveyRecord cSVSurveyRecord : this.surveyRecords) {
			if (tokens.contains(cSVSurveyRecord.getToken())) {
				LOGGER.error("Survey CSV contains duplicate token");
			} else {
				tokens.add(cSVSurveyRecord.getToken());
			}
		}
		return tokens;
	}
}
