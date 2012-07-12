package de.fu_berlin.imp.seqan.usability_analyzer.core.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;

public class FingerprintAdapter extends XmlAdapter<String, Fingerprint> {

	public String marshal(Fingerprint v) throws Exception {
		if (v == null)
			return "";
		return v.toString();
	}

	public Fingerprint unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty())
			return null;
		return new Fingerprint(v);
	}

}