package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public abstract class AdaptingLocatorProvider implements ILocatorProvider,
		IAdapterFactory {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AdaptingLocatorProvider.class);

	private final Class<? extends ILocatable>[] classes;

	public AdaptingLocatorProvider(Class<? extends ILocatable>... classes) {
		this.classes = classes;
		Platform.getAdapterManager().registerAdapters(this, URI.class);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return classes;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof URI) {
			final URI uri = (URI) adaptableObject;
			if (ArrayUtils.contains(classes, adapterType)) {
				return getObject(uri, null);
			}
			return null;
		}
		return null;
	}
}
