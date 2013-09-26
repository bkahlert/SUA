package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.impl;

import java.net.URI;
import java.util.ArrayList;
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
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.LocatableUtils;
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

	private ExecutorService executorService = new ExecutorService();

	// TODO use cache
	private Cache<URI, ILocatable> uriCache = new Cache<URI, ILocatable>(
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
					for (final ILocatorProvider locatorProvider : LocatableUtils
							.filterResponsibleLocators(uri, locatorProviders)) {
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
	public Future<ILocatable> resolve(final URI uri,
			final IProgressMonitor monitor) {
		Assert.isLegal(uri != null);
		return this.executorService.nonUIAsyncExec(new Callable<ILocatable>() {
			@Override
			public ILocatable call() throws Exception {
				return LocatorService.this.uriCache.getPayload(uri, monitor);
			}
		});
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
						List<Future<ILocatable>> locatables = new ArrayList<Future<ILocatable>>();
						for (final URI uri : uris) {
							Future<ILocatable> locatable = LocatorService.this
									.resolve(uri, subMonitor.newChild(1));
							locatables.add(locatable);
						}

						List<ILocatable> findings = new ArrayList<ILocatable>();
						for (Future<ILocatable> locatable : locatables) {
							ILocatable finding = locatable.get();
							if (finding != null) {
								findings.add(finding);
							}
						}
						return findings.toArray(new ILocatable[0]);
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

		final SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		return this.executorService.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				ILocatable[] locatables = LocatorService.this.resolve(uris,
						subMonitor.newChild(1)).get();
				boolean convertedAll = locatables.length == uris.length;
				if (locatables.length > 0) {
					boolean showedAll = LocatorService.this.showInWorkspace(
							locatables, open, subMonitor.newChild(1)).get();
					return showedAll && convertedAll;
				} else {
					subMonitor.worked(1);
					return convertedAll;
				}
			}
		});
	}

	@Override
	public Future<Boolean> showInWorkspace(ILocatable locatable, boolean open,
			IProgressMonitor monitor) {
		return this.showInWorkspace(new ILocatable[] { locatable }, open,
				monitor);
	}

	@Override
	public Future<Boolean> showInWorkspace(final ILocatable[] locatables,
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
				List<Future<Boolean>> rs = new ArrayList<Future<Boolean>>();
				for (final ILocatorProvider locatorProvider : locatorProviders) {
					Future<Boolean> future = LocatorService.this.executorService
							.nonUIAsyncExec(new Callable<Boolean>() {
								@Override
								public Boolean call() throws Exception {
									ILocatable[] suitableLocatables = LocatableUtils
											.filterSuitableLocatators(
													locatorProvider, locatables);
									if (suitableLocatables.length == 0) {
										return true;
									}

									return locatorProvider.showInWorkspace(
											suitableLocatables, open,
											subMonitor.newChild(1));
								}
							});
					rs.add(future);
				}
				for (Future<Boolean> r : rs) {
					try {
						if (!r.get()) {
							return false;
						}
					} catch (InterruptedException e) {
						LOGGER.error("Error while showing coded objects", e);
						return false;
					} catch (ExecutionException e) {
						LOGGER.error("Error while showing coded objects", e);
						return false;
					}
				}
				return true;
			}
		});
	}
}
