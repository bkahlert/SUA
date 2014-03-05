package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.net.URISyntaxException;
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

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.views.UriPresentingEditorView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class AbstractMemoView extends UriPresentingEditorView {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractMemoView.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			if (uris.contains(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			if (uris.contains(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		};

		private void reloadIfNecessary(URI uri) {
			if (uri.equals(AbstractMemoView.this.getLoadedObject())) {
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

	private final IHistory<URI> history;

	public AbstractMemoView() {
		super(2000, ToolbarSet.DEFAULT, true);
		this.history = new History<URI>();
	}

	@Override
	public void postInit() {
		super.postInit();
		this.addAnkerLabelProviders();
		this.addAnkerListeners();
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	private void addAnkerLabelProviders() {
		this.getEditor().addAnkerLabelProvider(new IAnkerLabelProvider() {
			@Override
			public boolean isResponsible(IAnker anker) {
				if (anker.getHref() != null) {
					try {
						URI uri = new URI(anker.getHref());
						Future<ILocatable> locatable = AbstractMemoView.this.locatorService
								.resolve(uri, null);
						if (locatable.get() != null) {
							return true;
						}
					} catch (Exception e) {
						LOGGER.error("Error handling " + anker.getHref(), e);
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

	private void addAnkerListeners() {
		Map<String, IAnkerListener> listeners = new HashMap<String, IAnkerListener>();
		listeners.put("SUA", new AnkerAdaptingListener(new URIAdapter() {
			@Override
			public void uriClicked(final java.net.URI nativeUri) {
				try {
					final URI uri = new URI(nativeUri);
					if (!KeyboardUtils.isMetaKeyPressed()) {
						// treat link as a typical link that opens a resource
						AbstractMemoView.this.history.add(uri);
						AbstractMemoView.this.updateNavigation();
						AbstractMemoView.this.load(uri);
					} else {
						final ILocatable locatable = AbstractMemoView.this.locatorService
								.resolve(uri, null).get();

						// do not follow the link but make Eclipse open the
						// resource
						HasDateRange range = (HasDateRange) Platform
								.getAdapterManager().getAdapter(locatable,
										HasDateRange.class);
						if (range != null) {
							AbstractMemoView.this.highlightService.highlight(
									AbstractMemoView.this, range.getDateRange()
											.getCalendarRange(), true);
						}

						// open element
						if (!AbstractMemoView.this.locatorService
								.showInWorkspace(uri,
										KeyboardUtils.isMetaKeyPressed(), null)
								.get()) {
							ExecUtils.asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(PlatformUI
											.getWorkbench()
											.getActiveWorkbenchWindow()
											.getShell(), "Artefact not found",
											"The artefact " + uri.toString()
													+ " could not be found.");
								}
							});
						}
					}
				} catch (InterruptedException e) {
					LOGGER.error(e);
				} catch (ExecutionException e) {
					LOGGER.error(e);
				}
			}
		}));
		this.getEditor().addAnkerListener(
				new SchemeAnkerListener(listeners,
						new BrowserOpeningAnkerListener()));
	};

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		super.dispose();
	}

	@Override
	public PartInfo getDefaultPartInfo() {
		return new PartInfo("No Memo Support", ImageManager.CODE);
	}

	@Override
	public PartInfo getPartInfo(URI uri) throws Exception {
		ILocatable locatable;
		try {
			locatable = this.locatorService.resolve(uri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error retrieving " + PartInfo.class.getSimpleName()
					+ " for " + uri);
			return this.getDefaultPartInfo();
		}

		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;
			return new PartInfo(code.getCaption(), ImageManager.CODE);
		} else if (locatable instanceof ICodeInstance) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider lp = this.labelProviderService
					.getLabelProvider(codeInstance.getUri());
			if (lp != null) {
				return new PartInfo(lp.getText(uri) + " (coded with "
						+ codeInstance.getCode().getCaption() + ")",
						lp.getImage(uri));
			} else {
				return null;
			}
		} else {
			ILabelProvider lp = this.labelProviderService.getLabelProvider(uri);
			return new PartInfo(lp.getText(uri), lp.getImage(locatable));
		}
	}

	// TODO PropertyChangeService implementieren; header wird nicht
	// aktualisiert, wenn sich der compilation state verändert hat, da keine
	// Abhängigkeit zum Diff-Plugin eingeführt werden kann (sonst: zyklische
	// Abhängigkeit).

	public void setSourceMode(boolean on) {
		if (on) {
			this.getEditor().showSource();
		} else {
			this.getEditor().hideSource();
		}
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

	public void loadAndClearHistory(URI uri) {
		this.history.clear();
		this.history.add(uri);
		this.updateNavigation();
		this.load(uri);
	}

	protected void updateNavigation() {
		IEvaluationService evaluationService = (IEvaluationService) this
				.getSite().getService(IEvaluationService.class);
		evaluationService
				.requestEvaluation("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.canNavigateBack");
		evaluationService
				.requestEvaluation("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.canNavigateForward");
	}

	public boolean canNavigateBack() {
		return this.history.hasPrev();
	}

	public void navigateBack() {
		if (this.history.hasPrev()) {
			this.load(this.history.back());
			this.updateNavigation();
		}
	}

	public boolean canNavigateForward() {
		return this.history.hasNext();
	}

	public void navigateForward() {
		if (this.history.hasNext()) {
			this.load(this.history.forward());
			this.updateNavigation();
		}
	}
}
