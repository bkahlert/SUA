package de.fu_berlin.imp.apiua.doclog.mapping;

import de.fu_berlin.imp.apiua.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.apiua.core.model.identifier.ID;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;

public class FinterprintAlreadyMappedException extends Exception {

	private static final long serialVersionUID = -3973751907072660227L;
	private IIdentifier fingerprint;
	private IIdentifier oldID;
	private IIdentifier newID;

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

	public IIdentifier getFingerprint() {
		return this.fingerprint;
	}

	public IIdentifier getOldID() {
		return this.oldID;
	}

	public IIdentifier getNewID() {
		return this.newID;
	}
}
