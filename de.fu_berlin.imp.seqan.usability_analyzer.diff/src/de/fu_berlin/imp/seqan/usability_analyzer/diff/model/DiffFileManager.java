package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.TrunkUtils;
import difflib.PatchFailedException;

public class DiffFileManager extends DataSourceManager {

	private Logger logger = Logger.getLogger(DiffFileManager.class);

	private File logDirectory;
	private File trunkDirectory;
	private Map<ID, DiffFileList> diffFiles;

	public DiffFileManager(File logDirectory, File trunkDirectory)
			throws DataSourceInvalidException {
		super(logDirectory);

		this.logDirectory = logDirectory;
		this.trunkDirectory = trunkDirectory;

		this.scanFiles();
		this.sort();
		this.calculateRecordMillisecondsPassed();
		this.calculateNeighbors();
		this.calculateSources();
	}

	public void scanFiles() {
		Map<ID, DiffFileList> diffFiles = new HashMap<ID, DiffFileList>();
		for (File diffFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(DiffFile.PATTERN))) {
			DiffFile currentDiffFile = new DiffFile(this.logDirectory,
					diffFile.getAbsolutePath());
			ID id = currentDiffFile.getId();
			if (!diffFiles.containsKey(id))
				diffFiles.put(id, new DiffFileList());
			diffFiles.get(id).add(currentDiffFile);
		}
		this.diffFiles = diffFiles;
	}

	private void sort() {
		for (ID id : this.diffFiles.keySet()) {
			DiffFileList diffFiles = this.diffFiles.get(id);
			Collections.sort(diffFiles);
		}
	}

	private void calculateRecordMillisecondsPassed() {
		for (ID id : this.diffFiles.keySet()) {
			DiffFileList diffFiles = this.diffFiles.get(id);
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

	private void calculateNeighbors() {
		for (ID id : this.diffFiles.keySet()) {
			HashMap<String, DiffFileRecord> fileNameToLastRecord = new HashMap<String, DiffFileRecord>();
			DiffFileList diffFiles = this.diffFiles.get(id);
			for (DiffFile diffFile : diffFiles) {
				DiffFileRecordList diffFileRecords = diffFile
						.getDiffFileRecords();
				if (diffFileRecords == null)
					continue;
				for (DiffFileRecord diffFileRecord : diffFileRecords) {
					String filename = diffFileRecord.getFilename();
					DiffFileRecord lastRecord = fileNameToLastRecord
							.get(filename);
					if (lastRecord != null) {
						lastRecord.setSuccessor(diffFileRecord);
						diffFileRecord.setPredecessor(lastRecord);
					}
					fileNameToLastRecord.put(filename, diffFileRecord);
				}
			}
		}
	}

	private void calculateSources() {
		for (ID id : this.diffFiles.keySet()) {
			HashMap<String, List<String>> fileNameToSource = new HashMap<String, List<String>>();
			HashMap<String, Boolean> fileNameToContinuePatching = new HashMap<String, Boolean>();
			DiffFileList diffFiles = this.diffFiles.get(id);
			for (DiffFile diffFile : diffFiles) {
				DiffFileRecordList diffFileRecords = diffFile
						.getDiffFileRecords();
				if (diffFileRecords == null)
					continue;
				for (DiffFileRecord diffFileRecord : diffFileRecords) {
					String filename = diffFileRecord.getFilename();

					// init continue flag
					if (!fileNameToContinuePatching.containsKey(filename)) {
						fileNameToContinuePatching.put(filename, true);
					}

					// check continue flag
					if (!fileNameToContinuePatching.get(filename)) {
						System.err.println("skipped");
						continue;
					}

					if (diffFileRecord.sourceExists()) {
						// skip already patched records
					} else {
						// first source read
						if (!fileNameToSource.containsKey(filename)) {
							String source;

							if (diffFileRecord.getPredecessor() != null
									&& diffFileRecord.getPredecessor()
											.sourceExists()) {
								// start from the last successfully patched
								// record
								source = diffFileRecord.getPredecessor()
										.getSource();
							} else {
								// or if none exists read from the trunk
								source = readSourceFromTrunk(filename);
							}
							fileNameToSource.put(filename,
									Arrays.asList(source.split("\n")));
						}

						List<String> oldSource = fileNameToSource.get(filename);

						// Create patch
						try {
							List<String> newSource = diffFileRecord
									.computePatchedSource(oldSource);
							diffFileRecord.setAndPersistSource(newSource);
							fileNameToSource.put(filename, newSource);
						} catch (PatchFailedException e) {
							// future patches can't be applied
							fileNameToContinuePatching.put(filename, false);

							// we don't care about files in bin/*
							if (!filename.startsWith("bin/"))
								logger.warn("Could not patch ID: " + id + ", "
										+ filename, e);
						}
					}
				}
			}
		}
	}

	private String readSourceFromTrunk(String filename) {
		File diffFileRecordSourceFile = TrunkUtils.getTrunkFile(
				this.trunkDirectory, filename);
		String source = null;
		if (diffFileRecordSourceFile != null
				&& diffFileRecordSourceFile.canRead()) {
			try {
				source = FileUtils.readFileToString(diffFileRecordSourceFile);
			} catch (IOException e) {
				logger.error("Could not read the underlaying file of "
						+ DiffFileRecord.class.getSimpleName(), e);
			}
		} else {
			source = "";
		}
		return source;
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
