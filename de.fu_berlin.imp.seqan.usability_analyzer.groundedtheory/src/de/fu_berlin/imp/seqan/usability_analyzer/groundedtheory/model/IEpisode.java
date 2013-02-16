package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public interface IEpisode extends ICodeable, HasDateRange, Comparable<IEpisode> {

	/**
	 * Returns the {@link ID} this {@link IEpisode} belongs to.
	 * 
	 * @return false if this {@link IEpisode} belongs to a {@link Fingerprint}
	 */
	public ID getId();

	/**
	 * Returns the {@link Fingerprint} this {@link IEpisode} belongs to.
	 * 
	 * @return false if this {@link IEpisode} belongs to a {@link ID}
	 */
	public Fingerprint getFingerprint();

	/**
	 * Returns the {@link ID} or {@link Fingerprint} this {@link IEpisode}
	 * belongs to.
	 * 
	 * @return always an object != null and of either type {@link ID} or
	 *         {@link Fingerprint}
	 */
	public Object getKey();

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

	/**
	 * Return the color of this {@link IEpisode}.
	 * 
	 * @return
	 */
	public RGB getColor();

	/**
	 * Returns a copy of the {@link IEpisode} with the new color. This instance
	 * (not the returned one) stays untouched.
	 * 
	 * @param episode
	 * @return
	 */
	public IEpisode changeColor(RGB rgb);

}
