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
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

public class StatsFileManager extends DataSourceManager {

	private Logger log = Logger.getLogger(StatsFileManager.class);

	private File logDirectory;
	private List<StatsFile> statsFiles;

	public StatsFileManager(File logDirectory)
			throws DataSourceInvalidException {
		super(logDirectory);

		this.logDirectory = logDirectory;
	}

	public void scanFiles() {
		List<StatsFile> statsFiles = new ArrayList<StatsFile>();
		for (File statsFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(StatsFile.PATTERN))) {
			try {
				statsFiles.add(new StatsFile(statsFile.getAbsolutePath()));
			} catch (DataSourceInvalidException e) {
				log.warn("Could not process stats file", e);
			}
		}
		this.statsFiles = statsFiles;
	}

	/**
	 * Returns a list of all {@link StatsFile}s
	 * 
	 * @return
	 */
	public List<StatsFile> getStatsFiles() {
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
		for (StatsFile statsFile : this.statsFiles) {
			if (!statsIDs.contains(statsFile.getId())) {
				statsIDs.add(statsFile.getId());
			}
		}
		return statsIDs;
	}

	/**
	 * Returns the {@link StatsFile} associated with a given {@link ID}
	 * 
	 * @param id
	 * @return
	 */
	public StatsFile getStatsFile(ID id) {
		for (StatsFile statsFile : this.statsFiles) {
			if (statsFile.getId().equals(id)) {
				return statsFile;
			}
		}
		return null;
	}
}
