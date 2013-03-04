package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordMeta;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;

public class DiffRecord implements IDiffRecord {

	private static final long serialVersionUID = -1200532570493919910L;

	private static Logger LOGGER = Logger.getLogger(DiffRecord.class);

	private IDiff diff;
	private IData originalSource;
	private ISourceStore sourceCache;

	private String commandLine;
	private IDiffRecordMeta meta;

	/**
	 * Contains the byte position within the parent {@link Diff} where the patch
	 * starts
	 */
	private long patchStart;

	/**
	 * Contains the byte position within the parent {@link Diff} where the patch
	 * end
	 */
	private long patchEnd;

	private IDiffRecord predecessor = null;

	private static List<IDiffRecord> patchFailed = new ArrayList<IDiffRecord>();

	public DiffRecord(IDiff diff, IData originalSource,
			ISourceStore sourceCache, String commandLine, IDiffRecordMeta meta,
			long contentStart, long contentEnd) {
		this.diff = diff;
		this.originalSource = originalSource;
		this.sourceCache = sourceCache;

		this.commandLine = commandLine;
		this.meta = meta;
		this.patchStart = contentStart;
		this.patchEnd = contentEnd;

		getSource(); // implicitly patches
		if (patchFailed.contains(this)) {
			LOGGER.warn("Failed to patch " + originalSource + " of "
					+ diff.getID());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getCodeInstanceID()
	 */
	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI(this.getDiffFile().getCodeInstanceID().toString()
					+ "/" + URLEncoder.encode(meta.getToFileName(), "UTF-8"));
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for a "
							+ DiffRecord.class.getSimpleName(), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#getID()
	 */
	@Override
	public ID getID() {
		return this.diff.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#getDiffFile
	 * ()
	 */
	@Override
	public IDiff getDiffFile() {
		return diff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getCommandLine()
	 */
	@Override
	public String getCommandLine() {
		return commandLine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#getFilename
	 * ()
	 */
	@Override
	public String getFilename() {
		return this.meta.getToFileName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getPatchLines()
	 */
	@Override
	public List<String> getPatchLines() {
		return this.getDiffFile().getContent(this.patchStart, this.patchEnd);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getFilename();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#getDateRange
	 * ()
	 */
	@Override
	public TimeZoneDateRange getDateRange() {
		return this.meta.getDateRange();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#isTemporary
	 * ()
	 */
	@Override
	public boolean isTemporary() {
		return this.getFilename().endsWith("~");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getSourceFile()
	 */
	@Override
	public File getSourceFile() throws IOException {
		return this.sourceCache.getSourceFile(diff.getID(), diff.getRevision(),
				this.meta.getToFileName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#sourceExists
	 * ()
	 */
	@Override
	public boolean sourceExists() {
		try {
			File sourceFile = getSourceFile();
			return sourceFile != null && sourceFile.exists();
		} catch (IOException e) {
			LOGGER.error(e);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#getSource
	 * ()
	 */
	@Override
	public String getSource() {
		if (patchFailed.contains(this)) {
			return null;
		}

		IDiffRecord predecessor = this.getPredecessor();
		if ((predecessor != null && patchFailed.contains(predecessor))) {
			patchFailed.add(this);
			return null;
		}

		if (this.sourceExists()) {
			try {
				return FileUtils.readFileToString(this.getSourceFile());
			} catch (IOException e) {
				LOGGER.debug("Could not find source file for "
						+ DiffRecord.class.getSimpleName() + ": " + this, e);
				return null;
			}
		} else {
			String predecessorSource;
			if (predecessor == null) {
				if (this.originalSource != null) {
					try {
						predecessorSource = this.originalSource.read();
					} catch (RuntimeException e) {
						if (e.getCause() instanceof FileNotFoundException) {
							predecessorSource = "";
						} else {
							throw e;
						}
					}
				} else {
					predecessorSource = "";
				}
			} else {
				predecessorSource = predecessor.getSource();
			}

			try {
				List<String> patch = this.getPatchLines();
				List<String> newSource = DiffUtils.patch(predecessorSource,
						patch);
				if (newSource.size() == 0) {
					// user emptied file
				} else {
					String lastLine = newSource.get(newSource.size() - 1);
					if (lastLine.equals(""))
						newSource.remove(newSource.size() - 1);
					if (newSource.size() > 0
							&& newSource.get(newSource.size() - 1).endsWith(
									"\n"))
						newSource.get(newSource.size() - 1).replace("\n$", "");
				}
				this.setAndPersistSource(newSource);
				LOGGER.info("Successfully patched " + diff.getID() + ", "
						+ getFilename());
				return StringUtils.join(newSource, "\n");
			} catch (Exception e) {
				String filename = getFilename();
				LOGGER.warn("Could not patch ID: " + diff.getID() + ", "
						+ filename, e);
				patchFailed.add(this);
				return null;
			}
		}
	}

	void setAndPersistSource(List<String> source) {
		// this method consumes much space
		Runtime.getRuntime().gc();

		String sourceString = StringUtils.join(source, "\n");
		if (sourceString.endsWith("\n")) {
			sourceString = sourceString.substring(0, sourceString.length() - 1);
		}
		try {
			File tmp = File.createTempFile("source", ".tmp");
			FileUtils.write(tmp, sourceString);
			this.sourceCache.setSourceFile(diff.getID(), diff.getRevision(),
					this.meta.getToFileName(), tmp);
		} catch (IOException e) {
			LOGGER.error(
					"Could not write source file for "
							+ DiffRecord.class.getSimpleName() + ": " + this, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getPredecessor()
	 */
	@Override
	public IDiffRecord getPredecessor() {
		if (this.predecessor == null) {
			String filename = getFilename();
			IDiff prevDiffFile = this.getDiffFile().getPrevDiffFile();
			outer: while (prevDiffFile != null) {
				for (IDiffRecord diffRecord : prevDiffFile.getDiffFileRecords()) {
					if (diffRecord.getFilename().equals(filename)) {
						this.predecessor = diffRecord;
						break outer;
					}
				}
				prevDiffFile = prevDiffFile.getPrevDiffFile();
			}
		}
		return this.predecessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#open()
	 */
	@Override
	public void open() {
		DiffFileEditorUtils.closeCompareEditors(this);
		DiffFileEditorUtils.openCompareEditor(this);
	}

}
