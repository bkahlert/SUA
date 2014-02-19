package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.net.URI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

public interface ICodeInstance extends ILocatable, HasDateRange, HasIdentifier {
	/**
	 * Returns the internal identifier for this {@link ICodeInstance}. Each ID
	 * should only be used once in the life cycle of the application.
	 * 
	 * @return
	 */
	long getCodeInstanceID();

	/**
	 * Returns the {@link ICodeInstance}'s public URI.
	 */
	@Override
	public URI getUri();

	public ICode getCode();

	/**
	 * Returns the {@link URI} of the resource this {@link ICodeInstance} links
	 * to the {@link ICode} returned by {@link #getCode()}.
	 * 
	 * @return
	 */
	public URI getId();

	public TimeZoneDate getCreation();
}
