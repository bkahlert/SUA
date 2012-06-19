package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;

public class CodeStoreIntegrityProtectionException extends
		CodeStoreWriteException {

	private static final long serialVersionUID = 1L;

	public CodeStoreIntegrityProtectionException(String message) {
		super(message);
	}
}
