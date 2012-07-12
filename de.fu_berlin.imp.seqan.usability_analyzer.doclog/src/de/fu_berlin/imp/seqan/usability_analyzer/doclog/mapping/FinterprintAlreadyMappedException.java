package de.fu_berlin.imp.seqan.usability_analyzer.doclog.mapping;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class FinterprintAlreadyMappedException extends Exception {

	private static final long serialVersionUID = -3973751907072660227L;
	private Fingerprint fingerprint;
	private ID oldID;
	private ID newID;

	public FinterprintAlreadyMappedException(Fingerprint fingerprint, ID oldID,
			ID newID) {
		super("The " + Fingerprint.class.getSimpleName() + " \"" + fingerprint
				+ "\" is already associated with " + ID.class.getSimpleName()
				+ " \"" + oldID + "\". The rejected "
				+ ID.class.getSimpleName() + "\" is " + newID + "\".");

		this.fingerprint = fingerprint;
		this.oldID = oldID;
		this.newID = newID;
	}

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public ID getOldID() {
		return oldID;
	}

	public ID getNewID() {
		return newID;
	}
}
