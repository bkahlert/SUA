package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.model;

import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.csv.CSVSurveyRecord;

public class SurveyRecordItem implements ISurveyRecordItem {

	private CSVSurveyRecord cSVSurveyRecord;
	private String key;

	public SurveyRecordItem(CSVSurveyRecord cSVSurveyRecord, String column) {
		super();
		this.cSVSurveyRecord = cSVSurveyRecord;
		this.key = column;
	}

	@Override
	public CSVSurveyRecord getSurveyRecord() {
		return this.cSVSurveyRecord;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public String getValue() {
		return this.cSVSurveyRecord.getField(this.key);
	}

}
