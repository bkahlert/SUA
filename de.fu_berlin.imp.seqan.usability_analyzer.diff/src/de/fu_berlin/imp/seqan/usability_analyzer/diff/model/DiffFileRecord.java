package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class DiffFileRecord implements HasDateRange {

	private Logger logger = Logger.getLogger(DiffFileRecord.class);

	private DiffFile diffFile;
	private List<String> raw;

	private String commandLine;
	private DiffFileRecordMeta meta;
	private String patch;

	private File sourceFile;
	private DiffFileRecord predecessor;
	private DiffFileRecord successor;

	public DiffFileRecord(File logDirectory, DiffFile diffFile,
			String commandLine, List<String> lines) {
		this.diffFile = diffFile;
		this.commandLine = commandLine;

		this.raw = lines;

		this.meta = new DiffFileRecordMeta(lines.get(0), lines.get(1));

		String[] content = Arrays.copyOfRange(lines.toArray(new String[0]), 2,
				lines.size());
		this.patch = StringUtils.join(content, "\n");

		this.sourceFile = new de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffUtils(
				logDirectory).getSourceFile(this);
	}

	public DiffFile getDiffFile() {
		return diffFile;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public String getFilename() {
		return this.meta.getToFileName().substring("./".length());
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
	public List<String> getPatch() {
		return raw;
	}

	/**
	 * Returns the patch without meta information
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * <code>
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
	 * @return
	 */
	public String getPatchContent() {
		return patch;
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

	/**
	 * Computes this {@link DiffFileRecord}'s source based on a given source and
	 * the patch this {@link DiffFileRecord} consists of.
	 * 
	 * TODO: Verify if it wouldn't be better to use getPredecessor in order to
	 * get the orginial source
	 * 
	 * @param original
	 * @return
	 * @throws PatchFailedException
	 */
	public List<String> computePatchedSource(List<String> original)
			throws PatchFailedException {
		Patch patch = DiffUtils.parseUnifiedDiff(this.getPatch());
		@SuppressWarnings("unchecked")
		List<String> newSource = (List<String>) DiffUtils
				.patch(original, patch);
		return newSource;
	}

	public String getSource() {
		String source = null;
		try {
			source = FileUtils.readFileToString(this.sourceFile);
		} catch (IOException e) {
			logger.debug("Could not find source file for "
					+ DiffFileRecord.class.getSimpleName() + ": " + this, e);
		}
		return source;
	}

	void setAndPersistSource(List<String> source) {
		String sourceString = StringUtils.join(source, "\n");
		if (sourceString.endsWith("\n")) {
			sourceString = sourceString.substring(0, sourceString.length() - 1);
		}
		try {
			FileUtils.write(this.sourceFile, sourceString);
		} catch (IOException e) {
			logger.error("Could not write source file for "
					+ DiffFileRecord.class.getSimpleName() + ": " + this, e);
		}
	}

	// TODO evtl. cachen
	public boolean sourceExists() {
		return this.sourceFile.canRead();
	}

	void setPredecessor(DiffFileRecord predecessor) {
		this.predecessor = predecessor;
	}

	public DiffFileRecord getPredecessor() {
		return predecessor;
	}

	void setSuccessor(DiffFileRecord successor) {
		this.successor = successor;
	}

	public DiffFileRecord getSuccessor() {
		return successor;
	}
}
