package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

public class CodeDoesNotExistException extends Exception {

	private static final long serialVersionUID = -3526378257935448355L;

	public CodeDoesNotExistException() {
		super();
	}

	public CodeDoesNotExistException(ICode code) {
		super(ICode.class.getSimpleName() + " \"" + code + "\" does not exist.");
	}

}
