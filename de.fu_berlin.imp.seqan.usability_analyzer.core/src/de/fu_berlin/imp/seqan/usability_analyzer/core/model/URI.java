package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.Serializable;
import java.net.URISyntaxException;

public class URI implements Serializable {

	private static final long serialVersionUID = 8337713802506985728L;
	private java.net.URI uri;

	public URI(String address) {
		try {
			this.uri = new java.net.URI(address);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public URI(java.net.URI uri) {
		this.uri = uri;
	}

	public java.net.URI getRawURI() {
		return this.uri;
	}

	public String getScheme() {
		return this.uri.getScheme();
	}

	public String getHost() {
		return this.uri.getHost();
	}

	public String getRawPath() {
		return this.uri.getRawPath();
	}

	public String getFragment() {
		return this.uri.getFragment();
	}

	@Override
	public String toString() {
		return this.uri.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.uri == null) ? 0 : this.uri.hashCode());
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
		URI other = (URI) obj;
		if (this.uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!this.uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
