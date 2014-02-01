package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.Iterator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecordHistory;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diffs;

/**
 * Instances of this class describe all {@link IDiff} belonging to a
 * {@link IIdentifier}.
 * 
 * @author bkahlert
 * 
 */
public interface IDiffs extends ILocatable, HasIdentifier, Iterable<IDiff>,
		HasDateRange {

	@Override
	public TimeZoneDateRange getDateRange();

	/**
	 * Returns the longest common prefix among the files described by the
	 * {@link DiffRecord} in this {@link Diffs}.
	 * <p>
	 * e.g. considering there are the files /source/a.txt, /source/b.txt,
	 * /src/c.txt this method would return /source/.
	 * 
	 * @return
	 */
	public String getLongestCommonPrefix();

	public IDiff getSuccessor(IDiff diff);

	/**
	 * Returns all {@link DiffRecord}'s describing the same file.
	 * 
	 * @param filename
	 * @return
	 */
	public DiffRecordHistory getHistory(String filename);

	@Override
	public Iterator<IDiff> iterator();

	/**
	 * Returns the i-th {@link IDiff} in this {@link Diffs}.
	 * <p>
	 * Throws an {@link IndexOutOfBoundsException} exception if i access an
	 * undefined position.
	 * 
	 * @param i
	 * @return
	 */
	public IDiff get(int i);

	/**
	 * Returns the number of {@link IDiff}s in this {@link Diffs}.
	 * 
	 * @return
	 */
	public int length();

	/**
	 * Returns a {@link IDiff} array representing the {@link IDiff}s of this
	 * {@link Diffs}.
	 * 
	 * @return
	 */
	public IDiff[] toArray();

}