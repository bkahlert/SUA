package de.fu_berlin.imp.seqan.usability_analyzer.person.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.IRangeable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.person.NoInternalIdentifierException;
import de.fu_berlin.imp.seqan.usability_analyzer.person.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

/*
 * TODO remove earliest/latest date if setXXX removes the data the date is based on
 */
public class Person implements IRangeable {
	private DiffFileList diffFiles;
	private DoclogFile doclogFile;
	private SurveyRecord surveyRecord;

	private StatsFile statsFile;
	private CMakeCacheFile cMakeCacheFile;

	private de.fu_berlin.imp.seqan.usability_analyzer.person.mapping.Mapper mapper;

	/* cached fields for performance reasons */
	private Date earliestEntryDate;
	private Date latestEntryDate;

	public Person(Mapper mapper) {
		this.mapper = mapper;
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

	public ID getId() {
		if (this.diffFiles != null && this.diffFiles.size() > 0)
			return this.diffFiles.get(0).getId();
		else
			return null;
	}

	public List<Fingerprint> getFingerprints() {
		List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
		ID id = this.getId();
		if (id != null) {
			fingerprints.addAll(this.mapper.getFingerprints(id));
		} else {
			if (this.doclogFile != null) {
				Fingerprint fingerprint = this.doclogFile.getFingerprint();
				fingerprints.add(fingerprint);
			}
		}
		return fingerprints;
	}

	public Token getToken() {
		if (this.surveyRecord != null)
			return this.surveyRecord.getToken();
		else
			return null;
	}

	public DiffFileList getDiffFiles() {
		return this.diffFiles;
	}

	public void setDiffFiles(DiffFileList diffFiles) {
		if (diffFiles != null) {
			for (DiffFile diffFile : diffFiles) {
				updateEarliestEntryDate(diffFile.getDate());
				updateLatestEntryDate(diffFile.getDate());
			}
		}
		this.diffFiles = diffFiles;
	}

	public DoclogFile getDoclogFile() {
		return doclogFile;
	}

	public void setDoclogFile(DoclogFile doclogFile) {
		if (doclogFile != null && doclogFile.getDoclogRecords() != null) {
			for (DoclogRecord doclogRecord : doclogFile.getDoclogRecords()) {
				updateEarliestEntryDate(doclogRecord.getDate());
				updateLatestEntryDate(doclogRecord.getDate());
			}
		}
		this.doclogFile = doclogFile;
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

	private void updateEarliestEntryDate(Date date) {
		if (date == null)
			return;
		if (this.earliestEntryDate == null)
			this.earliestEntryDate = date;
		else if (this.earliestEntryDate.getTime() > date.getTime())
			this.earliestEntryDate = date;
	}

	private void updateLatestEntryDate(Date date) {
		if (date == null)
			return;
		if (this.latestEntryDate == null)
			this.latestEntryDate = date;
		else if (this.latestEntryDate.getTime() < date.getTime())
			this.latestEntryDate = date;
	}

	public Date getEarliestEntryDate() {
		return this.earliestEntryDate;
	}

	public Date getLatestEntryDate() {
		return this.latestEntryDate;
	}

	public boolean isInRange(DateRange dateRange) {
		if (this.earliestEntryDate == null || this.latestEntryDate == null)
			return false;
		if (dateRange.isInRange(this.earliestEntryDate)
				|| dateRange.isInRange(this.latestEntryDate))
			return true;
		if (dateRange.isBeforeRange(this.earliestEntryDate)
				&& dateRange.isAfterRange(this.latestEntryDate))
			return true;
		return false;
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
		Person other = (Person) obj;
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
