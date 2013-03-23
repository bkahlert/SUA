package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

/**
 * This factory can construct {@link IIdentifier}s.
 * 
 * @author bkahlert
 * 
 */
public class IdentifierFactory {
	/**
	 * Constructs a new {@link IIdentifier} from a string.
	 * 
	 * @param string
	 * @return null if the string does not present a valid {@link IIdentifier}.
	 */
	public static IIdentifier createFrom(String string) {
		if (ID.isValid(string)) {
			return new ID(string);
		}
		if (Fingerprint.isValid(string)) {
			return new Fingerprint(string);
		}
		return null;
	}

	public static IIdentifier createFrom(ILocatable locatable) {
		if (locatable != null && locatable.getUri() != null) {
			String[] parts = locatable.getUri().getRawPath().split("\\s+");
			if (parts.length > 2) {
				return createFrom(parts[0]);
			}
		}
		return null;
	}

}
