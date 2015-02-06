package de.fu_berlin.imp.apiua.core.model;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class URI implements Serializable {

	private static final long serialVersionUID = 8337713802506985728L;
	private final java.net.URI uri;
	private List<String> segments = null;

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

	/**
	 * Returns the {@link URI}'s host and path portions.
	 * <p>
	 * e.g. for protocol://host/a/b/c#d {host, a, b, c} is returned.
	 *
	 * @return
	 */
	public List<String> getSegments() {
		if (this.segments == null) {
			String host = this.uri.getHost();
			String path = this.uri.getRawPath();

			this.segments = new ArrayList<String>();
			this.segments.add(host != null ? host : "");
			List<String> pathSegments = path != null ? Arrays.asList(path
					.split("/")) : new ArrayList<String>();
			for (int i = 1, m = pathSegments.size(); i < m; i++) {
				this.segments.add(pathSegments.get(i));
			}
		}
		return this.segments;
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
		if (!(obj instanceof URI)) {
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
