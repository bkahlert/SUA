package de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping;

import java.io.FileNotFoundException;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.mapping.DoclogKeyMap;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;

public class Mapper {
	private DoclogDataContainer doclogDataContainer;
	private DoclogKeyMap doclogMapper;

	public Mapper(DoclogDataContainer doclogDataContainer)
			throws FileNotFoundException {
		this.doclogDataContainer = doclogDataContainer;
		this.doclogMapper = DoclogKeyMap.load(doclogDataContainer
				.getMappingFile());
	}

	public ID getID(Fingerprint fingerprint) {
		return this.doclogMapper.getID(fingerprint);
	}

	public List<Fingerprint> getFingerprints(ID id) {
		return this.doclogMapper.getFingerprints(id);
	}

	/**
	 * Returns the {@link Fingerprint}s of all <b>{@link Fingerprint} based</b>
	 * {@link Doclog}s belonging to the given {@link Token}.
	 * <p>
	 * If you are interested in <b>all</b> {@link Doclog}s you also need to
	 * check {@link #getID(Token)} as this method only returns an {@link ID} if
	 * an <b>{@link ID} based</b> {@link Doclog} exists.
	 * 
	 * @param token
	 * @return
	 */
	public List<Fingerprint> getFingerprints(Token token) {
		return this.doclogDataContainer.getFingerprints(token);
	}

	public ID getID(Token token) {
		return this.doclogDataContainer.getID(token);
	}

	public Token getToken(Fingerprint fingerprint) {
		return this.doclogDataContainer.getToken(fingerprint);
	}
}
