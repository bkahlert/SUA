package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.Map;

import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.utils.ExecUtils;

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
	 * {@link ExecUtils#asyncExec(Runnable)} or something similar to interact
	 * with the UI.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param ranges
	 * @param moveInsideViewport
	 * @see IHighlightService#highlight(TimeZoneDateRange[])
	 */
	public void highlight(Object sender, CalendarRange[] ranges,
			boolean moveInsideViewport);

	/**
	 * This method is called if grouped {@link TimeZoneDateRange}s are requested
	 * to be highlighted.
	 * <p>
	 * This method is called from a non-UI thread. You will have to use
	 * {@link ExecUtils#asyncExec(Runnable)} or something similar to interact
	 * with the UI.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param dataContainers
	 * @param moveInsideViewport
	 * @see IHighlightService#highlight(Map)
	 */
	public void highlight(Object sender,
			Map<IIdentifier, CalendarRange[]> groupedRanges,
			boolean moveInsideViewport);

}
