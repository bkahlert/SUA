package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IOpenable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

/**
 * Instances of this class describe a record within a {@link IDiff}.
 * 
 * @author bkahlert
 * 
 */
public interface IDiffRecord extends HasDateRange, ICodeable, HasID, IOpenable {

	/**
	 * Returns the {@link IDiff} this {@link IDiffRecord} belongs to.
	 * 
	 * @return
	 */
	public IDiff getDiffFile();

	/**
	 * Returns the call that was used to generate this {@link IDiffRecord}.
	 * 
	 * @return
	 */
	public String getCommandLine();

	/**
	 * Returns the name of the file this {@link IDiffRecord} symbolizes.
	 * 
	 * @return
	 */
	public String getFilename();

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
	public List<String> getPatchLines();

	/**
	 * Returns true if this symbolized file is temporary.
	 * 
	 * @return
	 */
	public boolean isTemporary();

	/**
	 * Returns the file that looks like the one after this {@link IDiffRecord}
	 * has been applied.
	 * 
	 * @return
	 * @throws IOException
	 */
	public File getSourceFile() throws IOException;

	/**
	 * Returns true if {@link #getSourceFile()} returns a proper file.
	 * 
	 * @return
	 */
	public boolean sourceExists();

	/**
	 * Returns the contents of the file that looks like the one after this
	 * {@link IDiffRecord} has been applied.
	 * 
	 * @return
	 */
	public String getSource();

	/**
	 * Returns this {@link IDiffRecord}'s predecessor.
	 * 
	 * @return
	 */
	public IDiffRecord getPredecessor();

}