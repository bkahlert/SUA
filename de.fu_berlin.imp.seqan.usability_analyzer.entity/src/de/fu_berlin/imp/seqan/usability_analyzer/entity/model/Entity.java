package de.fu_berlin.imp.seqan.usability_analyzer.entity.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.NoInternalIdentifierException;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class Entity implements HasDateRange, ICodeable {

	private static final Logger LOGGER = Logger.getLogger(Entity.class);

	private static final long serialVersionUID = 1969965491590110977L;

	private ID id;
	private Fingerprint fingerprint;
	private SurveyRecord surveyRecord;

	private StatsFile statsFile;
	private CMakeCacheFile cMakeCacheFile;

	private de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper mapper;

	/* cached fields for performance reasons */
	private TimeZoneDate earliestEntryDate;
	private TimeZoneDate latestEntryDate;

	public Entity(Mapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI("sua://entity/" + this.getInternalId());
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for a "
							+ DiffFile.class.getSimpleName(), e);
		}
		return null;
	}

	private String getInternalId() throws NoInternalIdentifierException {
		ID id = this.getId();
		if (id != null)
			return id.toString();

		Token token = this.getToken();
		if (token != null)
			return token.toString();

		List<Fingerprint> fingerprints = this.getFingerprints();
		if (fingerprints != null && fingerprints.size() > 0)
			return StringUtils.join(fingerprints, "");

		throw new NoInternalIdentifierException(this);
	}

	public boolean isValid() {
		try {
			this.getInternalId();
		} catch (NoInternalIdentifierException e) {
			return false;
		}
		return true;
	}

	public void setId(ID id) {
		if (this.id != null && this.id.equals(id))
			return;

		DiffFileDirectory diffFileDirectory = Activator.getDefault()
				.getDiffFileDirectory();
		TimeZoneDateRange diffFilesDateRange = diffFileDirectory
				.getDateRange(id);
		if (diffFilesDateRange != null) {
			updateEarliestEntryDate(diffFilesDateRange.getStartDate());
			updateLatestEntryDate(diffFilesDateRange.getEndDate());
		}

		DoclogDirectory doclogFileDirectory = de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator
				.getDefault().getDoclogDirectory();
		TimeZoneDateRange doclogFileDateRange = doclogFileDirectory
				.getDateRange(id);
		if (doclogFileDateRange != null) {
			updateEarliestEntryDate(doclogFileDateRange.getStartDate());
			updateLatestEntryDate(doclogFileDateRange.getStartDate());
		}

		this.id = id;
	}

	public ID getId() {
		return this.id;
	}

	public void setFingerprint(Fingerprint fingerprint) {
		this.fingerprint = fingerprint;

		DoclogDirectory doclogFileDirectory = de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator
				.getDefault().getDoclogDirectory();
		TimeZoneDateRange doclogFileDateRange = doclogFileDirectory
				.getDateRange(fingerprint);
		updateEarliestEntryDate(doclogFileDateRange.getStartDate());
		updateLatestEntryDate(doclogFileDateRange.getStartDate());
	}

	public List<Fingerprint> getFingerprints() {
		List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
		ID id = this.getId();
		if (id != null) {
			fingerprints.addAll(this.mapper.getFingerprints(id));
		} else {
			fingerprints.add(fingerprint);
		}
		return fingerprints;
	}

	public Token getToken() {
		if (this.surveyRecord != null)
			return this.surveyRecord.getToken();
		else
			return null;
	}

	public SurveyRecord getSurveyRecord() {
		return surveyRecord;
	}

	public void setSurveyRecord(SurveyRecord surveyRecord) {
		if (surveyRecord != null) {
			updateEarliestEntryDate(surveyRecord.getDate());
			updateLatestEntryDate(surveyRecord.getDate());
		}
		this.surveyRecord = surveyRecord;
	}

	public StatsFile getStatsFile() {
		return statsFile;
	}

	public void setStatsFile(StatsFile statsFile) {
		this.statsFile = statsFile;
	}

	public CMakeCacheFile getCMakeCacheFile() {
		return cMakeCacheFile;
	}

	public void setCMakeCacheFile(CMakeCacheFile cMakeCacheFile) {
		this.cMakeCacheFile = cMakeCacheFile;
	}

	private void updateEarliestEntryDate(TimeZoneDate date) {
		if (date == null)
			return;
		if (this.earliestEntryDate == null)
			this.earliestEntryDate = date;
		else if (this.earliestEntryDate.getTime() > date.getTime())
			this.earliestEntryDate = date;
	}

	private void updateLatestEntryDate(TimeZoneDate date) {
		if (date == null)
			return;
		if (this.latestEntryDate == null)
			this.latestEntryDate = date;
		else if (this.latestEntryDate.getTime() < date.getTime())
			this.latestEntryDate = date;
	}

	public TimeZoneDate getEarliestEntryDate() {
		return this.earliestEntryDate;
	}

	public TimeZoneDate getLatestEntryDate() {
		return this.latestEntryDate;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return new TimeZoneDateRange(this.earliestEntryDate,
				this.latestEntryDate);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapper == null) ? 0 : mapper.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (this.getInternalId() == null) {
			if (other.getInternalId() != null)
				return false;
		} else if (!this.getInternalId().equals(other.getInternalId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Person: " + this.getInternalId();
	}

}
