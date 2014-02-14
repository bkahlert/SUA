package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location;

import java.net.URI;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.nebula.utils.AdapterFactoryProxy;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IOpenable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IRevealableInOS;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public abstract class AdaptingLocatorProvider implements ILocatorProvider,
		IAdapterFactory {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AdaptingLocatorProvider.class);

	public static final Class<?>[] SHARED_ADAPTERS = new Class<?>[] {
			ILocatable.class, HasDateRange.class, IOpenable.class,
			IRevealableInOS.class, HasIdentifier.class };

	/**
	 * Eclipse's default {@link IAdapterManager} does only consult one
	 * {@link IAdapterFactory}. Therefore we use {@link AdapterFactoryProxy}.
	 */
	private static AdapterFactoryProxy<URI> adapterFactoryProxy = new AdapterFactoryProxy<URI>(
			URI.class, SHARED_ADAPTERS);

	private final Class<? extends ILocatable>[] classes;

	/**
	 * 
	 * @param classes
	 *            that can be returned by {@link #getType(URI)} and all possible
	 *            types of the objects returned by
	 *            {@link #getObject(URI, IProgressMonitor)}. All given types may
	 *            be used by Eclipse's adapter facility to adapt {@link URI} to
	 *            one of the types using
	 *            {@link #getObject(URI, IProgressMonitor)}.
	 */
	public AdaptingLocatorProvider(Class<? extends ILocatable>... classes) {
		this.classes = classes;
		adapterFactoryProxy.registerAdapters(this);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		System.err.println("LOOKUP");
		return this.classes;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof URI) {
			final URI uri = (URI) adaptableObject;
			if (uri.toString().contains("diff")
					&& adapterType == HasDateRange.class
					&& this.getClass().getSimpleName().contains("Diff")) {
				System.err.println("OK");
			}
			if (ArrayUtils.contains(SHARED_ADAPTERS, adapterType)
					|| ArrayUtils.contains(this.classes, adapterType)) {
				ILocatable locatable = this.getObject(uri, null);
				if (adapterType.isInstance(locatable)) {
					return locatable;
				}
			}
			return null;
		}
		return null;
	}
}
