package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.TrunkUtils;

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
		this.calculateSources();
	}

	public void scanFiles() {
		Map<ID, DiffFileList> diffFiles = new HashMap<ID, DiffFileList>();
		for (File diffFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(DiffFile.PATTERN))) {
			DiffFile currentDiffFile = new DiffFile(this.trunkDirectory,
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

	private void calculateSources() {
		for (ID id : this.diffFiles.keySet()) {
			HashMap<String, String> fileNameToSource = new HashMap<String, String>();
			DiffFileList diffFiles = this.diffFiles.get(id);
			for (DiffFile diffFile : diffFiles) {
				DiffFileRecordList diffFileRecords = diffFile
						.getDiffFileRecords();
				if (diffFileRecords == null)
					continue;
				for (DiffFileRecord diffFileRecord : diffFileRecords) {
					String filename = diffFileRecord.getFilename();
					if (!fileNameToSource.containsKey(filename)) {
						String source = readSourceFromTrunk(filename);
						fileNameToSource.put(filename, source);
					}
					diff_match_patch dmp = new diff_match_patch();
					String oldSource = fileNameToSource.get(filename);
					String patch = diffFileRecord.getContent();
					LinkedList<Patch> patch2 = dmp.patch_fromText(patch);
					for (Patch patch3 : patch2) {
						for (Diff diff : patch3.diffs) {
							diff.text += "\n";
						}
					}

					Object[] applied = dmp.patch_apply(patch2, oldSource);
					if (applied.length == 2 && applied[0] instanceof String
							&& applied[1] instanceof boolean[]) {
						String newSource = (String) applied[0];
						boolean[] patchResults = (boolean[]) applied[1];
						for (int i = 1, j = patchResults.length; i < j; i++) {
							if (!patchResults[i])
								logger.warn("Could not apply "
										+ Patch.class.getSimpleName() + " " + i
										+ " to " + filename);
						}
						fileNameToSource.put(filename, newSource);
						diffFileRecord.setSource(newSource);
					} else {
						logger.fatal(diff_match_patch.class.getSimpleName()
								+ " returned empty result");
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
