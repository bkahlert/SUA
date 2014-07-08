package de.fu_berlin.imp.apiua.groundedtheory.services.impl;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;

public class EpisodeDoesNotExistException extends CodeServiceException {

	private static final long serialVersionUID = 1L;

	public EpisodeDoesNotExistException(IEpisode episode) {
		super(episode + " does not exist");
	}

	public EpisodeDoesNotExistException(Collection<IEpisode> episodes) {
		super("The following " + IEpisode.class.getSimpleName()
				+ " do not exist:\n" + StringUtils.join(episodes, "\n"));
	}

}
