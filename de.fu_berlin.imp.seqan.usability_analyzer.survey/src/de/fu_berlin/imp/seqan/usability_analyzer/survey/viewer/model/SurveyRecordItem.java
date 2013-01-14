package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.model;

import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class SurveyRecordItem implements ISurveyRecordItem {

	private SurveyRecord surveyRecord;
	private String key;

	public SurveyRecordItem(SurveyRecord surveyRecord, String column) {
		super();
		this.surveyRecord = surveyRecord;
		this.key = column;
	}

	@Override
	public SurveyRecord getSurveyRecord() {
		return this.surveyRecord;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public String getValue() {
		return this.surveyRecord.getField(this.key);
	}

}
