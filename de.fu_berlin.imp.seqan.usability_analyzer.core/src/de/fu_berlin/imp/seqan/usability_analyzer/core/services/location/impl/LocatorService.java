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

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.nebula.utils.CompletedFuture;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;

public class LocatorService implements ILocatorService {

	private static final Logger LOGGER = Logger.getLogger(LocatorService.class);
	private static final boolean LOG_FAST_RUNTIME = false;
	private static final boolean LOG_SLOW_RUNTIME = true;
	private static final int CACHE_SIZE = 500;

	private static ILocatorProvider[] locatorProviders = null;

	private static ILocatorProvider[] getRegisteredLocatorProviders() {
		if (locatorProviders == null) {
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
			locatorProviders = registeredLocatableProviders
					.toArray(new ILocatorProvider[0]);
		}
		return locatorProviders;
	}

	private List<ILocatorProvider> getFastLocatorProviders(URI uri) {
		List<ILocatorProvider> fastLocatorProviders = new ArrayList<ILocatorProvider>(
				getRegisteredLocatorProviders().length);
		for (final ILocatorProvider locatorProvider : getRegisteredLocatorProviders()) {
			if (locatorProvider.isResolvabilityImpossible(uri)) {
				continue;
			}
			if (locatorProvider.getObjectIsShortRunning(uri)) {
				fastLocatorProviders.add(locatorProvider);
			}
		}
		return fastLocatorProviders;
	}

	private List<ILocatorProvider> getSlowLocatorProviders(URI uri) {
		List<ILocatorProvider> slowLocatorProviders = new ArrayList<ILocatorProvider>(
				getRegisteredLocatorProviders().length);
		for (final ILocatorProvider locatorProvider : getRegisteredLocatorProviders()) {
			if (locatorProvider.isResolvabilityImpossible(uri)) {
				continue;
			}
			if (!locatorProvider.getObjectIsShortRunning(uri)) {
				slowLocatorProviders.add(locatorProvider);
			}
		}
		return slowLocatorProviders;
	}

	private final ExecutorUtil executorUtil = new ExecutorUtil(
			LocatorService.class);

	private final Cache<URI, ILocatable> uriCache = new Cache<URI, ILocatable>(
			new CacheFetcher<URI, ILocatable>() {

				@Override
				public ILocatable fetch(final URI uri, IProgressMonitor monitor) {
					Assert.isLegal(uri != null);

					if (getRegisteredLocatorProviders() == null
							|| getRegisteredLocatorProviders().length == 0) {
						return null;
					}

					List<ILocatorProvider> fastLocatorProviders = getFastLocatorProviders(uri);

					for (final ILocatorProvider fastLocatorProvider : fastLocatorProviders) {
						ILocatable locatable = fastLocatorProvider.getObject(
								uri, null);
						if (locatable != null) {
							return locatable;
						}
					}

					List<ILocatorProvider> slowLocatorProviders = getSlowLocatorProviders(uri);
					if (slowLocatorProviders.size() > 0
							&& ExecutorUtil.isUIThread()) {
						LOGGER.fatal("Implementation Error - Slow "
								+ URI.class.getSimpleName()
								+ " resolution in the UI thread detected!");
					}

					final SubMonitor subMonitor = SubMonitor.convert(monitor,
							slowLocatorProviders.size());
					List<Future<ILocatable>> futureLocatables = new ArrayList<Future<ILocatable>>(
							slowLocatorProviders.size());
					for (final ILocatorProvider slowLocatorProvider : slowLocatorProviders) {
						Future<ILocatable> futureLocatable = LocatorService.this.executorUtil
								.nonUIAsyncExec(new Callable<ILocatable>() {
									@Override
									public ILocatable call() throws Exception {
										return slowLocatorProvider.getObject(
												uri, subMonitor.newChild(1));
									}
								});
						futureLocatables.add(futureLocatable);
					}
					for (Future<ILocatable> futureLocatable : futureLocatables) {
						ILocatable finding = null;
						try {
							finding = futureLocatable.get();
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
			}, CACHE_SIZE);

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		Assert.isLegal(uri != null);

		if (getRegisteredLocatorProviders() == null
				|| getRegisteredLocatorProviders().length == 0) {
			return null;
		}

		List<Class<? extends ILocatable>> types = new ArrayList<Class<? extends ILocatable>>();
		for (final ILocatorProvider locatorProvider : getRegisteredLocatorProviders()) {
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

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ILocatable> Future<T> resolve(final URI uri,
			final Class<T> clazz, final IProgressMonitor monitor) {
		Assert.isLegal(uri != null);
		Assert.isLegal(clazz != null);

		@SuppressWarnings("unused")
		final long start = LOG_FAST_RUNTIME || LOG_SLOW_RUNTIME ? System
				.currentTimeMillis() : 0l;
		if (this.uriCache.isCached(uri)
				|| getSlowLocatorProviders(uri).size() == 0) {
			ILocatable locatable = LocatorService.this.uriCache.getPayload(uri,
					monitor);
			if (!clazz.isInstance(locatable)) {
				locatable = null;
			}
			if (LOG_FAST_RUNTIME) {
				System.out.println("Sync-Fetched " + uri + " within "
						+ (System.currentTimeMillis() - start) + "ms");
			}
			return new CompletedFuture<T>((T) locatable, null);
		} else {
			return this.executorUtil.nonUIAsyncExec(new Callable<T>() {
				@Override
				public T call() throws Exception {
					ILocatable locatable = LocatorService.this.uriCache
							.getPayload(uri, monitor);
					if (!clazz.isInstance(locatable)) {
						locatable = null;
					}
					if (LOG_FAST_RUNTIME) {
						System.out.println("Async-Fetched " + uri + " within "
								+ (System.currentTimeMillis() - start) + "ms");
					}
					return (T) locatable;
				}
			});
		}
	}

	private <T extends ILocatable> List<T> resolveList(final List<URI> uris,
			Class<T> clazz, SubMonitor subMonitor) throws Exception {
		List<Future<T>> futureLocatables = new ArrayList<Future<T>>();
		for (final URI uri : uris) {
			Future<T> futureLocatable = resolve(uri, clazz,
					subMonitor != null ? subMonitor.newChild(1) : null);
			futureLocatables.add(futureLocatable);
		}

		List<T> locatables = new ArrayList<T>();
		for (Future<T> fututeLocatable : futureLocatables) {
			T locatable = fututeLocatable.get();
			if (locatable != null) {
				locatables.add(locatable);
			}
		}
		return locatables;
	}

	@Override
	public Future<ILocatable[]> resolve(final URI[] uris,
			IProgressMonitor monitor) {
		Assert.isLegal(uris != null);

		boolean runFast = true;
		for (URI uri : uris) {
			if (getSlowLocatorProviders(uri).size() > 0) {
				runFast = false;
				break;
			}
		}

		if (runFast) {
			try {
				return new CompletedFuture<ILocatable[]>(resolveList(
						Arrays.asList(uris), ILocatable.class, null).toArray(
						new ILocatable[0]), null);
			} catch (Exception e) {
				return new CompletedFuture<ILocatable[]>(null, e);
			}
		} else {
			final SubMonitor subMonitor = SubMonitor.convert(monitor,
					uris.length);
			return this.executorUtil
					.nonUIAsyncExec(new Callable<ILocatable[]>() {
						@Override
						public ILocatable[] call() throws Exception {
							return resolveList(Arrays.asList(uris),
									ILocatable.class, subMonitor).toArray(
									new ILocatable[0]);
						}
					});
		}
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

		boolean runFast = true;
		for (URI uri : uris) {
			if (getSlowLocatorProviders(uri).size() > 0) {
				runFast = false;
				break;
			}
		}

		if (runFast) {
			try {
				return new CompletedFuture<List<T>>(resolveList(uris, clazz,
						null), null);
			} catch (Exception e) {
				return new CompletedFuture<List<T>>(null, e);
			}
		} else {
			final SubMonitor subMonitor = SubMonitor.convert(monitor,
					uris.size());
			return this.executorUtil.nonUIAsyncExec(new Callable<List<T>>() {
				@Override
				public List<T> call() throws Exception {
					return resolveList(uris, clazz, subMonitor);
				}
			});
		}
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
		return this.executorUtil.nonUIAsyncExec(new Callable<Boolean>() {
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
