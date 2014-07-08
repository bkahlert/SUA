package de.fu_berlin.imp.apiua.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;
import de.fu_berlin.imp.apiua.core.model.identifier.ID;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.stats.model.StatsFile;

public class StatsFileManager {

	private static final Logger LOGGER = Logger
			.getLogger(StatsFileManager.class);

	private Map<IBaseDataContainer, List<StatsFile>> statsFiles;

	public StatsFileManager(
			List<? extends IBaseDataContainer> baseDataContainers) {
		this.statsFiles = new HashMap<IBaseDataContainer, List<StatsFile>>();
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			this.statsFiles.put(baseDataContainer, null);
		}
	}

	public void scanFiles() {
		for (IBaseDataContainer baseDataContainer : this.statsFiles.keySet()) {
			IDataContainer diffFileContainer = baseDataContainer
					.getSubContainer("diff");

			List<StatsFile> statsFiles = new ArrayList<StatsFile>();
			user: for (IDataContainer userContainer : diffFileContainer
					.getSubContainers()) {
				if (IdentifierFactory.createFrom(userContainer.getName()) == null) {
					LOGGER.warn("Directory with invalid "
							+ IIdentifier.class.getSimpleName()
							+ " name detected: " + diffFileContainer.toString());
					continue;
				}

				for (IData stats : userContainer.getResources()) {
					if (!StatsFile.PATTERN.matcher(stats.getName()).matches()) {
						continue;
					}
					try {
						statsFiles.add(new StatsFile(stats));
						continue user;
					} catch (Exception e) {
						LOGGER.warn("Could not process stats file", e);
					}
				}
				LOGGER.warn("No stats file found for "
						+ userContainer.getName());
			}

			this.statsFiles.put(baseDataContainer, statsFiles);
		}
	}

	/**
	 * Returns a list of all {@link StatsFile}s
	 * 
	 * @return
	 */
	public List<StatsFile> getStatsFiles() {
		List<StatsFile> statsFiles = new ArrayList<StatsFile>();
		for (List<StatsFile> sf : this.statsFiles.values()) {
			statsFiles.addAll(sf);
		}
		return statsFiles;
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link StatsFile}s
	 * 
	 * @return
	 */
	public List<ID> getIDs() {
		List<ID> statsIDs = new ArrayList<ID>();
		for (List<StatsFile> statsFiles : this.statsFiles.values()) {
			for (StatsFile statsFile : statsFiles) {
				if (!statsIDs.contains(statsFile.getId())) {
					statsIDs.add(statsFile.getId());
				}
			}
		}
		return statsIDs;
	}

	/**
	 * Returns the {@link StatsFile} associated with a given {@link ID} TODO:
	 * only returns first occurence
	 * 
	 * @param id
	 * @return
	 */
	public StatsFile getStatsFile(ID id) {
		List<StatsFile> found = new ArrayList<StatsFile>();
		for (List<StatsFile> statsFiles : this.statsFiles.values()) {
			for (StatsFile statsFile : statsFiles) {
				if (statsFile.getId().equals(id)) {
					found.add(statsFile);
				}
			}
		}
		return found.size() > 0 ? found.get(0) : null;
	}
}
