package de.fu_berlin.imp.seqan.usability_analyzer.uri.model;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

/**
 * Instances of this class wrap a {@link java.net.URI} or a {@link java.net.URL}
 * so they may be used a {@link ILocatable}s.
 * 
 * @author bkahlert
 * 
 */
public class Uri implements IUri, Serializable {
	private static final long serialVersionUID = 1L;

	private String title;
	private URI uri;

	/**
	 * Constructs a title and a {@link NO_CODES} from a {@link java.net.URI}.
	 * 
	 * @param uri
	 */
	public Uri(String title, URI uri) {
		this.title = title;
		this.uri = uri;
	}

	/**
	 * Constructs a {@link NO_CODES} from a {@link java.net.URI}.
	 * 
	 * @param uri
	 */
	public Uri(URI uri) {
		this(null, uri);
	}

	/**
	 * Constructs a title and a {@link NO_CODES} from a {@link java.net.URL}.
	 * 
	 * @param url
	 */
	public Uri(String title, java.net.URL url) {
		this.title = title;
		try {
			url.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error constructing a "
					+ Uri.class.getSimpleName(), e);
		}
	}

	/**
	 * Constructs a {@link NO_CODES} from a {@link java.net.URL}.
	 * 
	 * @param url
	 */
	public Uri(java.net.URL url) {
		this(null, url);
	}

	/**
	 * Constructs a {@link NO_CODES} from a String.
	 * <p>
	 * This constructor is only a short hand for
	 * <code>new NO_CODES(new java.net.URI(uri))</code> whereas <code>uri</code> is
	 * your String.
	 * 
	 * @param uri
	 * @throws URISyntaxException
	 */
	public Uri(String title, String uri) {
		this(title, new URI(uri));
	}

	/**
	 * Constructs a title and a {@link NO_CODES} from a String.
	 * <p>
	 * This constructor is only a short hand for
	 * <code>new NO_CODES(new java.net.URI(uri))</code> whereas <code>uri</code> is
	 * your String.
	 * 
	 * @param uri
	 * @throws URISyntaxException
	 */
	public Uri(String uri) throws URISyntaxException {
		this(null, uri);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		this.title = (String) stream.readObject();
		this.uri = new URI(stream.readObject().toString());
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public String toString() {
		return this.uri.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.title == null) ? 0 : this.title.hashCode());
		result = prime * result
				+ ((this.uri == null) ? 0 : this.uri.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Uri)) {
			return false;
		}
		Uri other = (Uri) obj;
		if (this.title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!this.title.equals(other.title)) {
			return false;
		}
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
