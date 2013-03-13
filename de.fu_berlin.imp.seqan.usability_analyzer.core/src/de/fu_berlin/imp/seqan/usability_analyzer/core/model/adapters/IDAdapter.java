package de.fu_berlin.imp.seqan.usability_analyzer.core.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;

public class IDAdapter extends XmlAdapter<String, ID> {

	@Override
	public String marshal(ID v) throws Exception {
		if (v == null) {
			return "";
		}
		return v.getIdentifier();
	}

	@Override
	public ID unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty()) {
			return null;
		}
		return new ID(v);
	}

}