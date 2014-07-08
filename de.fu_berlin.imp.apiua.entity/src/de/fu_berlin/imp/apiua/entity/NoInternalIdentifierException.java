package de.fu_berlin.imp.apiua.entity;

import de.fu_berlin.imp.apiua.entity.model.Entity;

public class NoInternalIdentifierException extends RuntimeException {

	private static final long serialVersionUID = 6920576719802855580L;

	public NoInternalIdentifierException(Entity person) {
		super();
	}

}
