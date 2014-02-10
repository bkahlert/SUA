package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;

import com.bkahlert.nebula.datetime.CalendarRange;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

/**
 * This service mediates highlight request where
 * {@link IHighlightServiceListener} are requested to highlight the given data
 * in their views.
 * 
 * @author bkahlert
 * 
 */
public interface IHighlightService {

	public void addHighlightServiceListener(
			IHighlightServiceListener highlightServiceListener);

	public void removeHighlightServiceListener(
			IHighlightServiceListener highlightServiceListener);

	/**
	 * Highlights the given {@link TimeZoneDateRange}.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param range
	 * @param moveInsideViewport
	 */
	public void highlight(Object sender, CalendarRange range,
			boolean moveInsideViewport);

	/**
	 * Highlights the given {@link TimeZoneDateRange}s.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param ranges
	 * @param moveInsideViewport
	 */
	public void highlight(Object sender, CalendarRange[] ranges,
			boolean moveInsideViewport);

	/**
	 * Highlights the {@link TimeZoneDateRange}s that belong to a specific
	 * {@link IIdentifier}.
	 * <p>
	 * e.g. if a {@link IHighlightServiceListener}'s view is displaying the data
	 * associated to ID "A" and ID "B" only the {@link TimeZoneDateRange} that
	 * belong to "A" are used to highlight data belonging to "A"; same with "B".
	 * <p>
	 * {@link IIdentifier}s a {@link IHighlightServiceListener} is not currently
	 * displaying are ignored.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param groupedRanges
	 * @param moveInsideViewport
	 */
	public void highlight(Object sender,
			Map<IIdentifier, CalendarRange[]> groupedRanges,
			boolean moveInsideViewport);

	/**
	 * Highlights the {@link TimeZoneDateRange}s that belong to a specific
	 * {@link IIdentifier} found in the given {@link ISelection}.
	 * <p>
	 * e.g. if a {@link IHighlightServiceListener}'s view is displaying the data
	 * associated to ID "A" and ID "B" only the {@link TimeZoneDateRange} that
	 * belong to "A" are used to highlight data belonging to "A"; same with "B".
	 * <p>
	 * {@link IIdentifier}s a {@link IHighlightServiceListener} is not currently
	 * displaying are ignored.
	 * 
	 * @param sender
	 *            that requested the highlight
	 * @param groupedRanges
	 * @param moveInsideViewport
	 */
	public void highlight(Object sender, ISelection selection,
			boolean moveInsideViewport);

}
