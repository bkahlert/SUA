package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;

public class DoclogDirectory extends File {

	private static final long serialVersionUID = 6163186892736791622L;
	private static final Logger LOGGER = Logger
			.getLogger(DoclogDirectory.class);

	private static Map<Object, File> readDoclogFileMappings(File directory) {
		Map<Object, File> rawFiles = new HashMap<Object, File>();
		for (File diffFile : directory
				.listFiles((FileFilter) new RegexFileFilter(
						DoclogFile.ID_PATTERN))) {
			ID id = DoclogFile.getId(diffFile);
			rawFiles.put(id, diffFile);
		}
		for (File diffFile : directory
				.listFiles((FileFilter) new RegexFileFilter(
						DoclogFile.FINGERPRINT_PATTERN))) {
			Fingerprint fingerprint = DoclogFile.getFingerprint(diffFile);
			rawFiles.put(fingerprint, diffFile);
		}
		return rawFiles;
	}

	private Map<Object, File> files;
	private Map<Object, TimeZoneDateRange> fileDateRanges;
	private Map<Object, Token> fileToken;

	public DoclogDirectory(File dataDirectory)
			throws DataSourceInvalidException {
		super(dataDirectory.getAbsolutePath());

		long start = System.currentTimeMillis();
		this.files = readDoclogFileMappings(this);
		this.fileDateRanges = new HashMap<Object, TimeZoneDateRange>(
				this.files.size());
		this.fileToken = new HashMap<Object, Token>(this.files.size());
		for (Object key : this.files.keySet()) {
			if (key.equals(new Fingerprint("4cc22d53a1f8e318697b8e9613c0134d"))) {
				System.err.println("k");
			}
			this.fileDateRanges.put(key,
					DoclogFile.getDateRange(this.files.get(key)));
			this.fileToken.put(key, DoclogFile.getToken(this.files.get(key)));
		}
		long end = System.currentTimeMillis();
		LOGGER.info(DoclogDirectory.class.getSimpleName() + " "
				+ dataDirectory.getName() + " scanned within " + (end - start)
				+ "ms.");
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link DoclogFile}s
	 * 
	 * @return
	 */
	public List<ID> getIDs() {
		List<ID> doclogIDs = new ArrayList<ID>();
		for (Object key : this.files.keySet()) {
			if (key instanceof ID)
				doclogIDs.add((ID) key);
		}
		return doclogIDs;
	}

	/**
	 * Returns the {@link ID} that is associated to a {@link DoclogFile}
	 * containing the specified {@link Token}.
	 * 
	 * @param token
	 * @return
	 */
	public ID getID(Token token) {
		for (Object key : this.fileToken.keySet()) {
			if (key instanceof ID && this.fileToken.get(key) != null
					&& this.fileToken.get(key).equals(token)) {
				return (ID) key;
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
		for (Object key : this.files.keySet()) {
			if (key instanceof Fingerprint)
				doclogFingerprints.add((Fingerprint) key);
		}
		return doclogFingerprints;
	}

	/**
	 * Returns a list of the {@link Fingerprint}s that are associated to a
	 * {@link DoclogFile} containing the specified {@link Token}.
	 * 
	 * @param token
	 * @return
	 */
	public List<Fingerprint> getFingerprints(Token token) {
		LinkedList<Fingerprint> fingerprints = new LinkedList<Fingerprint>();
		for (Object key : this.fileToken.keySet()) {
			if (key instanceof Fingerprint && this.fileToken.get(key) != null
					&& this.fileToken.get(key).equals(token)) {
				fingerprints.add((Fingerprint) key);
			}
		}
		return fingerprints;
	}

	public Token getToken(Object key) {
		for (Object currentKey : this.fileToken.keySet()) {
			if (currentKey.equals(key)) {
				return this.fileToken.get(key);
			}
		}
		return null;
	}

	public TimeZoneDateRange getDateRange(Object key) {
		return this.fileDateRanges.get(key);
	}

	/**
	 * Returns a list of all keys (of types {@link ID} and {@link Fingerprint}
	 * occurring in the managed {@link DoclogFile}s
	 * 
	 * @return
	 */
	public Set<Object> getKeys() {
		return this.files.keySet();
	}

	/**
	 * Returns a {@link File}Êthat can be parsed as a {@link ID} based
	 * {@link DoclogFile}.
	 * 
	 * @param id
	 * @return
	 */
	public File getFile(ID id) {
		return this.files.get(id);
	}

	/**
	 * Returns a {@link File}Êthat can be parsed as a {@link Fingerprint} based
	 * {@link DoclogFile}.
	 * 
	 * @param fingerprint
	 * @return
	 */
	public File getFile(Fingerprint fingerprint) {
		return this.files.get(fingerprint);
	}

	/**
	 * Returns the {@link DoclogFile} associated with a given {@link ID}
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 */
	public DoclogFile getDoclogFile(ID id, IProgressMonitor progressMonitor) {
		return this.getDoclogFile((Object) id, progressMonitor);
	}

	/**
	 * Returns the {@link DoclogFile} associated with a given
	 * {@link Fingerprint}
	 * 
	 * @param fingerprint
	 * @param progressMonitor
	 * @return
	 */
	public DoclogFile getDoclogFile(Fingerprint fingerprint,
			IProgressMonitor progressMonitor) {
		return this.getDoclogFile((Object) fingerprint, progressMonitor);
	}

	/**
	 * Returns the {@link DoclogFile} associated with a given key
	 * 
	 * @param key
	 * @param progressMonitor
	 * @return
	 */
	public DoclogFile getDoclogFile(Object key, IProgressMonitor progressMonitor) {
		progressMonitor.beginTask(
				"Parsing " + DoclogFile.class.getSimpleName(), 2);
		File file = this.files.get(key);
		if (file == null)
			return null;

		TimeZoneDateRange dateRange = this.fileDateRanges.get(key);
		Token token = this.fileToken.get(key);
		progressMonitor.worked(1);
		DoclogFile doclogFile = new DoclogFile(file, key, dateRange, token);
		progressMonitor.worked(1);
		progressMonitor.done();
		return doclogFile;
	}
}
