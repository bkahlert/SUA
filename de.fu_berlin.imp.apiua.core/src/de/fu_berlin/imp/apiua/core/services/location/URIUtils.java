package de.fu_berlin.imp.apiua.core.services.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;

import com.bkahlert.nebula.utils.HashUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;

public class URIUtils {

	/**
	 * Tries to extract the resource from the given {@link URI}.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in diff
	 *
	 * @param uri
	 * @return
	 */
	public static String getResource(URI uri) {
		return uri != null ? uri.getHost() : null;
	}

	/**
	 * Tries to extract the resources from the given {@link URI}s.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in diff
	 *
	 * @param uris
	 * @return
	 */
	public static Set<String> getResources(URI[] uris) {
		Set<String> resources = new HashSet<String>();
		if (uris == null) {
			uris = new URI[0];
		}
		for (URI uri : uris) {
			String resource = getResource(uri);
			if (resource != null) {
				resources.add(resource);
			}
		}
		return resources;
	}

	/**
	 * Tries to extract the {@link IIdentifier} from the given {@link URI}.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in !sg2h, since this is
	 * detected as a valid {@link IIdentifier}
	 *
	 * @param uri
	 * @return
	 */
	public static IIdentifier getIdentifier(URI uri) {
		String[] parts = uri != null ? uri.getRawPath().split("/")
				: new String[0];

		if (parts.length > 1) {
			IIdentifier identifier;
			// FIXME apiua://survey/cd/id should be of the format
			// apiua://survey-cd/id
			if ("cd".equals(parts[1])) {
				identifier = IdentifierFactory.createFrom(HashUtils
						.md5(parts[2]));
			} else {
				identifier = IdentifierFactory.createFrom(parts[1]);
			}
			if (identifier != null) {
				return identifier;
			}
		}

		return null;
	}

	/**
	 * Tries to extract the distinct {@link IIdentifier}s from the given
	 * {@link URI}s.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in !sg2h, since this is
	 * detected as a valid {@link IIdentifier}
	 *
	 * @param uris
	 * @return
	 */
	public static Set<IIdentifier> getIdentifiers(URI[] uris) {
		Set<IIdentifier> ids = new HashSet<IIdentifier>();
		if (uris == null) {
			uris = new URI[0];
		}
		for (URI uri : uris) {
			IIdentifier identifier = getIdentifier(uri);
			if (identifier != null) {
				ids.add(identifier);
			}
		}
		return ids;
	}

	/**
	 * Tries to extract the trail from the given {@link URI}. The trail is the
	 * part following the resource and {@link IIdentifier}.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in a list with the
	 * elements "resource" and "abc"
	 *
	 * @param uri
	 * @return an empty list if no trail is found
	 */
	public static List<String> getTrail(URI uri) {
		if (uri == null) {
			return new LinkedList<String>();
		}
		return Arrays.asList((String[]) ArrayUtils.subarray(uri.getRawPath()
				.split("/"), 2, Integer.MAX_VALUE));
	}

	/**
	 * Tries to extract the trails from the given {@link URI}s. The trail is the
	 * part following the resource and {@link IIdentifier}.
	 * <p>
	 * e.g. apiua://diff/!sg2h/resource/abc will result in a list with the
	 * elements "resource" and "abc"
	 *
	 * @param uri
	 * @return an empty list if no trail is found
	 */
	public static Set<List<String>> getTrails(URI[] uris) {
		Set<List<String>> trails = new HashSet<List<String>>();
		if (uris == null) {
			uris = new URI[0];
		}
		for (URI uri : uris) {
			List<String> trail = getTrail(uri);
			if (trail != null && trail.size() > 0) {
				trails.add(trail);
			}
		}
		return trails;
	}

	public static URI adapt(Object element) {
		if (element instanceof URI) {
			return (URI) element;
		}
		if (element instanceof ILocatable) {
			return ((ILocatable) element).getUri();
		}
		URI uri = (URI) Platform.getAdapterManager().getAdapter(element,
				URI.class);
		return uri;
	}

	/**
	 * Filters the given {@link URI}s by the given resources.
	 *
	 * @param uris
	 * @param resource
	 *            a value of null and an empty string are treated equally (no
	 *            resource in {@link URI})
	 * @return
	 */
	public static List<URI> filterByResource(List<URI> uris, String[] resources) {
		List<URI> filtered = new ArrayList<URI>();
		if (resources == null) {
			resources = new String[] { null };
		}
		for (int i = 0, m = resources.length; i < m; i++) {
			if ("".equals(resources[i])) {
				resources[i] = null;
			}
		}
		if (uris == null) {
			uris = new LinkedList<URI>();
		}
		for (URI uri : uris) {
			for (int j = 0, n = resources.length; j < n; j++) {
				if (StringUtils.equals(resources[j], getResource(uri))) {
					filtered.add(uri);
				}
			}
		}
		return filtered;
	}

	/**
	 * Filters the given {@link URI}s by the given resource.
	 *
	 * @param uris
	 * @param resource
	 *            a value of null and an empty string are treated equally (no
	 *            resource in {@link URI})
	 * @return
	 */
	public static List<URI> filterByResource(List<URI> uris, String resource) {
		return filterByResource(uris, new String[] { resource });
	}

	/**
	 * Filters the given {@link URI}s by the given resources.
	 *
	 * @param uris
	 * @param resource
	 *            a value of null and an empty string are treated equally (no
	 *            resource in {@link URI})
	 * @return
	 */
	public static URI[] filterByResource(URI[] uris, String[] resources) {
		if (uris == null) {
			return new URI[0];
		}
		return filterByResource(Arrays.asList(uris), resources).toArray(
				new URI[0]);
	}

	/**
	 * Filters the given {@link URI}s by the given resource.
	 *
	 * @param uris
	 * @param resource
	 *            a value of null and an empty string are treated equally (no
	 *            resource in {@link URI})
	 * @return
	 */
	public static URI[] filterByResource(URI[] uris, String resource) {
		return filterByResource(uris, new String[] { resource });
	}

	/**
	 * Tries to adapt all element of the given {@link ISelection} to an
	 * {@link URI} and returns the ones with the given resource.
	 *
	 * @param selection
	 * @param resource
	 *            a value of null and an empty string are treated equally (no
	 *            resource in {@link URI})
	 * @return
	 */
	public static URI[] filterByResource(ISelection selection, String resource) {
		URI[] uris = SelectionUtils.getAdaptableObjects(selection, URI.class)
				.toArray(new URI[0]);
		return filterByResource(uris, resource);
	}

	/**
	 * Tries to adapt all element of the given {@link ISelection} to an
	 * {@link URI} and returns the ones with the given resources.
	 *
	 * @param selection
	 * @param resource
	 *            a value of null and an empty string are treated equally (no
	 *            resource in {@link URI})
	 * @return
	 */
	public static URI[] filterByResource(ISelection selection,
			String[] resources) {
		URI[] uris = SelectionUtils.getAdaptableObjects(selection, URI.class)
				.toArray(new URI[0]);
		return filterByResource(uris, resources);
	}

}
