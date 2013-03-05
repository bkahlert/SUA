package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.net.URI;

/**
 * Instances that implement this interface can provide a unique {@link URI}.
 * 
 * @author bkahlert
 * 
 */
public interface ILocatable {

	public URI getUri();

}
