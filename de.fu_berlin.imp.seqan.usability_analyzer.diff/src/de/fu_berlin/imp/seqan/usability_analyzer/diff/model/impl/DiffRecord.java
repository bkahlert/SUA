package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.bkahlert.nebula.utils.DiffUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordMeta;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;

public class DiffRecord implements IDiffRecord {

	private static final long serialVersionUID = -1200532570493919910L;

	private static Logger LOGGER = Logger.getLogger(DiffRecord.class);

	private URI uri;

	private final IDiff diff;
	private final IData originalSource;
	private final ISourceStore sourceCache;

	private final String filename;
	private final TimeZoneDateRange dateRange;

	private final FLAGS[] flags;

	@Deprecated
	private String commandLine;
	@Deprecated
	private IDiffRecordMeta meta;

	/**
	 * Contains the byte position within the parent {@link Diff} where the patch
	 * starts
	 */
	@Deprecated
	private long patchStart;

	/**
	 * Contains the byte position within the parent {@link Diff} where the patch
	 * end
	 */
	@Deprecated
	private long patchEnd;

	private IDiffRecord predecessor = null;

	@Deprecated
	private static List<IDiffRecord> patchFailed = new ArrayList<IDiffRecord>();

	public DiffRecord(IDiff diff, IData originalSource,
			ISourceStore sourceCache, String filename,
			TimeZoneDateRange dateRange, List<FLAGS> flags) {
		this.diff = diff;
		this.originalSource = originalSource;
		this.sourceCache = sourceCache;

		this.filename = filename;
		this.dateRange = dateRange;
		this.flags = flags != null ? flags.toArray(new FLAGS[0]) : new FLAGS[0];
	}

	public DiffRecord(IDiff diff, IData originalSource,
			ISourceStore sourceCache, String commandLine, IDiffRecordMeta meta,
			long contentStart, long contentEnd, List<FLAGS> flags) {
		this(diff, originalSource, sourceCache, meta.getToFileName(), meta
				.getDateRange(), flags);

		this.commandLine = commandLine;
		this.meta = meta;
		this.patchStart = contentStart;
		this.patchEnd = contentEnd;

		this.getSource(); // implicitly patches
		if (patchFailed.contains(this)) {
			LOGGER.warn("Failed to patch " + originalSource + " of "
					+ diff.getIdentifier());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getCodeInstanceID()
	 */
	@Override
	public URI getUri() {
		if (this.uri == null) {
			try {
				this.uri = new URI(this.getDiffFile().getUri().toString()
						+ "/" + URLEncoder.encode(this.getFilename(), "UTF-8"));
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + DiffRecord.class, e);
			}
		}
		return this.uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getIdentifier()
	 */
	@Override
	public IIdentifier getIdentifier() {
		return this.diff.getIdentifier();
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
		return this.diff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getCommandLine()
	 */
	@Override
	@Deprecated
	public String getCommandLine() {
		return this.commandLine;
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
		return this.filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord#
	 * getPatchLines()
	 */
	@Override
	@Deprecated
	public List<String> getPatchLines() {
		return this.getDiffFile().getContent(this.patchStart, this.patchEnd);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getFilename()
				+ " @ " + this.diff.getRevision();
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
		return this.dateRange;
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
		return this.sourceCache.getSourceFile(this.diff.getIdentifier(),
				this.diff.getRevision(), this.getFilename());
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
			File sourceFile = this.getSourceFile();
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
		// zip file, no patching needed
		if (this.meta == null) {
			if (this.sourceExists()) {
				try {
					return FileUtils.readFileToString(this.getSourceFile());
				} catch (IOException e) {
					LOGGER.debug("Could not find source file for "
							+ DiffRecord.class.getSimpleName() + ": " + this, e);
					return null;
				}
			} else {
				return null;
			}
		}

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
				String newSource = DiffUtils.patch(predecessorSource,
						StringUtils.join(patch, "\n") + "\n");
				this.setAndPersistSource(newSource);
				LOGGER.info("Successfully patched " + this.diff.getIdentifier()
						+ ", " + this.getFilename());
				return newSource;
			} catch (Exception e) {
				String filename = this.getFilename();
				LOGGER.warn("Could not patch ID: " + this.diff.getIdentifier()
						+ ", " + filename, e);
				patchFailed.add(this);
				return null;
			}
		}
	}

	@Deprecated
	void setAndPersistSource(String sourceString) {
		// this method consumes much space
		Runtime.getRuntime().gc();

		try {
			File tmp = File.createTempFile("source", ".tmp");
			FileUtils.write(tmp, sourceString);
			this.sourceCache.setSourceFile(this.diff.getIdentifier(),
					this.diff.getRevision(), this.meta.getToFileName(), tmp);
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
			String filename = this.getFilename();
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

	@Override
	public FLAGS[] getFlags() {
		return this.flags;
	}

	@Override
	public boolean hasFlag(FLAGS flag) {
		for (FLAGS currFlag : this.flags) {
			if (currFlag == flag) {
				return true;
			}
		}
		return false;
	}

	@Override
	public File getFile() throws IOException {
		return this.diff.getFile();
	}

}
