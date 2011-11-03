package de.fu_berlin.imp.seqan.usability_analyzer.person;

import de.fu_berlin.imp.seqan.usability_analyzer.person.model.Person;

public class NoInternalIdentifierException extends RuntimeException {

	private static final long serialVersionUID = 6920576719802855580L;

	public NoInternalIdentifierException(Person person) {
		super();
	}

}
