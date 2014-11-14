package de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions;


public class CodeStoreIntegrityProtectionException extends
		CodeStoreWriteException {

	private static final long serialVersionUID = 1L;

	public CodeStoreIntegrityProtectionException(String message) {
		super(message);
	}
}
