package de.fu_berlin.imp.apiua.diff.model.source;

import de.fu_berlin.imp.apiua.core.model.HasIdentifier;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.IOpenable;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.diff.model.ICompilable;

public interface IProgrammingSession extends Iterable<IRevision>,
		HasIdentifier, HasDateRange, ILocatable, IOpenable, ICompilable {
	public int size();
}
