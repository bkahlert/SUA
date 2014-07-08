package de.fu_berlin.imp.apiua.survey.viewer.model;

import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyRecord;

/**
 * Denotes an item of a {@link CSVSurveyRecord}.
 * <p>
 * The 5th element of an {@link CSVSurveyRecord}Êis the the {@link CSVSurveyRecord}
 * itself + the column with the value 4.
 */
public interface ISurveyRecordItem {
	public CSVSurveyRecord getSurveyRecord();

	public String getKey();

	public String getValue();
}
