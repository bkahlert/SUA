package de.fu_berlin.imp.seqan.usability_analyzer.doclog.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class DoclogMapper {
	public static final String DOCLOG_ID_RELPATH = "doclog/doclog_id.txt";
	private File doclogId;
	private Map<Fingerprint, ID> fingerprintIdMap;
	private Map<ID, List<Fingerprint>> idFingerprintMap;

	public DoclogMapper(File logDirectory) {
		this.doclogId = new File(logDirectory.getAbsolutePath() + "/"
				+ DOCLOG_ID_RELPATH);
		this.buildFingerprintIdMap();
		this.buildIdFingerprintMap();
	}

	private void buildFingerprintIdMap() {
		fingerprintIdMap = new HashMap<Fingerprint, ID>();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(this.doclogId));
			Set<Object> fingerprints = properties.keySet();
			for (Object fingerprint : fingerprints) {
				Object id = properties.getProperty((String) fingerprint);
				// TODO id = "undefined" nicht verwenden
				this.fingerprintIdMap.put(
						new Fingerprint((String) fingerprint), new ID(
								(String) id));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildIdFingerprintMap() {
		idFingerprintMap = new HashMap<ID, List<Fingerprint>>();
		for (ID id : fingerprintIdMap.values()) {
			for (Fingerprint fingerprint : fingerprintIdMap.keySet()) {
				ID currentID = fingerprintIdMap.get(fingerprint);
				if (id.equals(currentID)) {
					if (idFingerprintMap.get(id) == null)
						idFingerprintMap.put(id, new ArrayList<Fingerprint>());
					List<Fingerprint> fingerprints = idFingerprintMap.get(id);
					fingerprints.add(fingerprint);
					break;
				}
			}
		}
	}

	public ID getID(Fingerprint fingerprint) {
		return this.fingerprintIdMap.get(fingerprint);
	}

	public List<Fingerprint> getFingerprints(ID id) {
		List<Fingerprint> fingerprints = this.idFingerprintMap.get(id);
		return (fingerprints != null) ? fingerprints
				: new ArrayList<Fingerprint>();
	}
}
