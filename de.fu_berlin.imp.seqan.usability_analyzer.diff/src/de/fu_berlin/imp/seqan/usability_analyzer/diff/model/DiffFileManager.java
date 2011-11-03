package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.RegexFileFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class DiffFileManager extends DataSourceManager {
	private File logDirectory;
	private DiffFileList diffFiles;

	public DiffFileManager(File logDirectory) throws DataSourceInvalidException {
		super(logDirectory);

		this.logDirectory = logDirectory;

		this.scanFiles();
	}

	public void scanFiles() {
		DiffFile lastDiffFile = null;
		DiffFileList diffFiles = new DiffFileList();
		for (File diffFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(DiffFile.PATTERN))) {
			DiffFile newDiffFile = new DiffFile(diffFile.getAbsolutePath());
			if (lastDiffFile != null) {
				long millisecondsPassed = newDiffFile.getDate().getTime()
						- lastDiffFile.getDate().getTime();
				lastDiffFile.setMillisecondsPassed(millisecondsPassed);
			}
			diffFiles.add(newDiffFile);
			lastDiffFile = newDiffFile;
		}
		this.diffFiles = diffFiles;
	}

	/**
	 * Returns a list of all {@link DiffFile}s
	 * 
	 * @return
	 */
	public List<DiffFile> getDiffFiles() {
		return diffFiles;
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link DiffFile}s
	 * 
	 * @return
	 */
	public List<ID> getIDs() {
		List<ID> diffIDs = new ArrayList<ID>();
		for (DiffFile diffFile : this.diffFiles) {
			if (!diffIDs.contains(diffFile.getId())) {
				diffIDs.add(diffFile.getId());
			}
		}
		return diffIDs;
	}

	/**
	 * Returns all {@link DiffFile}s associated with a given {@link ID}
	 * 
	 * @param id
	 * @return
	 */
	public DiffFileList getDiffFiles(ID id) {
		DiffFileList filteredDiffFiles = new DiffFileList();
		for (DiffFile diffFile : this.diffFiles) {
			if (diffFile.getId().equals(id)) {
				filteredDiffFiles.add(diffFile);
			}
		}
		return filteredDiffFiles;
	}

	public static Map<ID, DiffFileList> group(List<DiffFile> diffFiles) {
		Map<ID, DiffFileList> groupedDiffFiles = new HashMap<ID, DiffFileList>();
		for (DiffFile diffFile : diffFiles) {
			ID id = diffFile.getId();
			if (!groupedDiffFiles.containsKey(id))
				groupedDiffFiles.put(id, new DiffFileList());
			groupedDiffFiles.get(id).add(diffFile);
		}
		return groupedDiffFiles;
	}
}
