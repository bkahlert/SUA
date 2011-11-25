package de.fu_berlin.imp.seqan.usability_analyzer.entity;

import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class NoInternalIdentifierException extends RuntimeException {

	private static final long serialVersionUID = 6920576719802855580L;

	public NoInternalIdentifierException(Entity person) {
		super();
	}

}
