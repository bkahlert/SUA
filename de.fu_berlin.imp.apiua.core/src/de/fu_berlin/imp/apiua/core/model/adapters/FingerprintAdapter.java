package de.fu_berlin.imp.apiua.core.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.fu_berlin.imp.apiua.core.model.identifier.Fingerprint;

public class FingerprintAdapter extends XmlAdapter<String, Fingerprint> {

	@Override
	public String marshal(Fingerprint v) throws Exception {
		if (v == null) {
			return "";
		}
		return v.getIdentifier();
	}

	@Override
	public Fingerprint unmarshal(String v) throws Exception {
		if (v == null || v.isEmpty()) {
			return null;
		}
		return new Fingerprint(v);
	}

}