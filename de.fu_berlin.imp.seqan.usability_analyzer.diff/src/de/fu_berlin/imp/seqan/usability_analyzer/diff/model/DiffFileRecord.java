package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import difflib.PatchFailedException;

public class DiffFileRecord implements HasDateRange, ICodeable {

	private static final long serialVersionUID = -1200532570493919910L;

	private Logger logger = Logger.getLogger(DiffFileRecord.class);

	private DiffFile diffFile;
	private File originalSourceFile;
	private File cachedSourceFile;

	private String commandLine;
	private DiffFileRecordMeta meta;

	/**
	 * Contains the byte position within the parent {@link DiffFile} where the
	 * patch starts
	 */
	private long patchStart;

	/**
	 * Contains the byte position within the parent {@link DiffFile} where the
	 * patch end
	 */
	private long patchEnd;

	private DiffFileRecord predecessor = null;

	private boolean patchFailed = false;

	public DiffFileRecord(DiffFile diffFile, File originalSourceFile,
			File cachedSourceFile, String commandLine, DiffFileRecordMeta meta,
			long contentStart, long contentEnd) {
		this.diffFile = diffFile;
		this.originalSourceFile = originalSourceFile;
		this.cachedSourceFile = cachedSourceFile;

		this.commandLine = commandLine;
		this.meta = meta;
		this.patchStart = contentStart;
		this.patchEnd = contentEnd;

		getSource(); // implicitly patches
	}

	@Override
	public String getCodeInstanceID() {
		try {
			return this.getDiffFile().getCodeInstanceID()
					+ "/"
					+ URLEncoder.encode(originalSourceFile.getAbsolutePath(),
							"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.fatal("Could not calculate ID of " + this);
			return null;
		}
	}

	public DiffFile getDiffFile() {
		return diffFile;
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

	public File getSourceFile() {
		return this.cachedSourceFile;
	}

	public boolean sourceExists() {
		return this.cachedSourceFile != null ? this.cachedSourceFile.canRead()
				: false;
	}

	public String getSource() {
		DiffFileRecord predecessor = this.getPredecessor();
		if ((predecessor != null && predecessor.patchFailed) || patchFailed) {
			this.patchFailed = true;
			return null;
		}

		if (this.sourceExists()) {
			try {
				return FileUtils.readFileToString(this.cachedSourceFile);
			} catch (IOException e) {
				logger.debug("Could not find source file for "
						+ DiffFileRecord.class.getSimpleName() + ": " + this, e);
				return null;
			}
		} else {
			String predecessorSource;
			if (predecessor == null) {
				if (this.originalSourceFile != null
						&& this.originalSourceFile.canRead()) {
					try {
						predecessorSource = FileUtils
								.readFileToString(this.originalSourceFile);
					} catch (IOException e) {
						logger.error("Could not read the underlaying file of "
								+ DiffFileRecord.class.getSimpleName(), e);
						predecessorSource = "";
					}
				} else {
					predecessorSource = "";
				}
			} else {
				predecessorSource = predecessor.getSource();
			}

			try {
				List<String> newSource = DiffUtils.patch(predecessorSource,
						this.getPatchLines());
				String lastLine = newSource.get(newSource.size() - 1);
				if (lastLine.equals(""))
					newSource.remove(newSource.size() - 1);
				if (newSource.size() > 0
						&& newSource.get(newSource.size() - 1).endsWith("\n"))
					newSource.get(newSource.size() - 1).replace("\n$", "");
				this.setAndPersistSource(newSource);
				return StringUtils.join(newSource, "\n");
			} catch (PatchFailedException e) {
				String filename = getFilename();
				logger.warn("Could not patch ID: " + diffFile.getId() + ", "
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
			FileUtils.write(this.cachedSourceFile, sourceString);
		} catch (IOException e) {
			logger.error("Could not write source file for "
					+ DiffFileRecord.class.getSimpleName() + ": " + this, e);
		}
	}

	public DiffFileRecord getPredecessor() {
		if (this.predecessor == null) {
			String filename = getFilename();
			DiffFile prevDiffFile = this.getDiffFile().getPrevDiffFile();
			outer: while (prevDiffFile != null) {
				for (DiffFileRecord diffFileRecord : prevDiffFile
						.getDiffFileRecords()) {
					if (diffFileRecord.getFilename().equals(filename)) {
						this.predecessor = diffFileRecord;
						break outer;
					}
				}
				prevDiffFile = prevDiffFile.getPrevDiffFile();
			}
		}
		return this.predecessor;
	}
}
