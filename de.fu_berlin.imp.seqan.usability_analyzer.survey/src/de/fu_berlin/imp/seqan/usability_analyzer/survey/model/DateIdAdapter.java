package de.fu_berlin.imp.seqan.usability_analyzer.survey.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateIdAdapter extends XmlAdapter<String, DateId> {

	@Override
	public String marshal(DateId v) throws Exception {
		if (v == null) {
			return "";
		}
		return v.getIdentifier();
	}

	@Override
	public DateId unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty()) {
			return null;
		}
		return new DateId(v);
	}

}