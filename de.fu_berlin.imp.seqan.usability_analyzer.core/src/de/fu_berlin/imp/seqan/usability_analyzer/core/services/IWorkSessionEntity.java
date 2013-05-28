package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.io.Serializable;
import java.net.URI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

/**
 * Instances of this class can be part of an {@link IWorkSession}.
 * 
 * @author bkahlert
 * 
 */
public interface IWorkSessionEntity extends Serializable, ILocatable {
	/**
	 * Returns the {@link URI} that identifies the entity in focus.
	 * 
	 * @return
	 */
	@Override
	public URI getUri();
}
