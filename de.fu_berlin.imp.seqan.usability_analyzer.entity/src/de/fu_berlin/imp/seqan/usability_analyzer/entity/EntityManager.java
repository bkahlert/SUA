package de.fu_berlin.imp.seqan.usability_analyzer.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;

public class EntityManager {
	private static Logger LOGGER = Logger.getLogger(EntityManager.class);

	private DiffContainer diffContainer;
	private DoclogDataContainer doclogDataContainer;
	private SurveyContainer surveyContainer;

	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private List<Entity> persons = new ArrayList<Entity>();

	public EntityManager(DiffContainer diffContainer,
			DoclogDataContainer doclogDataContainer,
			SurveyContainer surveyContainer, StatsFileManager statsFileManager,
			CMakeCacheFileManager cMakeCacheFileManager, Mapper mapper) {
		super();
		this.diffContainer = diffContainer;
		this.doclogDataContainer = doclogDataContainer;
		this.surveyContainer = surveyContainer;

		this.statsFileManager = statsFileManager;
		this.cMakeCacheFileManager = cMakeCacheFileManager;

		this.mapper = mapper;
	}

	public void scan(IProgressMonitor monitor) {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		final ArrayList<Entity> entities = new ArrayList<Entity>();

		ExecutorService executorService = ExecutorUtil
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
					List<Entity> diffBasedEntities = EntityManager.this
							.getEntitiesDiffBased();
					synchronized (entities) {
						entities.addAll(diffBasedEntities);
					}
				} catch (Exception e) {
					EntityManager.LOGGER.fatal(e);
				}
				subMonitor.worked(1);
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
					EntityManager.this.checkPersonsDoclogIdBased();
				} catch (Exception e) {
					EntityManager.LOGGER.fatal(e);
				}
				subMonitor.worked(1);
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
					List<Entity> fingerprintBasedEntities = EntityManager.this
							.buildEntitiesDoclogFingerprintBased();
					synchronized (entities) {
						entities.addAll(fingerprintBasedEntities);
					}
				} catch (Exception e) {
					EntityManager.LOGGER.fatal(e);
				}
				subMonitor.worked(1);
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
					List<Entity> tokenBasedEntities = EntityManager.this
							.getEntitiesTokenBased();
					synchronized (entities) {
						entities.addAll(tokenBasedEntities);
					}
				} catch (Exception e) {
					EntityManager.LOGGER.fatal(e);
				}
				subMonitor.worked(1);
				return null;
			}
		});
		try {
			executorService.invokeAll(callables);
		} catch (InterruptedException e) {
			LOGGER.fatal(
					"Error matching " + Entity.class.getSimpleName() + "s", e);
		}

		this.persons = entities;
	}

	public List<Entity> getPersons() {
		return this.persons;
	}

	private List<Entity> getEntitiesDiffBased() {
		List<Entity> entities = new ArrayList<Entity>();

		Set<ID> ids = this.diffContainer.getIDs();
		for (ID id : ids) {
			Entity entity = new Entity(this.mapper);
			entity.setId(id);
			entity.setStatsFile(this.statsFileManager.getStatsFile(id));
			entity.setCMakeCacheFile(this.cMakeCacheFileManager
					.getCMakeCacheFile(id));

			for (Fingerprint fingerprint : this.mapper.getFingerprints(id)) {
				if (this.doclogDataContainer.getFile(fingerprint) != null) {
					this.logDoclogRewriteError(id,
							this.doclogDataContainer.getFile(fingerprint));
				}
			}

			Token token = this.doclogDataContainer.getToken(id);
			if (token != null) {
				SurveyRecord surveyRecord = this.surveyContainer
						.getSurveyRecord(token);
				entity.setSurveyRecord(surveyRecord);

				if (this.mapper.getFingerprints(token).size() > 0) {
					this.logIdBasedSurveyHasFingerprints();
				}
				if (this.mapper.getIDs(token).size() != 1
						|| !this.mapper.getIDs(token).get(0).equals(id)) {
					this.logIdTokenNotBijective(id, token,
							this.mapper.getIDs(token).get(0));
				}
			}

			if (entity.getId() != null && entity.getSurveyRecord() == null) {
				SurveyRecord surveyRecord = this.surveyContainer
						.getSurveyRecord(entity.getId());
				entity.setSurveyRecord(surveyRecord);
			}

			if (entity.isValid()) {
				entities.add(entity);
			} else {
				throw new AutomatonDesignError();
			}
		}

		return entities;
	}

	private void checkPersonsDoclogIdBased() {
		List<ID> doclogIDs = new ArrayList<ID>();
		IIdentifier[] doclogIdentifiers = this.doclogDataContainer
				.getIdentifiers();
		for (IIdentifier identifier : doclogIdentifiers) {
			if (identifier instanceof ID) {
				doclogIDs.add((ID) identifier);
			}
		}
		Set<ID> diffIDs = this.diffContainer.getIDs();
		for (ID doclogID : doclogIDs) {
			if (!diffIDs.contains(doclogID)) {
				this.logNoDiffFilesButDoclog(doclogID);
			}
		}
	}

	private List<Entity> buildEntitiesDoclogFingerprintBased() {
		List<Entity> entities = new ArrayList<Entity>();

		List<Fingerprint> doclogFingerprints = new ArrayList<Fingerprint>();
		IIdentifier[] doclogIdentifiers = this.doclogDataContainer
				.getIdentifiers();
		for (IIdentifier identifier : doclogIdentifiers) {
			if (identifier instanceof Fingerprint) {
				doclogFingerprints.add((Fingerprint) identifier);
			}
		}
		for (Fingerprint doclogFingerprint : doclogFingerprints) {
			Entity entity = new Entity(this.mapper);

			if (this.mapper.getID(doclogFingerprint) != null) {
				this.logDoclogRewriteError(
						this.mapper.getID(doclogFingerprint), null);
			}

			entity.setFingerprint(doclogFingerprint);

			Token token = this.doclogDataContainer.getToken(doclogFingerprint);
			if (token != null) {
				entity.setSurveyRecord(this.surveyContainer
						.getSurveyRecord(token));

				List<Fingerprint> revFingerprints = this.mapper
						.getFingerprints(token);
				if (revFingerprints.size() > 1) {
					this.logDifferentFingerprintsForSurvey(this.mapper
							.getFingerprints(token));
				}
				if (revFingerprints.size() == 0) {
					this.logFingerprintTokenNotBijective(doclogFingerprint,
							token, null);
				}
				if (revFingerprints.size() == 1
						&& !revFingerprints.get(0).equals(doclogFingerprint)) {
					this.logFingerprintTokenNotBijective(doclogFingerprint,
							token, revFingerprints.get(0));
				}
			}

			if (entity.getId() != null && entity.getSurveyRecord() == null) {
				SurveyRecord surveyRecord = this.surveyContainer
						.getSurveyRecord(entity.getId());
				entity.setSurveyRecord(surveyRecord);
			}

			if (entity.isValid()) {
				entities.add(entity);
			} else {
				throw new AutomatonDesignError();
			}
		}

		return entities;
	}

	private List<Entity> getEntitiesTokenBased() {
		List<Entity> entities = new ArrayList<Entity>();

		List<Token> tokens = this.surveyContainer.getTokens();
		for (Token token : tokens) {
			Entity entity = new Entity(this.mapper);
			SurveyRecord surveyRecord = this.surveyContainer
					.getSurveyRecord(token);
			entity.setSurveyRecord(surveyRecord);

			List<Fingerprint> fingerprints = this.mapper.getFingerprints(token);
			if (fingerprints.size() > 1) {
				this.logDifferentFingerprintsForSurvey(fingerprints);
			}
			if (fingerprints.size() == 0) {
				if (this.mapper.getIDs(token).size() == 0) {
					this.logNoFingerprintButToken(token);
				} else {
					// handled by id based
				}
			}
			if (fingerprints.size() == 1) {
				// handled by fingerprint based

				if (this.mapper.getIDs(token).size() != 0) {
					this.logIdBasedSurveyHasFingerprints();
				}
			}
		}

		return entities;
	}

	// TODO: survey nach IDs durchsuchen und damit beginnen

	private void logDoclogRewriteError(IIdentifier id, IData data) {
		LOGGER.error("Although the ID is known a fingerprint based doclog was found:\nID: "
				+ id + ((data != null) ? "\n:Fingerprint: " + data : ""));
	}

	private void logIdBasedSurveyHasFingerprints() {
		LOGGER.error("Although the survey record could be mapped via an ID "
				+ "based doclog other fingerprint based doclogs could be found");
	}

	private void logIdTokenNotBijective(IIdentifier id, Token token,
			IIdentifier id2) {
		LOGGER.error("ID-token-mapping not bijective:\nID " + id + " -> "
				+ token + " -> " + id2);
	}

	private void logFingerprintTokenNotBijective(IIdentifier fingerprint,
			Token token, IIdentifier fingerprint2) {
		LOGGER.error("ID-token-mapping not bijective:\nID " + fingerprint
				+ " -> " + token + " -> " + fingerprint2);
	}

	private void logDifferentFingerprintsForSurvey(
			List<Fingerprint> fingerprints) {
		LOGGER.error("Different fingerprints accessed the same survey\nFingerprints: "
				+ StringUtils.join(fingerprints, ", "));
	}

	private void logNoDiffFilesButDoclog(IIdentifier id) {
		LOGGER.error("A user never uploaded diff files although he accessed the online documentation with the successfully generated ID:\nID: "
				+ id);
	}

	private void logNoFingerprintButToken(Token token) {
		LOGGER.error("Although a token exists no corresponding fingerprint could be found\nToken: "
				+ token + "\nVery probably the user has deactivated JavaScript");
	}
}
