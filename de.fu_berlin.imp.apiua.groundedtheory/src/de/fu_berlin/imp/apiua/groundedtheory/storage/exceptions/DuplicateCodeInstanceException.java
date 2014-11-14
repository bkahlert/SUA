package de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

public class DuplicateCodeInstanceException extends Exception {
	private static final long serialVersionUID = 6180288983403262230L;

	public DuplicateCodeInstanceException(
			List<ICodeInstance> duplicateCodeInstances) {
		super("The following " + ICodeInstance.class.getSimpleName()
				+ "s are duplicates: \n"
				+ StringUtils.join(duplicateCodeInstances, "\n"));
	}
}
