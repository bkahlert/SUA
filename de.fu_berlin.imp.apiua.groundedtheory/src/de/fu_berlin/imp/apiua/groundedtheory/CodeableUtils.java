package de.fu_berlin.imp.apiua.groundedtheory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;

public class CodeableUtils {

	/**
	 * Tries to extract the {@link IIdentifier}s described in the
	 * {@link ICodeable}'s instance ids.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in !sg2h, since this is
	 * detected as a valid {@link IIdentifier}
	 * 
	 * @param codedObjects
	 * @return
	 */
	public static Set<IIdentifier> getIdentifiers(
			final List<ILocatable> codedObjects) {
		Set<IIdentifier> ids = new HashSet<IIdentifier>();
		for (ILocatable codeable : codedObjects) {
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
