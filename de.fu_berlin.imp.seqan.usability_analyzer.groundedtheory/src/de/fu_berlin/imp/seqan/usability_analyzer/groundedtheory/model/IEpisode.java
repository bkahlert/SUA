package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public interface IEpisode extends ICodeable, HasIdentifier, HasDateRange,
		Comparable<IEpisode> {

	/**
	 * Returns the {@link IIdentifier} this {@link IEpisode} belongs to.
	 * 
	 * @return
	 */
	@Override
	public IIdentifier getIdentifier();

	/**
	 * Returns the {@link TimeZoneDate} this {@link IEpisode} starts.
	 * 
	 * @return
	 */
	public TimeZoneDate getStart();

	/**
	 * Returns the {@link TimeZoneDate} this {@link IEpisode} ends.
	 * 
	 * @return
	 */
	public TimeZoneDate getEnd();

	/**
	 * Returns the {@link TimeZoneDateRange} this {@link IEpisode} comprises.
	 * 
	 * @return
	 */
	@Override
	public TimeZoneDateRange getDateRange();

	/**
	 * Returns a copy of the {@link IEpisode} with the new range. This instance
	 * (not the returned one) stays untouched.
	 * 
	 * @param episode
	 * @return
	 */
	public IEpisode changeRange(TimeZoneDateRange range);

	/**
	 * Return the {@link IEpisode}'s caption.
	 * 
	 * @return
	 */
	public String getCaption();

	/**
	 * Return the {@link IEpisode}'s creation date.
	 * 
	 * @return
	 */
	public TimeZoneDate getCreation();

	/**
	 * Returns a copy of the {@link IEpisode} with the new caption. This
	 * instance (not the returned one) stays untouched.
	 * 
	 * @param episode
	 * @return
	 */
	public IEpisode changeCaption(String caption);

}
