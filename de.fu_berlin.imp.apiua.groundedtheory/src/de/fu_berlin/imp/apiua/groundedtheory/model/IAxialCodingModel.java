package de.fu_berlin.imp.apiua.groundedtheory.model;

import java.util.List;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;

/**
 * Instances of this describe a graph describing an axial coding model.
 *
 * @author bkahlert
 *
 */
public interface IAxialCodingModel extends ILocatable {

	public String getTitle();

	public List<URI> getCodes();

	public List<IRelation> getRelations();

	public String serialize();

}
