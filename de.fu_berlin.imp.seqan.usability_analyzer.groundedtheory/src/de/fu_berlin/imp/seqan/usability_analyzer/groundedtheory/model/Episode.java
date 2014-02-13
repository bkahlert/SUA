package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.net.URI;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class Episode implements IEpisode {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Episode.class);
	private static final long serialVersionUID = 1L;

	private URI uri;
	private IIdentifier identifier = null;
	private TimeZoneDateRange range;
	private String caption;
	private final TimeZoneDate creation;

	public Episode(IIdentifier identifier, TimeZoneDate start,
			TimeZoneDate end, String name) {
		assert identifier != null;
		this.identifier = identifier;
		this.range = new TimeZoneDateRange(start, end);
		this.caption = name;
		this.creation = new TimeZoneDate();
	}

	public Episode(IIdentifier identifier, TimeZoneDateRange range,
			String caption) {
		assert identifier != null;
		this.identifier = identifier;
		this.range = range;
		this.caption = caption;
		this.creation = new TimeZoneDate();
	}

	private Episode(IEpisode episode) {
		this.identifier = episode.getIdentifier();
		this.range = episode.getDateRange();
		this.caption = episode.getCaption();
		this.creation = episode.getCreation();
	}

	private Episode(IEpisode episode, String caption) {
		this(episode);
		this.caption = caption;
	}

	private Episode(IEpisode episode, TimeZoneDateRange range) {
		this(episode);
		this.range = range;
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			StringBuilder sb = new StringBuilder("sua://episode/"
					+ this.identifier);

			sb.append("/");

			sb.append(this.getCreation().toISO8601());
			try {
				this.uri = new URI(sb.toString());
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + Episode.class, e);
			}
		}
		return this.uri;
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.identifier;
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
	public TimeZoneDateRange getDateRange() {
		return this.range;
	}

	@Override
	public IEpisode changeRange(TimeZoneDateRange range) {
		return new Episode(this, range);
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
		sb.append(this.getIdentifier());
		sb.append(": ");
		if (this.getStart() != null) {
			sb.append(this.getStart().toISO8601());
		} else {
			sb.append("-∞");
		}
		sb.append(" to ");
		if (this.getEnd() != null) {
			sb.append(this.getEnd().toISO8601());
		} else {
			sb.append("+∞");
		}
		return sb.toString();
	}

	@Override
	public int compareTo(IEpisode episode) {
		if (this.getDateRange().isBeforeRange(
				episode.getDateRange().getStartDate())) {
			return 1;
		} else if (episode.getDateRange().isBeforeRange(
				this.getDateRange().getStartDate())) {
			return -1;
		} else {
			return this.getIdentifier().compareTo(episode.getIdentifier());
		}
	}
}
