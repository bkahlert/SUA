package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class CodeableUtils {

	/**
	 * Tries to extract the {@link ID}s described in the {@link ICodeable}'s
	 * instance ids.
	 * <p>
	 * e.g. sua://diff/sg2h/resource/abc will result in sg2h, since this is
	 * detected as a valid {@link ID}
	 * 
	 * @param codedObjects
	 * @return
	 */
	public static Set<ID> getIDs(final List<ICodeable> codedObjects) {
		Set<ID> ids = new HashSet<ID>();
		for (ICodeable codeable : codedObjects) {
			String[] uri = codeable.getUri().getRawPath().split("/");
			if (uri.length > 1 && ID.isValid(uri[1]))
				ids.add(new ID(uri[1]));
		}
		return ids;
	}

	/**
	 * Tries to extract the {@link Fingerprint}s described in the
	 * {@link ICodeable}'s instance ids.
	 * <p>
	 * e.g. sua://diff/!hh2x/resource/abc will result in hh2x, since this is
	 * detected as a valid {@link Fingerprint}
	 * 
	 * @param codedObjects
	 * @return
	 */
	public static Set<Fingerprint> getFingerprints(
			final List<ICodeable> codedObjects) {
		Set<Fingerprint> fingerprints = new HashSet<Fingerprint>();
		for (ICodeable codeable : codedObjects) {
			String[] uri = codeable.getUri().getRawPath().split("/");
			if (uri.length > 1 && Fingerprint.isValid(uri[1]))
				fingerprints.add(new Fingerprint(uri[1]));
		}
		return fingerprints;
	}

	/**
	 * Tries to extract the {@link ID}s and {@link Fingerprint}s described in
	 * the {@link ICodeable}'s instance ids.
	 * <p>
	 * e.g. sua://diff/!hh2x/resource/abc will result in hh2x, since this is
	 * detected as a valid {@link Fingerprint}
	 * 
	 * @param codedObjects
	 * @return
	 */
	public static Set<Object> getKeys(final List<ICodeable> codedObjects) {
		Set<Object> idsAndfingerprints = new HashSet<Object>();
		idsAndfingerprints.addAll(getIDs(codedObjects));
		idsAndfingerprints.addAll(getFingerprints(codedObjects));
		return idsAndfingerprints;
	}
}
