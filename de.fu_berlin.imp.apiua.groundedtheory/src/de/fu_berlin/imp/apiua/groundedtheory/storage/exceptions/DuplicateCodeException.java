package de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;

public class DuplicateCodeException extends Exception {
	private static final long serialVersionUID = 6180288983303262230L;

	public DuplicateCodeException(List<ICode> duplicateCodes) {
		super("The following " + ICode.class.getSimpleName()
				+ "s are duplicates: \n"
				+ StringUtils.join(duplicateCodes, "\n"));
	}
}
