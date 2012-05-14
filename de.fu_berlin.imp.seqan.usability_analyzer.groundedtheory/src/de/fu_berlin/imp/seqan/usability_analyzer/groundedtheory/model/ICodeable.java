package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.io.Serializable;
import java.net.URI;

/**
 * Instances of this class can have {@link ICode}s assigned to them.
 * 
 * @author bkahlert
 * 
 */
public interface ICodeable extends Serializable {
	public URI getCodeInstanceID();
}
