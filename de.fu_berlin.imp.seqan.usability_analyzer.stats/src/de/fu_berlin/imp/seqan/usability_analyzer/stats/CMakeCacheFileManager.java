package de.fu_berlin.imp.seqan.usability_analyzer.stats;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.RegexFileFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;

public class CMakeCacheFileManager extends DataSourceManager {
	private File logDirectory;
	private List<CMakeCacheFile> cMakeCacheFiles;

	public CMakeCacheFileManager(File logDirectory)
			throws DataSourceInvalidException {
		super(logDirectory);

		this.logDirectory = logDirectory;

		this.scanFiles();
	}

	public void scanFiles() {
		List<CMakeCacheFile> cMakeCacheFiles = new ArrayList<CMakeCacheFile>();
		for (File cMakeCacheFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(
						CMakeCacheFile.PATTERN))) {
			cMakeCacheFiles.add(new CMakeCacheFile(cMakeCacheFile
					.getAbsolutePath()));
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
