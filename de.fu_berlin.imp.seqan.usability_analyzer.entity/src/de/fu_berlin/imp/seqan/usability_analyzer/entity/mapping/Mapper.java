package de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping;

import java.io.File;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.mapping.DoclogMapper;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;

public class Mapper {
	private DoclogDirectory doclogDirectory;
	private DoclogMapper doclogMapper;

	public Mapper(DoclogDirectory doclogDirectory, File logDirectory) {
		super();
		this.doclogDirectory = doclogDirectory;
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
		return this.doclogDirectory.getFingerprints(token);
	}

	public ID getID(Token token) {
		return this.doclogDirectory.getID(token);
	}

	public Token getToken(Fingerprint fingerprint) {
		return this.doclogDirectory.getToken(fingerprint);
	}
}
