package de.fu_berlin.imp.seqan.usability_analyzer.doclog;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.RegexFileFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceManager;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFileList;

public class DoclogManager extends DataSourceManager {
	private File logDirectory;

	private DoclogFileList doclogFiles;
	private DoclogFileList fingerprintDoclogFiles;

	public DoclogManager(File logDirectory) throws DataSourceInvalidException {
		super(logDirectory);

		this.logDirectory = logDirectory;

		scanFiles();
	}

	public void scanFiles() {
		this.doclogFiles = new DoclogFileList();
		for (File diffFile : this.logDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return true;
			}
		})) {
			System.out.println(diffFile);
		}
		for (File diffFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(
						DoclogFile.ID_PATTERN))) {
			System.err.println(diffFile);
			System.err.println(diffFile.toString().charAt(40));
			String toString = diffFile.toString();
			this.doclogFiles.add(new DoclogFile(diffFile.getAbsolutePath()));
		}

		this.fingerprintDoclogFiles = new DoclogFileList();
		for (File diffFile : this.logDirectory
				.listFiles((FileFilter) new RegexFileFilter(
						DoclogFile.FINGERPRINT_PATTERN))) {
			this.fingerprintDoclogFiles.add(new DoclogFile(diffFile
					.getAbsolutePath()));
		}
	}

	/**
	 * Returns a list of all {@link DoclogFile}s that have an {@link ID}
	 * 
	 * @return
	 */
	public DoclogFileList getDoclogFiles() {
		return this.doclogFiles;
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link DoclogFile}s
	 * 
	 * @return
	 */
	public List<ID> getIDs() {
		List<ID> doclogIDs = new ArrayList<ID>();
		for (DoclogFile doclogFile : this.doclogFiles) {
			if (!doclogIDs.contains(doclogFile.getId())) {
				doclogIDs.add(doclogFile.getId());
			}
		}
		return doclogIDs;
	}

	/**
	 * Returns the {@link DoclogFile} associated with a given {@link ID}
	 * 
	 * @param id
	 * @return
	 */
	public DoclogFile getDoclogFile(ID id) {
		for (DoclogFile doclogFile : this.doclogFiles) {
			if (doclogFile.getId().equals(id)) {
				return doclogFile;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all {@link DoclogFile}s that only have a
	 * {@link Fingerprint}
	 * 
	 * @return
	 */
	public DoclogFileList getFingerprintDoclogFiles() {
		return this.fingerprintDoclogFiles;
	}

	/**
	 * Returns the {@link DoclogFile} associated with a given
	 * {@link Fingerprint}
	 * 
	 * @param fingerprint
	 * @return
	 */
	public DoclogFile getDoclogFile(Fingerprint fingerprint) {
		for (DoclogFile doclogFile : this.fingerprintDoclogFiles) {
			if (doclogFile.getFingerprint().equals(fingerprint)) {
				return doclogFile;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all {@link Fingerprint}s occurring in the managed
	 * {@link DoclogFile}s
	 * 
	 * @return
	 */
	public List<Fingerprint> getFingerprints() {
		List<Fingerprint> doclogFingerprints = new ArrayList<Fingerprint>();
		for (DoclogFile fingerprintDoclogFile : this.fingerprintDoclogFiles) {
			if (!doclogFingerprints.contains(fingerprintDoclogFile
					.getFingerprint())) {
				doclogFingerprints.add(fingerprintDoclogFile.getFingerprint());
			}
		}
		return doclogFingerprints;
	}
}
