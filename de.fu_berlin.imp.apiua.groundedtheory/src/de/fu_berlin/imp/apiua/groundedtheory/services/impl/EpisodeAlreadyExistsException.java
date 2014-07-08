package de.fu_berlin.imp.apiua.groundedtheory.services.impl;

import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;

public class EpisodeAlreadyExistsException extends CodeServiceException {

	private static final long serialVersionUID = 1L;

	public EpisodeAlreadyExistsException(IEpisode episode) {
		super(episode + " already exists");
	}

}
