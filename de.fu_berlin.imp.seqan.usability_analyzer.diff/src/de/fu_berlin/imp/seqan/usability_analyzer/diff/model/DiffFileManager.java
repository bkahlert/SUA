package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.RegexFileFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class DiffFileManager extends DataSourceManager {
	private File logDirectory;
	private Map<ID, DiffFileList> diffFiles;

	public DiffFileManager(File logDirectory) throws DataSourceInvalidException {
		super(logDirectory);

		this.logDirectory = logDirectory;

		this.scanFiles();
		this.calculateRecordMillisecondsPassed();
	}

	public void scanFiles() {
		Map<ID, DiffFileList> diffFiles = new HashMap<ID, DiffFileList>();
		for (File diffFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(DiffFile.PATTERN))) {
			DiffFile currentDiffFile = new DiffFile(diffFile.getAbsolutePath());
			ID id = currentDiffFile.getId();
			if (!diffFiles.containsKey(id))
				diffFiles.put(id, new DiffFileList());
			diffFiles.get(id).add(currentDiffFile);
		}
		this.diffFiles = diffFiles;
	}

	private void calculateRecordMillisecondsPassed() {
		for (ID id : this.diffFiles.keySet()) {
			DiffFileList diffFiles = this.diffFiles.get(id);
			Collections.sort(diffFiles);
			for (DiffFile diffFile : diffFiles) {
				DiffFile successor = diffFiles.getSuccessor(diffFile);
				if (successor != null) {
					Long millisecondsPassed = successor.getDate().getTime()
							- diffFile.getDate().getTime();
					millisecondsPassed--; // the previous action ends one
											// minimal
											// moment before the next action
											// starts
					diffFile.setMillisecondsPassed(millisecondsPassed);
				}
			}
		}
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link DiffFile}s
	 * 
	 * @return
	 */
	public Set<ID> getIDs() {
		return this.diffFiles.keySet();
	}

	/**
	 * Returns all {@link DiffFile}s associated with a given {@link ID}
	 * 
	 * @param id
	 * @return
	 */
	public DiffFileList getDiffFiles(ID id) {
		return this.diffFiles.get(id);
	}
}
