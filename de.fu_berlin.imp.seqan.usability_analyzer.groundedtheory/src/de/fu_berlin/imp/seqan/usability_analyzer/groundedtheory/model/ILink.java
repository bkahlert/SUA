package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

/**
 * Instances of this interface symbolize links between two {@link IEndpoint}s.
 * 
 * @author bkahlert
 * 
 */
public interface ILink extends ILocatable {

	public static interface IEndpoint {
	}

	public static interface ICoordinateEndpoint extends IEndpoint {
		public long getX();

		public long getY();
	}

	public static interface ICodeEndpoint extends IEndpoint {
		public URI getCode();
	}

	public String getTitle();

	public IEndpoint getSource();

	public IEndpoint getTarget();

}
