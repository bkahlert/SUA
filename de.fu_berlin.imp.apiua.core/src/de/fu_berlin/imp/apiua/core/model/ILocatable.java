package de.fu_berlin.imp.apiua.core.model;

import java.io.Serializable;

import de.fu_berlin.imp.apiua.core.model.URI;

/**
 * Instances that implement this interface can provide a unique {@link URI}.
 * 
 * @author bkahlert
 * 
 */
public interface ILocatable extends Serializable {

	/**
	 * Returns the {@link URI} that identifies this {@link ILocatable}.
	 * 
	 * @return
	 */
	public URI getUri();

}
