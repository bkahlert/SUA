package de.fu_berlin.imp.seqan.usability_analyzer.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

public class StatsFileManager {

	private static final Logger LOGGER = Logger
			.getLogger(StatsFileManager.class);

	private Map<IBaseDataContainer, List<StatsFile>> statsFiles;

	public StatsFileManager(
			List<? extends IBaseDataContainer> baseDataContainers) {
		statsFiles = new HashMap<IBaseDataContainer, List<StatsFile>>();
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			this.statsFiles.put(baseDataContainer, null);
		}
	}

	public void scanFiles() {
		for (IBaseDataContainer baseDataContainer : this.statsFiles.keySet()) {
			IDataContainer diffFileContainer = baseDataContainer
					.getSubContainer("diff");
			if (!ID.isValid(diffFileContainer.getName())) {
				LOGGER.warn("Directory with invalid "
						+ ID.class.getSimpleName() + " name detected: "
						+ diffFileContainer.toString());
				continue;
			}

			List<StatsFile> statsFiles = new ArrayList<StatsFile>();
			for (IData resource : diffFileContainer.getResources()) {
				if (!StatsFile.PATTERN.matcher(resource.getName()).matches())
					continue;
				try {
					statsFiles.add(new StatsFile(resource));
				} catch (Exception e) {
					LOGGER.warn("Could not process stats file", e);
				}
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
		for (List<StatsFile> sf : this.statsFiles.values())
			statsFiles.addAll(sf);
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
		for (List<StatsFile> statsFiles : this.statsFiles.values()) {
			for (StatsFile statsFile : statsFiles) {
				if (statsFile.getId().equals(id)) {
					return statsFile;
				}
			}
		}
		return null;
	}
}
