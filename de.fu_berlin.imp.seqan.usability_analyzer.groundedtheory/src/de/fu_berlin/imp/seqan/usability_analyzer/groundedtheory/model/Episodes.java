package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.net.URI;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class Episodes implements IEpisodes {
	private static final Logger LOGGER = Logger.getLogger(Episodes.class);
	private static final long serialVersionUID = 1L;
	private IIdentifier identifier = null;

	public Episodes(IIdentifier identifier) {
		assert identifier != null;
		this.identifier = identifier;
	}

	@Override
	public URI getUri() {
		try {
			return new URI("sua://episode/" + this.identifier);
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for an "
							+ Episodes.class.getSimpleName(), e);
		}
		return null;
	}

	@Override
	public IIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Episodes other = (Episodes) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		return true;
	}

}
