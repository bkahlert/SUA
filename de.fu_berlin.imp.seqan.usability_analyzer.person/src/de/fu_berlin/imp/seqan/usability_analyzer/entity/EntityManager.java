package de.fu_berlin.imp.seqan.usability_analyzer.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.DoclogManager;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyRecordManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class EntityManager {
	private Logger logger = Logger.getLogger(EntityManager.class);

	private DiffFileManager diffFileManager;
	private DoclogManager doclogManager;
	private SurveyRecordManager surveyRecordManager;

	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private List<Entity> persons = new ArrayList<Entity>();

	public EntityManager(DiffFileManager diffFileManager,
			DoclogManager doclogManager,
			SurveyRecordManager surveyRecordManager,
			StatsFileManager statsFileManager,
			CMakeCacheFileManager cMakeCacheFileManager, Mapper mapper) {
		super();
		this.diffFileManager = diffFileManager;
		this.doclogManager = doclogManager;
		this.surveyRecordManager = surveyRecordManager;

		this.statsFileManager = statsFileManager;
		this.cMakeCacheFileManager = cMakeCacheFileManager;

		this.mapper = mapper;

		this.persons = new ArrayList<Entity>();

		/*
		 * Diff based
		 */
		List<Entity> diffBasedPersons = this.getPersonsDiffBased();
		persons.addAll(diffBasedPersons);

		/*
		 * Doclog based
		 */
		this.checkPersonsDoclogIdBased();
		persons.addAll(this.buildPersonsDoclogFingerprintBased());

		/*
		 * Token based
		 */
		List<Entity> tokenBasedPersons = this.getPersonsTokenBased();
		persons.addAll(tokenBasedPersons);
	}

	public List<Entity> getPersons() {
		return this.persons;
	}

	private List<Entity> getPersonsDiffBased() {
		List<Entity> persons = new ArrayList<Entity>();

		Set<ID> ids = this.diffFileManager.getIDs();
		for (ID id : ids) {
			Entity person = new Entity(this.mapper);
			person.setDiffFiles(this.diffFileManager.getDiffFiles(id));
			person.setStatsFile(this.statsFileManager.getStatsFile(id));
			person.setCMakeCacheFile(this.cMakeCacheFileManager
					.getCMakeCacheFile(id));

			for (Fingerprint fingerprint : this.mapper.getFingerprints(id)) {
				if (this.doclogManager.getDoclogFile(fingerprint) != null) {
					DoclogFile outdatedDoclogFile = this.doclogManager
							.getDoclogFile(fingerprint);
					this.logDoclogRewriteError(id, outdatedDoclogFile);
				}
			}

			DoclogFile doclogFile = this.doclogManager.getDoclogFile(id);
			if (doclogFile != null) {
				person.setDoclogFile(doclogFile);

				Token token = doclogFile.getToken();
				if (token != null) {
					SurveyRecord surveyRecord = this.surveyRecordManager
							.getSurveyRecord(token);
					person.setSurveyRecord(surveyRecord);

					if (this.mapper.getFingerprints(token).size() > 0)
						this.logIdBasedSurveyHasFingerprints();
					if (!this.mapper.getID(token).equals(id))
						this.logIdTokenNotBijective(id, token,
								this.mapper.getID(token));
				}
			}

			if (person.isValid()) {
				persons.add(person);
			} else {
				throw new AutomatonDesignError();
			}
		}

		return persons;
	}

	private void checkPersonsDoclogIdBased() {
		List<ID> ids = this.doclogManager.getIDs();
		for (ID id : ids) {
			List<DiffFile> diffFiles = this.diffFileManager.getDiffFiles(id);
			if (diffFiles == null || diffFiles.size() == 0) {
				logNoDiffFilesButDoclog(id);
			}
		}
	}

	private List<Entity> buildPersonsDoclogFingerprintBased() {
		List<Entity> persons = new ArrayList<Entity>();

		List<Fingerprint> fingerprints = this.doclogManager.getFingerprints();
		for (Fingerprint fingerprint : fingerprints) {
			Entity person = new Entity(this.mapper);

			if (this.mapper.getID(fingerprint) != null) {
				this.logDoclogRewriteError(this.mapper.getID(fingerprint), null);
			}

			DoclogFile doclogFile = this.doclogManager
					.getDoclogFile(fingerprint);
			person.setDoclogFile(doclogFile);

			Token token = doclogFile.getToken();
			if (token != null) {
				person.setSurveyRecord(this.surveyRecordManager
						.getSurveyRecord(token));

				List<Fingerprint> revFingerprints = this.mapper
						.getFingerprints(token);
				if (revFingerprints.size() > 1)
					this.logDifferentFingerprintsForSurvey(this.mapper
							.getFingerprints(token));
				if (revFingerprints.size() == 0)
					this.logFingerprintTokenNotBijective(fingerprint, token,
							null);
				if (revFingerprints.size() == 1
						&& !revFingerprints.get(0).equals(fingerprint))
					this.logFingerprintTokenNotBijective(fingerprint, token,
							revFingerprints.get(0));
			}

			if (person.isValid()) {
				persons.add(person);
			} else {
				throw new AutomatonDesignError();
			}
		}

		return persons;
	}

	private List<Entity> getPersonsTokenBased() {
		List<Entity> persons = new ArrayList<Entity>();

		List<Token> tokens = this.surveyRecordManager.getTokens();
		for (Token token : tokens) {
			Entity person = new Entity(this.mapper);
			SurveyRecord surveyRecord = this.surveyRecordManager
					.getSurveyRecord(token);
			person.setSurveyRecord(surveyRecord);

			List<Fingerprint> fingerprints = this.mapper.getFingerprints(token);
			if (fingerprints.size() > 1)
				this.logDifferentFingerprintsForSurvey(fingerprints);
			if (fingerprints.size() == 0) {
				if (this.mapper.getID(token) == null) {
					this.logNoFingerprintButToken(token);
				} else {
					// handled by id based
				}
			}
			if (fingerprints.size() == 1) {
				// handled by fingerprint based

				if (this.mapper.getID(token) != null) {
					this.logIdBasedSurveyHasFingerprints();
				}
			}
		}

		return persons;
	}

	private void logDoclogRewriteError(ID id, DoclogFile outdatedDoclogFile) {
		logger.error("Although the ID is known a fingerprint based doclog was found:\nID: "
				+ id
				+ ((outdatedDoclogFile != null) ? "\n:Fingerprint: "
						+ outdatedDoclogFile.getFingerprint() : ""));
	}

	private void logIdBasedSurveyHasFingerprints() {
		logger.error("Although the survey record could be mapped via an ID "
				+ "based doclog other fingerprint based doclogs could be found");
	}

	private void logIdTokenNotBijective(ID id, Token token, ID id2) {
		logger.error("ID-token-mapping not bijective:\nID " + id + " -> "
				+ token + " -> " + id2);
	}

	private void logFingerprintTokenNotBijective(Fingerprint fingerprint,
			Token token, Fingerprint fingerprint2) {
		logger.error("ID-token-mapping not bijective:\nID " + fingerprint
				+ " -> " + token + " -> " + fingerprint2);
	}

	private void logDifferentFingerprintsForSurvey(
			List<Fingerprint> fingerprints) {
		logger.error("Different fingerprints accessed the same survey\nFingerprints: "
				+ StringUtils.join(fingerprints, ", "));
	}

	private void logNoDiffFilesButDoclog(ID id) {
		logger.error("A user never uploaded diff files although he accessed the online documentation with the successfully generated ID:\nID: "
				+ id);
	}

	private void logNoFingerprintButToken(Token token) {
		logger.error("Although a token exists no corresponding fingerprint could be found\nToken: "
				+ token + "\nVery probably the user has deactivated JavaScript");
	}
}
