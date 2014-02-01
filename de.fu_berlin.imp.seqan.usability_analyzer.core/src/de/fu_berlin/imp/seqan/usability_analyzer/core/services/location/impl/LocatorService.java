package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorService;
import com.bkahlert.nebula.utils.CompletedFuture;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;

public class LocatorService implements ILocatorService {

	private static final Logger LOGGER = Logger.getLogger(LocatorService.class);

	private static ILocatorProvider[] getRegisteredLocatorProviders() {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.core.locatorprovider");
		List<ILocatorProvider> registeredLocatableProviders = new ArrayList<ILocatorProvider>();
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof ILocatorProvider) {
					registeredLocatableProviders.add((ILocatorProvider) o);
				}
			} catch (CoreException e1) {
				LOGGER.error("Error retrieving a currently registered "
						+ ILocatorProvider.class.getSimpleName(), e1);
				return null;
			}
		}
		return registeredLocatableProviders.toArray(new ILocatorProvider[0]);
	}

	private final ExecutorService executorService = new ExecutorService();

	// TODO use cache
	private final Cache<URI, ILocatable> uriCache = new Cache<URI, ILocatable>(
			new CacheFetcher<URI, ILocatable>() {
				@Override
				public ILocatable fetch(final URI uri, IProgressMonitor monitor) {
					Assert.isLegal(uri != null);

					final ILocatorProvider[] locatorProviders = getRegisteredLocatorProviders();
					if (locatorProviders == null
							|| locatorProviders.length == 0) {
						return null;
					}

					final SubMonitor subMonitor = SubMonitor.convert(monitor,
							locatorProviders.length);
					List<Future<ILocatable>> locatables = new ArrayList<Future<ILocatable>>();
					for (final ILocatorProvider locatorProvider : locatorProviders) {
						if (locatorProvider.isResolvabilityImpossible(uri)) {
							continue;
						}

						Future<ILocatable> locatable = LocatorService.this.executorService
								.nonUIAsyncExec(new Callable<ILocatable>() {
									@Override
									public ILocatable call() throws Exception {
										return locatorProvider.getObject(uri,
												subMonitor.newChild(1));
									}
								});
						locatables.add(locatable);
					}
					for (Future<ILocatable> locatable : locatables) {
						ILocatable finding = null;
						try {
							finding = locatable.get();
						} catch (InterruptedException e) {
							LOGGER.error(
									"Error while resolving "
											+ URI.class.getSimpleName() + " "
											+ uri + " to "
											+ ILocatable.class.getSimpleName(),
									e);
						} catch (ExecutionException e) {
							LOGGER.error(
									"Error while resolving "
											+ URI.class.getSimpleName() + " "
											+ uri + " to "
											+ ILocatable.class.getSimpleName(),
									e);
						}
						if (finding != null) {
							return finding;
						}
					}
					return null;
				}
			}, 200);

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		Assert.isLegal(uri != null);

		final ILocatorProvider[] locatorProviders = getRegisteredLocatorProviders();
		if (locatorProviders == null || locatorProviders.length == 0) {
			return null;
		}

		List<Class<? extends ILocatable>> types = new ArrayList<Class<? extends ILocatable>>();
		for (final ILocatorProvider locatorProvider : locatorProviders) {
			if (locatorProvider.isResolvabilityImpossible(uri)) {
				continue;
			}

			Class<? extends ILocatable> type = locatorProvider.getType(uri);
			if (type != null) {
				types.add(type);
			}
		}

		if (types.size() > 1) {
			LOGGER.warn(types.size() + " possible types found for " + uri);
		}
		return types.size() == 0 ? null : types.get(0);
	}

	@Override
	public Future<ILocatable> resolve(final URI uri,
			final IProgressMonitor monitor) {
		return resolve(uri, ILocatable.class, monitor);
	}

	@Override
	public <T extends ILocatable> Future<T> resolve(final URI uri,
			final Class<T> clazz, final IProgressMonitor monitor) {
		Assert.isLegal(uri != null);
		Assert.isLegal(clazz != null);
		return this.executorService.nonUIAsyncExec(new Callable<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T call() throws Exception {
				ILocatable locatable = LocatorService.this.uriCache.getPayload(
						uri, monitor);
				if (clazz.isInstance(locatable)) {
					return (T) locatable;
				}
				return null;
			}
		});
	}

	private <T extends ILocatable> List<T> resolveList(final List<URI> uris,
			Class<T> clazz, SubMonitor subMonitor) throws Exception {
		List<Future<T>> locatables = new ArrayList<Future<T>>();
		for (final URI uri : uris) {
			Future<T> locatable = LocatorService.this.resolve(uri, clazz,
					subMonitor.newChild(1));
			locatables.add(locatable);
		}

		List<T> findings = new ArrayList<T>();
		for (Future<T> locatable : locatables) {
			T finding = locatable.get();
			if (finding != null) {
				findings.add(finding);
			}
		}
		return findings;
	}

	@Override
	public Future<ILocatable[]> resolve(final URI[] uris,
			IProgressMonitor monitor) {
		Assert.isLegal(uris != null);

		final SubMonitor subMonitor = SubMonitor.convert(monitor, uris.length);
		return this.executorService
				.nonUIAsyncExec(new Callable<ILocatable[]>() {
					@Override
					public ILocatable[] call() throws Exception {
						return resolveList(Arrays.asList(uris),
								ILocatable.class, subMonitor).toArray(
								new ILocatable[0]);
					}
				});
	}

	@Override
	public Future<List<ILocatable>> resolve(final List<URI> uris,
			IProgressMonitor monitor) {
		return resolve(uris, ILocatable.class, monitor);
	}

	@Override
	public <T extends ILocatable> Future<List<T>> resolve(final List<URI> uris,
			final Class<T> clazz, IProgressMonitor monitor) {
		Assert.isLegal(uris != null);

		final SubMonitor subMonitor = SubMonitor.convert(monitor, uris.size());
		return this.executorService.nonUIAsyncExec(new Callable<List<T>>() {
			@Override
			public List<T> call() throws Exception {
				return resolveList(uris, clazz, subMonitor);
			}
		});
	}

	@Override
	public void uncache(URI uri) {
		this.uncache(new URI[] { uri });
	}

	@Override
	public void uncache(URI[] uris) {
		for (URI uri : uris) {
			this.uriCache.removeKey(uri);
		}
	}

	@Override
	public Future<Boolean> showInWorkspace(URI uri, boolean open,
			IProgressMonitor monitor) {
		return this.showInWorkspace(new URI[] { uri }, open, monitor);
	}

	@Override
	public Future<Boolean> showInWorkspace(final URI[] uris,
			final boolean open, IProgressMonitor monitor) {
		final ILocatorProvider[] locatorProviders = getRegisteredLocatorProviders();
		if (locatorProviders == null || locatorProviders.length == 0) {
			return new CompletedFuture<Boolean>(true, null);
		}

		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				locatorProviders.length);
		return this.executorService.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean success = true;
				for (ILocatorProvider locatorProvider : locatorProviders) {
					List<URI> filtered = new ArrayList<URI>();
					for (URI uri : uris) {
						if (!locatorProvider.isResolvabilityImpossible(uri)) {
							filtered.add(uri);
						}
					}
					if (filtered.size() == 0) {
						subMonitor.worked(1);
					} else if (!locatorProvider.showInWorkspace(
							filtered.toArray(new URI[0]), open,
							subMonitor.newChild(1))) {
						success = false;
					}
				}
				return success;
			}
		});
	}
}
