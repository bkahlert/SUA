package de.fu_berlin.imp.apiua.core.ui.viewer;

import java.util.Collection;

/**
 * Instances of this class support making contained objects bold.
 * 
 * @author bkahlert
 * 
 */
public interface IBoldViewer<T> {
	public void setBold(Collection<T> boldObjects);
}
