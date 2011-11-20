package de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.DoclogManager;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.mapping.DoclogMapper;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;

public class Mapper {
	private DoclogManager doclogManager;
	private DoclogMapper doclogMapper;

	public Mapper(DoclogManager doclogManager, File logDirectory) {
		super();
		this.doclogManager = doclogManager;
		this.doclogMapper = new DoclogMapper(logDirectory);
	}

	public ID getID(Fingerprint fingerprint) {
		return this.doclogMapper.getID(fingerprint);
	}

	public List<Fingerprint> getFingerprints(ID id) {
		return this.doclogMapper.getFingerprints(id);
	}

	/**
	 * Returns the {@link Fingerprint}s of all <b>{@link Fingerprint} based</b>
	 * {@link DoclogFile}s belonging to the given {@link Token}.
	 * <p>
	 * If you are interested in <b>all</b> {@link DoclogFile}s you also need to
	 * check {@link #getID(Token)} as this method only returns an {@link ID} if
	 * an <b>{@link ID} based</b> {@link DoclogFile} exists.
	 * 
	 * @param token
	 * @return
	 */
	public List<Fingerprint> getFingerprints(Token token) {
		List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();

		List<DoclogFile> fingerprintDoclogFiles = this.doclogManager
				.getFingerprintDoclogFiles();
		for (DoclogFile fingerprintDoclogFile : fingerprintDoclogFiles) {
			if (token.equals(fingerprintDoclogFile.getToken()))
				fingerprints.add(fingerprintDoclogFile.getFingerprint());
		}

		return fingerprints;
	}

	public ID getID(Token token) {
		List<DoclogFile> doclogFiles = this.doclogManager.getDoclogFiles();
		for (DoclogFile doclogFile : doclogFiles) {
			if (token.equals(doclogFile.getToken()))
				return doclogFile.getId();
		}
		return null;
	}

	public Token getToken(Fingerprint fingerprint) {
		DoclogFile doclogFile = this.doclogManager.getDoclogFile(fingerprint);
		return doclogFile.getToken();
	}
}
