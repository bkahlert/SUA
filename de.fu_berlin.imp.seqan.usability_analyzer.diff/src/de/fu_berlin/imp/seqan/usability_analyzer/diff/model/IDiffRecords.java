package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecords;

public interface IDiffRecords extends Iterable<IDiffRecord> {
	/**
	 * Creates a {@link DiffRecord} and add it to this {@link DiffRecords}
	 * 
	 * @param commandLine
	 * @param metaOldLine
	 * @param metaNewLine
	 * @param contentStart
	 * @param contentEnd
	 */
	public IDiffRecord createAndAddRecord(String commandLine,
			String metaOldLine, String metaNewLine, long contentStart,
			long contentEnd);

	public IDiffRecord get(int i);

	public int size();

	public Object[] toArray();
}