package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public interface IEpisode extends ICodeable {

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
	public TimeZoneDateRange getRange();

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
