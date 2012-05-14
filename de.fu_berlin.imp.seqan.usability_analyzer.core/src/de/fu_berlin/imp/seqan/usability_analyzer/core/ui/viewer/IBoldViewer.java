package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer;

import java.util.Collection;

/**
 * Instances of this class support making contained objects bold.
 * 
 * @author bkahlert
 * 
 */
public interface IBoldViewer {
	public void setBold(Object boldObject);

	public void setBold(Collection<?> boldObjects);
}
