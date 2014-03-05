package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class Episodes implements IEpisodes {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Episodes.class);
	private static final long serialVersionUID = 1L;

	private URI uri;
	private IIdentifier identifier = null;

	public Episodes(IIdentifier identifier) {
		assert identifier != null;
		this.identifier = identifier;
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			try {
				this.uri = new URI("sua://episode/" + this.identifier);
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + Episodes.class, e);
			}
		}
		return this.uri;
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.identifier == null) ? 0 : this.identifier.hashCode());
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
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Episodes other = (Episodes) obj;
		if (this.identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!this.identifier.equals(other.identifier)) {
			return false;
		}
		return true;
	}

}
