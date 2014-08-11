package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;

/**
 * A {@link IDimension} that can be associated to some concept (e.g. a
 * {@link ICode}).
 * 
 * @author bkahlert
 * 
 */
public interface IDimension {
	public boolean isLegal(String value);
}
