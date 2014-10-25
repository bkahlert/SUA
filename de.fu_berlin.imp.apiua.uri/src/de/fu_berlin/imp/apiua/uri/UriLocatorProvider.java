package de.fu_berlin.imp.apiua.uri;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.util.Cache;
import de.fu_berlin.imp.apiua.core.util.Cache.CacheFetcher;
import de.fu_berlin.imp.apiua.uri.model.IUri;
import de.fu_berlin.imp.apiua.uri.services.IUriService;
import de.fu_berlin.imp.apiua.uri.views.UriView;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;

public class UriLocatorProvider extends AdaptingLocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(UriLocatorProvider.class);

	private final IUriService uriService = (IUriService) PlatformUI
			.getWorkbench().getService(IUriService.class);

	private final Cache<URI, ILocatable> cache = new Cache<URI, ILocatable>(
			new CacheFetcher<URI, ILocatable>() {
				@Override
				public ILocatable fetch(URI uri,
						IProgressMonitor progressMonitor) {
					for (IUri iuri : UriLocatorProvider.this.uriService.getUris()) {
						if (iuri.getUri().equals(uri)) {
							return iuri;
						}
					}
					return null;
				};
			}, 1000);

	@SuppressWarnings("unchecked")
	public UriLocatorProvider() {
		super(IUri.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return this.cache.getPayload(uri, null) == null;
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return IUri.class;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		return true;
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		return this.cache.getPayload(uri, monitor);
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);
		final ILocatable[] locatables;
		try {
			locatables = locatorService.resolve(uris, subMonitor.newChild(1))
					.get();
		} catch (Exception e) {
			LOGGER.error("Error resolving " + uris);
			return false;
		}

		if (locatables.length > 0) {
			final UriView uriView = (UriView) WorkbenchUtils
					.getView(UriView.ID);
			if (uriView == null) {
				return false;
			}

			URI[] selection;
			try {
				selection = ExecUtils.syncExec(new Callable<URI[]>() {
					@Override
					public URI[] call() throws Exception {
						return uriView.select(locatables);
					}
				});
			} catch (Exception e) {
				LOGGER.error("Error selecting " + uris);
				return false;
			}

			return selection.length == uris.length;
		}

		return true;
	}

}
