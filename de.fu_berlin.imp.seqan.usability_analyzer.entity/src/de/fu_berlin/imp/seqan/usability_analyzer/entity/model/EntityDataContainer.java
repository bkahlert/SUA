package de.fu_berlin.imp.seqan.usability_analyzer.entity.model;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.AggregatedBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.EntityManager;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.mapping.Mapper;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.CMakeCacheFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.StatsFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyContainer;

public class EntityDataContainer extends AggregatedBaseDataContainer {

	private static final Logger LOGGER = Logger
			.getLogger(EntityDataContainer.class);

	private SurveyContainer surveyContainer;
	private StatsFileManager statsFileManager;
	private CMakeCacheFileManager cMakeCacheFileManager;

	private Mapper mapper;

	private EntityManager entityManager;

	private DiffContainer diffContainer;

	private DoclogDataContainer doclogDataContainer;

	public EntityDataContainer(
			List<? extends IBaseDataContainer> baseDataContainers,
			DiffContainer diffContainer,
			DoclogDataContainer doclogDataContainer,
			SurveyContainer surveyContainer) throws EntityDataException {
		super(baseDataContainers);
		this.diffContainer = diffContainer;
		this.doclogDataContainer = doclogDataContainer;
		this.surveyContainer = surveyContainer;

		try {
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
			mapper = new Mapper(doclogDataContainer);
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

		entityManager = new EntityManager(diffContainer, doclogDataContainer,
				surveyContainer, statsFileManager, cMakeCacheFileManager,
				mapper);
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

	public CMakeCacheFileManager getCMakeCacheFileManager() {
		return cMakeCacheFileManager;
	}

}
