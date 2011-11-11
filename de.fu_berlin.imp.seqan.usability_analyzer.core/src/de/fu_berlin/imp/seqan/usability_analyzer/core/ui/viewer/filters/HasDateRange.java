package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;

/**
 * Objects that implement this interface can check whether they are within a
 * given {@link DateRange}.
 * 
 * @author bkahlert
 * 
 */
public interface HasDateRange {
	public DateRange getDateRange();
}
