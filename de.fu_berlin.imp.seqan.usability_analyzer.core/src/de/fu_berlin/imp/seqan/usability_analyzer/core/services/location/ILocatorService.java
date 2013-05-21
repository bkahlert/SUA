package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;
import java.util.concurrent.Future;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;

/**
 * Instances of this class can be part of an {@link IWorkSession}.
 * 
 * @author bkahlert
 * 
 */
public interface ILocatorService {

	/**
	 * Returns the {@link ILocatable} that is addressed by the given URI.
	 * 
	 * @param locatable
	 * @return
	 */
	public Future<ILocatable> getObject(URI uri);

}
