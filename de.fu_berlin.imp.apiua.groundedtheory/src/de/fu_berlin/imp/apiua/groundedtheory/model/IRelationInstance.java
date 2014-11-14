package de.fu_berlin.imp.apiua.groundedtheory.model;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;

public interface IRelationInstance extends ILocatable {

	/**
	 * Returns the {@link IRelationInstance}'s public URI.
	 */
	@Override
	public URI getUri();

	public IRelation getRelation();

	/**
	 * Returns the {@link URI} of the resource this {@link IRelationInstance}
	 * links to.
	 *
	 * @return
	 */
	public URI getPhenomenon();

	public TimeZoneDate getCreation();

}
