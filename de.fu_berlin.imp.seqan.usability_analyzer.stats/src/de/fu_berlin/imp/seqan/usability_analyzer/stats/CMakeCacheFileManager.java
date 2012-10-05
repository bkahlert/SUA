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
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;

public class CMakeCacheFileManager {
	private static final Logger LOGGER = Logger
			.getLogger(CMakeCacheFileManager.class);
	private Map<IBaseDataContainer, List<CMakeCacheFile>> cMakeCacheFiles;

	public CMakeCacheFileManager(
			List<? extends IBaseDataContainer> baseDataContainers) {
		this.cMakeCacheFiles = new HashMap<IBaseDataContainer, List<CMakeCacheFile>>();
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			this.cMakeCacheFiles.put(baseDataContainer, null);
		}
	}

	public void scanFiles() {
		for (IBaseDataContainer baseDataContainer : cMakeCacheFiles.keySet()) {
			IDataContainer diffFileContainer = baseDataContainer
					.getSubContainer("diff");

			List<CMakeCacheFile> cMakeCacheFiles = new ArrayList<CMakeCacheFile>();
			user: for (IDataContainer userContainer : diffFileContainer
					.getSubContainers()) {
				if (!ID.isValid(userContainer.getName())) {
					LOGGER.warn("Directory with invalid "
							+ ID.class.getSimpleName() + " name detected: "
							+ userContainer.toString());
					continue;
				}

				for (IData cMakeCache : userContainer.getResources()) {
					if (!CMakeCacheFile.PATTERN.matcher(cMakeCache.getName())
							.matches())
						continue;
					cMakeCacheFiles.add(new CMakeCacheFile(cMakeCache));
					continue user;
				}
				LOGGER.warn("No CMakeCache file found for "
						+ userContainer.getName());
			}

			this.cMakeCacheFiles.put(baseDataContainer, cMakeCacheFiles);
		}
	}

	/**
	 * Returns a list of all {@link CMakeCacheFile}s
	 * 
	 * @return
	 */
	public List<CMakeCacheFile> getCMakeCacheFiles() {
		List<CMakeCacheFile> cMakeCacheFiles = new ArrayList<CMakeCacheFile>();
		for (List<CMakeCacheFile> sf : this.cMakeCacheFiles.values())
			cMakeCacheFiles.addAll(sf);
		return cMakeCacheFiles;
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link CMakeCacheFile}s
	 * 
	 * @return
	 */
	public List<ID> getIDs() {
		List<ID> cMakeCacheIDs = new ArrayList<ID>();
		for (List<CMakeCacheFile> cMakeCacheFiles : this.cMakeCacheFiles
				.values()) {
			for (CMakeCacheFile cMakeCacheFile : cMakeCacheFiles) {
				if (!cMakeCacheIDs.contains(cMakeCacheFile.getId())) {
					cMakeCacheIDs.add(cMakeCacheFile.getId());
				}
			}
		}
		return cMakeCacheIDs;
	}

	/**
	 * Returns the {@link CMakeCacheFile} associated with a given {@link ID}
	 * 
	 * @param id
	 * @return
	 */
	public CMakeCacheFile getCMakeCacheFile(ID id) {
		for (List<CMakeCacheFile> cMakeCacheFiles : this.cMakeCacheFiles
				.values()) {
			for (CMakeCacheFile cMakeCacheFile : cMakeCacheFiles) {
				if (cMakeCacheFile.getId().equals(id)) {
					return cMakeCacheFile;
				}
			}
		}
		return null;
	}
}
