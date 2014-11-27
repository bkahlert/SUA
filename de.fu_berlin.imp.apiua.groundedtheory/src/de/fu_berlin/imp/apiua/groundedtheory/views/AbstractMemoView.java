package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.KeyboardUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.history.History;
import com.bkahlert.nebula.utils.history.IHistory;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.nebula.widgets.browser.listener.BrowserOpeningAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.SchemeAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.URIAdapter;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.composer.IAnkerLabelProvider;
import com.bkahlert.nebula.widgets.editor.Editor;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.IHighlightService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.core.views.UriPresentingEditorView;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.ui.UriPartRenamerConverter;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.ViewerURI;

public class AbstractMemoView extends UriPresentingEditorView {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractMemoView.class);

	private static final UriPartRenamerConverter CONVERTER = new UriPartRenamerConverter();

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			for (URI uri : AbstractMemoView.this.getLoadedObjects()) {
				if (uris.contains(uri)) {
					AbstractMemoView.this.refreshHeader();
				}
			}
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			for (URI uri : AbstractMemoView.this.getLoadedObjects()) {
				if (uris.contains(uri)) {
					AbstractMemoView.this.refreshHeader();
				}
			}
		};

		private void reloadIfNecessary(URI uri) {
			if (AbstractMemoView.this.getLoadedObjects().contains(uri)) {
				AbstractMemoView.this.refreshHeader();
			}
		}

		@Override
		public void memoAdded(URI uri) {
			this.reloadIfNecessary(uri);
		};

		@Override
		public void memoModified(URI uri) {
			this.reloadIfNecessary(uri);
		};

		@Override
		public void memoRemoved(URI uri) {
			this.reloadIfNecessary(uri);
		};
	};

	private final IHighlightService highlightService = (IHighlightService) PlatformUI
			.getWorkbench().getService(IHighlightService.class);

	private final List<IHistory<URI>> history;

	public AbstractMemoView() {
		super(CONVERTER, 2000, ToolbarSet.DEFAULT, true);
		this.history = new ArrayList<IHistory<URI>>();
	}

	@Override
	public void postInit() {
		super.postInit();
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void created(List<Editor<URI>> editors) {
		super.created(editors);
		this.addAnkerLabelProviders(editors);
		this.addAnkerListeners(editors);
	}

	private void addAnkerLabelProviders(List<Editor<URI>> editors) {
		for (Editor<URI> editor : editors) {
			editor.addAnkerLabelProvider(new IAnkerLabelProvider() {
				@Override
				public boolean isResponsible(IAnker anker) {
					if (anker.getHref() != null) {
						try {
							URI uri = new URI(anker.getHref());
							Future<ILocatable> locatable = LocatorService.INSTANCE
									.resolve(uri, null);
							if (locatable.get() != null) {
								return true;
							}
						} catch (Exception e) {
							if (!URISyntaxException.class.isInstance(e
									.getCause())) {
								LOGGER.error(
										"Error handling " + anker.getHref(), e);
							}
						}
					}
					return false;
				}

				@Override
				public String getHref(IAnker anker) {
					return anker.getHref();
				}

				@Override
				public String[] getClasses(IAnker anker) {
					return new String[] { "special" };
				}

				@Override
				public String getContent(IAnker anker) {
					if (anker.getHref() != null) {
						try {
							URI uri = new URI(anker.getHref());
							ILabelProvider labelProvider = AbstractMemoView.this.labelProviderService
									.getLabelProvider(uri);
							if (labelProvider != null) {
								return labelProvider.getText(uri);
							}
						} catch (URISyntaxException e) {

						} catch (Exception e) {
							LOGGER.error(e);
						}
					}
					return "!!! " + anker.getHref() + " !!!";
				}
			});
		}
	}

	private void addAnkerListeners(List<Editor<URI>> editors) {
		for (int i = 0; i < editors.size(); i++) {
			Editor<URI> editor = editors.get(i);
			final IHistory<URI> history = this.history.get(i);

			Map<String, IAnkerListener> listeners = new HashMap<String, IAnkerListener>();
			listeners.put(SUACorePreferences.URI_SCHEME,
					new AnkerAdaptingListener(new URIAdapter() {
						@Override
						public void uriClicked(final java.net.URI nativeUri) {
							try {
								final URI uri = new URI(nativeUri);
								if (!KeyboardUtils.isMetaKeyPressed()) {
									// treat link as a typical link that opens a
									// resource
									history.add(uri);
									AbstractMemoView.this.updateNavigation();
									AbstractMemoView.this.load(null, uri);
								} else {
									final ILocatable locatable = LocatorService.INSTANCE
											.resolve(uri, null).get();

									// do not follow the link but make Eclipse
									// open the
									// resource
									HasDateRange range = (HasDateRange) Platform
											.getAdapterManager().getAdapter(
													locatable,
													HasDateRange.class);
									if (range != null) {
										AbstractMemoView.this.highlightService
												.highlight(
														AbstractMemoView.this,
														range.getDateRange()
																.getCalendarRange(),
														true);
									}

									// open element
									if (!LocatorService.INSTANCE
											.showInWorkspace(
													uri,
													KeyboardUtils
															.isMetaKeyPressed(),
													null).get()) {
										ExecUtils.asyncExec(() -> MessageDialog
												.openInformation(
														PlatformUI
																.getWorkbench()
																.getActiveWorkbenchWindow()
																.getShell(),
														"Artefact not found",
														"The artefact "
																+ uri.toString()
																+ " could not be found."));
									}
								}
							} catch (InterruptedException e) {
								LOGGER.error(e);
							} catch (ExecutionException e) {
								LOGGER.error(e);
							}
						}
					}));
			editor.addAnkerListener(new SchemeAnkerListener(listeners,
					new BrowserOpeningAnkerListener()));
		}
	};

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		super.dispose();
	}

	// TODO PropertyChangeService implementieren; header wird nicht
	// aktualisiert, wenn sich der compilation state verändert hat, da keine
	// Abhängigkeit zum Diff-Plugin eingeführt werden kann (sonst: zyklische
	// Abhängigkeit).

	public void setSourceMode(boolean on) {
		if (on) {
			for (Editor<URI> editor : this.getEditors()) {
				editor.showSource();
			}
		} else {
			for (Editor<URI> editor : this.getEditors()) {
				editor.hideSource();
			}
		}
	}

	@Override
	public String getTitle(URI uri, IProgressMonitor monitor) throws Exception {
		return CONVERTER.convert(uri).getFirst();
	}

	@Override
	public String getHtml(URI uri, IProgressMonitor monitor) {
		return this.codeService.loadMemo(uri);
	}

	@Override
	public void setHtml(URI uri, String html, IProgressMonitor monitor) {
		try {
			this.codeService.setMemo(uri, html);
		} catch (CodeServiceException e) {
			LOGGER.error("Can't save memo for " + uri, e);
		}
	}

	public void loadAndClearHistory(final Runnable callback, URI... uris) {
		final List<URI> highlight = new ArrayList<URI>();
		final List<URI> highlightSpecial = new ArrayList<URI>();
		final List<URI> toOpen = new ArrayList<URI>();
		for (URI uri : uris) {
			if (uri instanceof ViewerURI) {
				continue;
			}

			// if a codeInstance is opened, open its reference instead but
			// highlight the original codeInstance
			if (LocatorService.INSTANCE.getType(uri) == ICodeInstance.class) {
				try {
					ICodeInstance codeInstance = LocatorService.INSTANCE
							.resolve(uri, ICodeInstance.class, null).get();
					if (codeInstance != null) {
						uri = codeInstance.getId();
						highlightSpecial.add(codeInstance.getUri());
					}
				} catch (Exception e) {
					LOGGER.error("Error checking where " + uri + " points to");
				}
			}

			toOpen.add(uri);

			// open all related instances's memos
			for (ICodeInstance codeInstance : this.codeService
					.getInstances(uri)) {
				if (!highlightSpecial.contains(codeInstance.getUri())) {
					highlight.add(codeInstance.getUri());
				}
				toOpen.add(codeInstance.getUri());
			}
		}
		while (this.history.size() > toOpen.size()) {
			this.history.remove(this.history.size() - 1);
		}
		while (this.history.size() < toOpen.size()) {
			this.history.add(new History<URI>());
		}
		for (int i = 0; i < toOpen.size(); i++) {
			this.history.get(i).clear();
			this.history.get(i).add(toOpen.get(i));
		}
		this.updateNavigation();
		this.load(() -> {
			for (Editor<URI> editor : AbstractMemoView.this.getEditors()) {
				URI uri = editor.getLoadedObject();
				RGB rgb = null;
				if (highlightSpecial.contains(uri)) {
					rgb = RGB.WARNING;
				} else if (highlight.contains(uri)) {
					rgb = RGB.INFO;
				}
				if (!editor.isDisposed()) {
					editor.setBackground(rgb);
				}
			}
			if (callback != null) {
				callback.run();
			}
		}, toOpen.toArray(new URI[0]));
	}

	protected void updateNavigation() {
		IEvaluationService evaluationService = (IEvaluationService) this
				.getSite().getService(IEvaluationService.class);
		evaluationService
				.requestEvaluation("de.fu_berlin.imp.apiua.groundedtheory.canNavigateBack");
		evaluationService
				.requestEvaluation("de.fu_berlin.imp.apiua.groundedtheory.canNavigateForward");
	}

	public boolean canNavigateBack() {
		for (IHistory<URI> history : this.history) {
			if (history.hasPrev()) {
				return true;
			}
		}
		return false;
	}

	public void navigateBack() {
		for (int i = 0; i < this.history.size(); i++) {
			if (this.history.get(i).hasPrev()) {
				this.load(null, this.history.get(i).back());
				this.updateNavigation();
			}
		}
	}

	public boolean canNavigateForward() {
		for (IHistory<URI> history : this.history) {
			if (history.hasNext()) {
				return true;
			}
		}
		return false;
	}

	public void navigateForward() {
		for (int i = 0; i < this.history.size(); i++) {
			if (this.history.get(i).hasNext()) {
				this.load(null, this.history.get(i).forward());
				this.updateNavigation();
			}
		}
	}
}
