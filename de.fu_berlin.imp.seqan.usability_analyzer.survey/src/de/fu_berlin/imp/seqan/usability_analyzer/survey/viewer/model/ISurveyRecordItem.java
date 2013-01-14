package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.model;

import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

/**
 * Denotes an item of a {@link SurveyRecord}.
 * <p>
 * The 5th element of an {@link SurveyRecord}Êis the the {@link SurveyRecord}
 * itself + the column with the value 4.
 */
public interface ISurveyRecordItem {
	public SurveyRecord getSurveyRecord();

	public String getKey();

	public String getValue();
}
