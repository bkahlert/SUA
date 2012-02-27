package de.fu_berlin.imp.seqan.usability_analyzer.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorsUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyRecordManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class EntityManager {
	private Logger logger = Logger.getLogger(EntityManager.class);

	private DiffFileDirectory diffFileDirectory;
	private DoclogDirectory doclogDirectory;
	private SurveyRecordManager surveyRecordManager;

	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private List<Entity> persons = new ArrayList<Entity>();

	public EntityManager(DiffFileDirectory diffFileManager,
			DoclogDirectory doclogDirectory,
			SurveyRecordManager surveyRecordManager,
			StatsFileManager statsFileManager,
			CMakeCacheFileManager cMakeCacheFileManager, Mapper mapper) {
		super();
		this.diffFileDirectory = diffFileManager;
		this.doclogDirectory = doclogDirectory;
		this.surveyRecordManager = surveyRecordManager;

		this.statsFileManager = statsFileManager;
		this.cMakeCacheFileManager = cMakeCacheFileManager;

		this.mapper = mapper;

		scan();
	}

	public void scan() {
		final ArrayList<Entity> entities = new ArrayList<Entity>();

		ExecutorService executorService = ExecutorsUtil
				.newFixedMultipleOfProcessorsThreadPool(2);
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
		// force class loading since they are used in the Callable
		Entity.class.getClass();
		NoInternalIdentifierException.class.getClass();
		Token.class.getClass();
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					/*
					 * Diff based
					 */
					List<Entity> diffBasedEntities = getEntitiesDiffBased();
					synchronized (entities) {
						entities.addAll(diffBasedEntities);
					}
				} catch (Exception e) {
					logger.fatal(e);
				}
				return null;
			}
		});
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					/*
					 * Doclog based
					 */
					checkPersonsDoclogIdBased();
				} catch (Exception e) {
					logger.fatal(e);
				}
				return null;
			}
		});
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					/*
					 * Doclog based
					 */
					List<Entity> fingerprintBasedEntities = buildEntitiesDoclogFingerprintBased();
					synchronized (entities) {
						entities.addAll(fingerprintBasedEntities);
					}
				} catch (Exception e) {
					logger.fatal(e);
				}
				return null;
			}
		});
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					/*
					 * Token based
					 */
					List<Entity> tokenBasedEntities = getEntitiesTokenBased();
					synchronized (entities) {
						entities.addAll(tokenBasedEntities);
					}
				} catch (Exception e) {
					logger.fatal(e);
				}
				return null;
			}
		});
		try {
			executorService.invokeAll(callables);
		} catch (InterruptedException e) {
			logger.fatal(
					"Error matching " + Entity.class.getSimpleName() + "s", e);
		}

		this.persons = entities;
	}

	public List<Entity> getPersons() {
		return this.persons;
	}

	private List<Entity> getEntitiesDiffBased() {
		List<Entity> persons = new ArrayList<Entity>();

		Set<ID> ids = this.diffFileDirectory.getIDs();
		for (ID id : ids) {
			Entity person = new Entity(this.mapper);
			person.setId(id);
			person.setStatsFile(this.statsFileManager.getStatsFile(id));
			person.setCMakeCacheFile(this.cMakeCacheFileManager
					.getCMakeCacheFile(id));

			for (Fingerprint fingerprint : this.mapper.getFingerprints(id)) {
				if (this.doclogDirectory.getFile(fingerprint) != null) {
					this.logDoclogRewriteError(id,
							this.doclogDirectory.getFile(fingerprint));
				}
			}

			Token token = doclogDirectory.getToken(id);
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

			if (person.isValid()) {
				persons.add(person);
			} else {
				throw new AutomatonDesignError();
			}
		}

		return persons;
	}

	private void checkPersonsDoclogIdBased() {
		List<ID> doclogIDs = this.doclogDirectory.getIDs();
		Set<ID> diffIDs = this.diffFileDirectory.getIDs();
		for (ID doclogId : doclogIDs) {
			if (!diffIDs.contains(doclogId))
				logNoDiffFilesButDoclog(doclogId);
		}
	}

	private List<Entity> buildEntitiesDoclogFingerprintBased() {
		List<Entity> persons = new ArrayList<Entity>();

		List<Fingerprint> fingerprints = this.doclogDirectory.getFingerprints();
		for (Fingerprint fingerprint : fingerprints) {
			Entity person = new Entity(this.mapper);

			if (this.mapper.getID(fingerprint) != null) {
				this.logDoclogRewriteError(this.mapper.getID(fingerprint), null);
			}

			person.setFingerprint(fingerprint);

			Token token = this.doclogDirectory.getToken(fingerprint);
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

	private List<Entity> getEntitiesTokenBased() {
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

	private void logDoclogRewriteError(ID id, File outdatedDoclogFile) {
		logger.error("Although the ID is known a fingerprint based doclog was found:\nID: "
				+ id
				+ ((outdatedDoclogFile != null) ? "\n:Fingerprint: "
						+ outdatedDoclogFile : ""));
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
