package de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;

public class DuplicateRelationException extends Exception {
	private static final long serialVersionUID = 6180288983303262230L;

	public DuplicateRelationException(List<IRelation> duplicates) {
		super("The following " + IRelation.class.getSimpleName()
				+ "s are duplicates: \n" + StringUtils.join(duplicates, "\n"));
	}
}
