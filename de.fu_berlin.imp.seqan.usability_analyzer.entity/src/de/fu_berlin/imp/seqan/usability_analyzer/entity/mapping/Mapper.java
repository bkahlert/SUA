package de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;
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

	public IIdentifier getID(Fingerprint fingerprint) {
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
	 * check {@link #getIDs(Token)} as this method only returns an {@link DateId} if
	 * an <b>{@link DateId} based</b> {@link Doclog} exists.
	 * 
	 * @param token
	 * @return
	 */
	public List<Fingerprint> getFingerprints(Token token) {
		List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
		for (IIdentifier identifier : this.doclogDataContainer
				.getIdentifiers(token)) {
			if (identifier instanceof Fingerprint) {
				fingerprints.add((Fingerprint) identifier);
			}
		}
		return fingerprints;
	}

	public List<ID> getIDs(Token token) {
		List<ID> ids = new ArrayList<ID>();
		for (IIdentifier identifier : this.doclogDataContainer
				.getIdentifiers(token)) {
			if (identifier instanceof ID) {
				ids.add((ID) identifier);
			}
		}
		return ids;
	}

	public Token getToken(Fingerprint fingerprint) {
		return this.doclogDataContainer.getToken(fingerprint);
	}
}
