package de.fu_berlin.imp.seqan.usability_analyzer.entity.model;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.AggregatedBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.EntityManager;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyRecordManager;

public class EntityDataContainer extends AggregatedBaseDataContainer {

	private static final Logger LOGGER = Logger
			.getLogger(EntityDataContainer.class);

	private SurveyRecordManager surveyRecordManager;
	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private EntityManager entityManager;

	private DiffContainer diffContainer;

	private DoclogDataDirectory doclogDataDirectory;

	public EntityDataContainer(
			List<? extends IBaseDataContainer> baseDataContainers,
			DiffContainer diffContainer,
			DoclogDataDirectory doclogDataDirectory) throws EntityDataException {
		super(baseDataContainers);
		this.diffContainer = diffContainer;
		this.doclogDataDirectory = doclogDataDirectory;

		IData surveyRecordPath = diffContainer
				.getResource("_survey.csv");

		try {
			surveyRecordManager = new SurveyRecordManager(surveyRecordPath);
			statsFileManager = new StatsFileManager(baseDataContainers);
			cMakeCacheFileManager = new CMakeCacheFileManager(
					baseDataContainers);
		} catch (Exception e) {
			throw new EntityDataException("Error", e);
		}
	}

	public void scan(final SubMonitor monitor) throws EntityDataException {
		monitor.setWorkRemaining(100);
		ExecutorService executorService = ExecutorUtil
				.newFixedMultipleOfProcessorsThreadPool(2);
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					surveyRecordManager.scanRecords();
				} catch (Exception e) {
					LOGGER.fatal(e);
				}
				monitor.worked(10);
				return null;
			}
		});
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					statsFileManager.scanFiles();
				} catch (Exception e) {
					LOGGER.fatal(e);
				}
				monitor.worked(10);
				return null;
			}
		});
		callables.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					cMakeCacheFileManager.scanFiles();
				} catch (Exception e) {
					LOGGER.fatal(e);
				}
				monitor.worked(10);
				return null;
			}
		});
		try {
			executorService.invokeAll(callables);
		} catch (InterruptedException e) {
			LOGGER.fatal("Error matching " + Entity.class.getSimpleName(), e);
		}

		try {
			mapper = new Mapper(doclogDataDirectory);
		} catch (FileNotFoundException e) {
			// TODO
			try {
				throw new EntityDataException("Could not instantiate "
						+ Mapper.class.getSimpleName(), e);
			} catch (EntityDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		monitor.worked(10);

		entityManager = new EntityManager(diffContainer,
				doclogDataDirectory, surveyRecordManager, statsFileManager,
				cMakeCacheFileManager, mapper);
		entityManager.scan(monitor.newChild(60));
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public Mapper getMapper() {
		return mapper;
	}

	public StatsFileManager getStatsFileManager() {
		return statsFileManager;
	}

	public SurveyRecordManager getSurveyRecordManager() {
		return surveyRecordManager;
	}

	public CMakeCacheFileManager getCMakeCacheFileManager() {
		return cMakeCacheFileManager;
	}

}
