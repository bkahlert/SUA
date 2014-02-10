package de.fu_berlin.imp.seqan.usability_analyzer.uri.services;

import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.IUri;

public class UriDoesNotExistException extends UriServiceException {

	private static final long serialVersionUID = 1L;

	public UriDoesNotExistException(IUri uri) {
		super("The " + IUri.class.getSimpleName() + " " + uri.toString()
				+ " does not exist");
	}

}
