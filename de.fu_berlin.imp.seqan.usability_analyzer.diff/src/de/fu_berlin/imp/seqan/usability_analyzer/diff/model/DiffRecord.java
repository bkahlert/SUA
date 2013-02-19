package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IOpenable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DiffRecord implements HasDateRange, ICodeable, HasID, IOpenable {

	private static final long serialVersionUID = -1200532570493919910L;

	private static Logger LOGGER = Logger.getLogger(DiffRecord.class);

	private Diff diff;
	private IData originalSource;
	private ISourceStore sourceCache;

	private String commandLine;
	private DiffRecordMeta meta;

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

	private DiffRecord predecessor = null;

	private boolean patchFailed = false;

	public DiffRecord(Diff diff, IData originalSource,
			ISourceStore sourceCache, String commandLine, DiffRecordMeta meta,
			long contentStart, long contentEnd) {
		this.diff = diff;
		this.originalSource = originalSource;
		this.sourceCache = sourceCache;

		this.commandLine = commandLine;
		this.meta = meta;
		this.patchStart = contentStart;
		this.patchEnd = contentEnd;

		getSource(); // implicitly patches
		if (this.patchFailed) {
			LOGGER.warn("Failed to patch " + originalSource + " of "
					+ diff.getID());
		}
	}

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

	public ID getID() {
		return this.diff.getID();
	}

	public Diff getDiffFile() {
		return diff;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public String getFilename() {
		return this.meta.getToFileName();
	}

	/**
	 * Returns the complete patch
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * <code>
	 * --- ./misc/seqan_instrumentation/last_revision_copy/sandbox/tum/apps/ministellar/ministellar.cpp	2011-09-13 14:57:41.156250000 +0200
	 * +++ ./sandbox/tum/apps/ministellar/ministellar.cpp	2011-09-13 14:59:20.984375000 +0200
	 * @@ -133,8 +133,8 @@
	 *                  // HINT: Create an align object on infixes of the sequences.
	 *  
	 *                  Align<TInfix, ArrayGaps> ali2;
	 * -                appendValue(rows(ali2), infix(seqs1[0], leftDim0(seed), rightDim0(seed));
	 * -                appendValue(rows(ali2), infix(seqs2[0], leftDim1(seed), rightDim1(seed));
	 * +                appendValue(rows(ali2), infix(seqs1[0], getBeginDim0(seed), getEndDim0(seed));
	 * +                appendValue(rows(ali2), infix(seqs2[0], getBeginDim1(seed), getEndDim1(seed));
	 *  
	 *                  //TScoreValue scoreValue2 = globalAlignment(ali2, score, NeedlemanWunsch());
	 *                  //std::cout << ali2 << std::endl;
	 * 
	 * </code>
	 * </pre>
	 * 
	 * @return list of patch lines
	 */
	public List<String> getPatchLines() {
		return this.getDiffFile().getContent(this.patchStart, this.patchEnd);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getFilename();
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.meta.getDateRange();
	}

	public boolean isTemporary() {
		return this.getFilename().endsWith("~");
	}

	public File getSourceFile() throws IOException {
		return this.sourceCache.getSourceFile(diff.getID(), diff.getRevision(),
				this.meta.getToFileName());
	}

	public boolean sourceExists() {
		try {
			File sourceFile = getSourceFile();
			return sourceFile != null && sourceFile.exists();
		} catch (IOException e) {
			LOGGER.error(e);
			return false;
		}
	}

	public String getSource() {
		DiffRecord predecessor = this.getPredecessor();
		if ((predecessor != null && predecessor.patchFailed) || patchFailed) {
			this.patchFailed = true;
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
				patchFailed = true;
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

	public DiffRecord getPredecessor() {
		if (this.predecessor == null) {
			String filename = getFilename();
			IDiff prevDiffFile = this.getDiffFile().getPrevDiffFile();
			outer: while (prevDiffFile != null) {
				for (DiffRecord diffRecord : prevDiffFile.getDiffFileRecords()) {
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

	@Override
	public void open() {
		DiffFileEditorUtils.closeCompareEditors(this);
		DiffFileEditorUtils.openCompareEditor(this);
	}
}
