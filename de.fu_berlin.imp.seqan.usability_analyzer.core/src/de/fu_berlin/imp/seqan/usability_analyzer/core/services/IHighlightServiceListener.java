package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.Map;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

/**
 * Instances of this interface listen for highlight requests.
 * 
 * @author bkahlert
 * 
 */
public interface IHighlightServiceListener {

	/**
	 * This method is called if {@link TimeZoneDateRange}s are requested to be
	 * highlighted.
	 * <p>
	 * This method is called from a non-UI thread. You will have to use
	 * {@link ExecutorUtil#asyncExec(Runnable)} or something similar to interact
	 * with the UI.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param ranges
	 * @see IHighlightService#highlight(TimeZoneDateRange[])
	 */
	public void highlight(Object sender, TimeZoneDateRange[] ranges);

	/**
	 * This method is called if grouped {@link TimeZoneDateRange}s are requested
	 * to be highlighted.
	 * <p>
	 * This method is called from a non-UI thread. You will have to use
	 * {@link ExecutorUtil#asyncExec(Runnable)} or something similar to interact
	 * with the UI.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param dataContainers
	 * @see IHighlightService#highlight(Map)
	 */
	public void highlight(Object sender,
			Map<IIdentifier, TimeZoneDateRange[]> groupedRanges);

}
