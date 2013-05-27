package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

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
		for (ILocatable locatable : locatables) {
			String[] uri = locatable.getUri().getRawPath().split("/");

			if (uri.length > 1) {
				IIdentifier identifier = IdentifierFactory.createFrom(uri[1]);
				if (identifier != null) {
					ids.add(identifier);
				}
			}
		}
		return ids;
	}

	public static ILocatorProvider[] filterResponsibleLocators(
			ILocatable locatable, ILocatorProvider[] locatorProviders) {
		return filterResponsibleLocators(locatable.getUri(), locatorProviders);
	}

	public static ILocatorProvider[] filterResponsibleLocators(URI uri,
			ILocatorProvider[] locatorProviders) {
		List<ILocatorProvider> responsibleLocators = new ArrayList<ILocatorProvider>();
		for (ILocatorProvider locatorProvider : locatorProviders) {
			String[] allowedNamespaces = locatorProvider.getAllowedNamespaces();
			if (allowedNamespaces == null
					|| ArrayUtils.contains(allowedNamespaces, uri.getHost())) {
				responsibleLocators.add(locatorProvider);
			}
		}
		return responsibleLocators.toArray(new ILocatorProvider[0]);
	}

	public static ILocatable[] filterSuitableLocatators(
			ILocatorProvider locatorProvider, ILocatable[] locatables) {
		String[] allowedNamespaces = locatorProvider.getAllowedNamespaces();
		if (allowedNamespaces == null) {
			return locatables;
		}

		List<ILocatable> suitableLocatables = new ArrayList<ILocatable>();
		for (ILocatable locatable : locatables) {
			if (ArrayUtils.contains(allowedNamespaces, locatable.getUri()
					.getHost())) {
				suitableLocatables.add(locatable);
			}
		}
		return suitableLocatables.toArray(new ILocatable[0]);

	}

	public static URI[] filterSuitableLocatators(
			ILocatorProvider locatorProvider, URI[] uris) {
		final List<ILocatable> locatables = new ArrayList<ILocatable>();
		for (final URI uri : uris) {
			locatables.add(new ILocatable() {
				private static final long serialVersionUID = 1L;

				@Override
				public URI getUri() {
					return uri;
				}
			});
		}
		ILocatable[] suitableLocatables = filterSuitableLocatators(
				locatorProvider, locatables.toArray(new ILocatable[0]));
		List<URI> suitableUris = new ArrayList<URI>();
		for (ILocatable suitableLocatable : suitableLocatables) {
			suitableUris.add(suitableLocatable.getUri());
		}
		return suitableUris.toArray(new URI[0]);
	}

}
