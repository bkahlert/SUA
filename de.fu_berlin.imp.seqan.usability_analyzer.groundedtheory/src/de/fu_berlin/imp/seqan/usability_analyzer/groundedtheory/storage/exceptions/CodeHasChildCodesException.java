package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

public class CodeHasChildCodesException extends Exception {

	private static final long serialVersionUID = -3526378257935448355L;

	public CodeHasChildCodesException() {
		super();
	}

	public CodeHasChildCodesException(ICode code) {
		super(ICode.class.getSimpleName() + " \"" + code + "\" does not exist.");
	}

}
