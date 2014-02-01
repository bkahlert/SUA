package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.source;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IOpenable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;

public interface IProgrammingSession extends Iterable<IRevision>,
		HasIdentifier, HasDateRange, ILocatable, IOpenable, ICompilable {
	public int size();
}
