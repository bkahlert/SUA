package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.util.HashSet;
import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class LocatableUtils {

	/**
	 * Tries to extract the {@link IIdentifier}s described in the
	 * {@link ILocatable}'s instance ids.
	 * <p>
	 * e.g. sua://diff/!sg2h/resource/abc will result in !sg2h, since this is
	 * detected as a valid {@link IIdentifier}
	 * 
	 * @param locatables
	 * @return
	 */
	public static Set<IIdentifier> getIdentifiers(final ILocatable[] locatables) {
		Set<IIdentifier> ids = new HashSet<IIdentifier>();
		for (ILocatable codeable : locatables) {
			String[] uri = codeable.getUri().getRawPath().split("/");

			if (uri.length > 1) {
				IIdentifier identifier = IdentifierFactory.createFrom(uri[1]);
				if (identifier != null) {
					ids.add(identifier);
				}
			}
		}
		return ids;
	}

}
