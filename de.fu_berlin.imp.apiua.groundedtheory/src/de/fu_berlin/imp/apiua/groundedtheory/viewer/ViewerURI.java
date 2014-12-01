package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.HashSet;
import java.util.Set;

import de.fu_berlin.imp.apiua.core.model.URI;

/**
 * Used to enrich an URI with meta information used for display purposes only.
 *
 * @author bkahlert
 *
 */
public class ViewerURI extends URI {

	private static final long serialVersionUID = 5238025920625242523L;

	public static final ViewerURI NO_CODES_URI = new ViewerURI(new URI(
			"nocodes"));

	public static final ViewerURI NO_RELATIONS_URI = new ViewerURI(new URI(
			"norelations"));

	public static final ViewerURI NO_PHENOMENONS_URI = new ViewerURI(new URI(
			"nophenomenons"));

	private Set<String> flags = new HashSet<>();

	public ViewerURI(URI uri) {
		super(uri.getRawURI());
	}

	public ViewerURI setFlag(String flag, boolean value) {
		if (this.flags.contains(flag)) {
			if (!value) {
				this.flags.remove(flag);
			}
		} else {
			if (value) {
				this.flags.add(flag);
			}
		}
		return this;
	}

	public boolean getFlag(String flag) {
		return this.flags.contains(flag);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
