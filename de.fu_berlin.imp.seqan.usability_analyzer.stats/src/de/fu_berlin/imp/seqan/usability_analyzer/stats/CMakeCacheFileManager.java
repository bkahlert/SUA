package de.fu_berlin.imp.seqan.usability_analyzer.stats;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;

public class CMakeCacheFileManager extends DataSourceManager {
	private static final Logger LOGGER = Logger
			.getLogger(CMakeCacheFileManager.class);
	private List<CMakeCacheFile> cMakeCacheFiles;

	public CMakeCacheFileManager(File dataDirectory)
			throws DataSourceInvalidException {
		super(new File(dataDirectory, "diff"));
	}

	public void scanFiles() {
		List<CMakeCacheFile> cMakeCacheFiles = new ArrayList<CMakeCacheFile>();
		for (File diffFileDir : this.getFile().listFiles()) {
			if (!diffFileDir.isDirectory())
				continue;
			if (!ID.isValid(diffFileDir.getName())) {
				LOGGER.warn("Directory with invalid "
						+ ID.class.getSimpleName() + " name detected: "
						+ diffFileDir.toString());
				continue;
			}

			for (File cMakeCacheFile : diffFileDir
					.listFiles((FileFilter) new RegexFileFilter(
							CMakeCacheFile.PATTERN))) {
				cMakeCacheFiles.add(new CMakeCacheFile(cMakeCacheFile
						.getAbsolutePath()));
			}
		}
		this.cMakeCacheFiles = cMakeCacheFiles;
	}

	/**
	 * Returns a list of all {@link CMakeCacheFile}s
	 * 
	 * @return
	 */
	public List<CMakeCacheFile> getCMakeCacheFiles() {
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
		for (CMakeCacheFile cMakeCacheFile : this.cMakeCacheFiles) {
			if (!cMakeCacheIDs.contains(cMakeCacheFile.getId())) {
				cMakeCacheIDs.add(cMakeCacheFile.getId());
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
		for (CMakeCacheFile cMakeCacheFile : this.cMakeCacheFiles) {
			if (cMakeCacheFile.getId().equals(id)) {
				return cMakeCacheFile;
			}
		}
		return null;
	}
}
