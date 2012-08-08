package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.net.URI;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class Episode implements IEpisode {

	private static final Logger LOGGER = Logger.getLogger(Episode.class);
	private static final long serialVersionUID = 1L;
	private ID id = null;
	private Fingerprint fingerprint = null;
	private TimeZoneDateRange range;
	private String caption;
	private TimeZoneDate creation;

	public Episode(ID id, TimeZoneDate start, TimeZoneDate end, String name) {
		assert id != null;
		this.id = id;
		this.range = new TimeZoneDateRange(start, end);
		this.caption = name;
		this.creation = new TimeZoneDate();
	}

	public Episode(ID id, TimeZoneDateRange range, String caption) {
		assert id != null;
		this.id = id;
		this.range = range;
		this.caption = caption;
		this.creation = new TimeZoneDate();
	}

	public Episode(Fingerprint fingerprint, TimeZoneDate start,
			TimeZoneDate end, String caption) {
		assert fingerprint != null;
		this.fingerprint = fingerprint;
		this.range = new TimeZoneDateRange(start, end);
		this.caption = caption;
		this.creation = new TimeZoneDate();
	}

	public Episode(Fingerprint fingerprint, TimeZoneDateRange range,
			String caption) {
		assert fingerprint != null;
		this.fingerprint = fingerprint;
		this.range = range;
		this.caption = caption;
		this.creation = new TimeZoneDate();
	}

	private Episode(IEpisode episode, String caption) {
		this.id = episode.getId();
		this.fingerprint = episode.getFingerprint();
		this.range = episode.getRange();
		this.creation = episode.getCreation();
		this.caption = caption;
	}

	@Override
	public URI getCodeInstanceID() {
		StringBuilder sb = new StringBuilder("sua://episode/"
				+ ((id != null) ? id : fingerprint));

		sb.append("/");
		if (this.getStart() != null)
			sb.append(this.getStart().toISO8601());

		sb.append("/");
		if (this.getEnd() != null)
			sb.append(this.getEnd());

		try {
			return new URI(sb.toString());
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for an "
							+ Episode.class.getSimpleName(), e);
		}
		return null;
	}

	@Override
	public ID getId() {
		return this.id;
	}

	@Override
	public Fingerprint getFingerprint() {
		return this.fingerprint;
	}

	@Override
	public Object getKey() {
		return this.id != null ? this.id : this.fingerprint;
	}

	@Override
	public TimeZoneDate getStart() {
		return this.range.getStartDate();
	}

	@Override
	public TimeZoneDate getEnd() {
		return this.range.getEndDate();
	}

	@Override
	public TimeZoneDateRange getRange() {
		return this.range;
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.creation;
	}

	@Override
	public String getCaption() {
		return this.caption;
	}

	@Override
	public IEpisode changeCaption(String caption) {
		return new Episode(this, caption);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Episode.class.getSimpleName());
		sb.append(" in ");
		sb.append(this.getKey());
		sb.append(": ");
		if (getEnd() != null)
			sb.append(getEnd().toISO8601());
		else
			sb.append("undefined");
		sb.append(" to ");
		if (getEnd() != null)
			sb.append(getEnd().toISO8601());
		else
			sb.append("undefined");
		return sb.toString();
	}

}
