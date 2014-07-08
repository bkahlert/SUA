package de.fu_berlin.imp.apiua.core.services;

import java.io.Serializable;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;

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
