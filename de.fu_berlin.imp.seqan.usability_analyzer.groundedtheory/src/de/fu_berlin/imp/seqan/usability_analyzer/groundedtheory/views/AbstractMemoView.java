package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.KeyboardUtils;
import com.bkahlert.devel.nebula.utils.history.History;
import com.bkahlert.devel.nebula.utils.history.IHistory;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.BrowserOpeningAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.SchemeAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.URIAdapter;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.composer.IAnkerLabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class AbstractMemoView extends InformationPresentingEditorView<Object> {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractMemoView.class);

	private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codesAssigned(List<ICode> codes, List<ILocatable> codeables) {
			if (codeables.contains(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ILocatable> codeables) {
			if (codeables.contains(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		};

		private void reloadIfNecessary(Object object) {
			if (object.equals(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		}

		@Override
		public void memoAdded(ICode code) {
			this.reloadIfNecessary(code);
		};

		@Override
		public void memoAdded(ILocatable codeable) {
			this.reloadIfNecessary(codeable);
		};

		@Override
		public void memoModified(ICode code) {
			this.reloadIfNecessary(code);
		};

		@Override
		public void memoModified(ILocatable codeable) {
			this.reloadIfNecessary(codeable);
		};

		@Override
		public void memoRemoved(ICode code) {
			this.reloadIfNecessary(code);
		};

		@Override
		public void memoRemoved(ILocatable codeable) {
			this.reloadIfNecessary(codeable);
		};
	};

	private IHighlightService highlightService = (IHighlightService) PlatformUI
			.getWorkbench().getService(IHighlightService.class);

	private IHistory<Object> history;

	public AbstractMemoView() {
		super(2000, ToolbarSet.DEFAULT, true);
		this.history = new History<Object>();
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
						ILocatable codeable = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AbstractMemoView.this.codeService
								.getCodedObject(uri);
						if (codeable != null) {
							return true;
						}
					} catch (URISyntaxException e) {

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
						ILocatable codeable = AbstractMemoView.this.codeService
								.getCodedObject(uri);
						ILabelProvider labelProvider = AbstractMemoView.this.labelProviderService
								.getLabelProvider(codeable);
						if (codeable != null && labelProvider != null) {
							return labelProvider.getText(codeable);
						}
					} catch (URISyntaxException e) {

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
			public void uriClicked(final URI uri) {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				final ILocatable codeable = codeService.getCodedObject(uri);
				if (!KeyboardUtils.isMetaKeyPressed()) {
					// treat link as a typical link that opens a resource
					AbstractMemoView.this.history.add(codeable);
					AbstractMemoView.this.updateNavigation();
					AbstractMemoView.this.load(codeable);
				} else {
					// do not follow the link but make Eclipse open the resource
					TimeZoneDateRange range = (TimeZoneDateRange) Platform
							.getAdapterManager().getAdapter(codeable,
									TimeZoneDateRange.class);
					if (range != null) {
						AbstractMemoView.this.highlightService.highlight(
								AbstractMemoView.this, range, true);
					}

					// open element
					if (!codeService.showCodedObjectInWorkspace(uri,
							KeyboardUtils.isMetaKeyPressed())) {
						ExecutorUtil.asyncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openInformation(PlatformUI
										.getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
										"Artefact not found", "The artefact "
												+ uri.toString()
												+ " could not be found.");
							}
						});
					}
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
	public PartInfo getPartInfo(Object loadedObject) {
		if (loadedObject instanceof ICode) {
			return new PartInfo(((ICode) loadedObject).getCaption(),
					ImageManager.CODE);
		} else if (loadedObject instanceof ICodeInstance) {
			ICodeInstance codeInstance = (ICodeInstance) loadedObject;
			ILocatable coded = this.codeService.getCodedObject(codeInstance
					.getId());
			if (coded != null) {
				ILabelProvider lp = this.labelProviderService
						.getLabelProvider(coded);
				return new PartInfo(lp.getText(coded) + " (coded with "
						+ codeInstance.getCode().getCaption() + ")",
						lp.getImage(coded));
			} else {
				return new PartInfo(codeInstance.getId().toString(), PlatformUI
						.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_WARN_TSK));
			}
		} else if (loadedObject instanceof ILocatable) {
			ILocatable codeable = (ILocatable) loadedObject;
			ILabelProvider lp = this.labelProviderService
					.getLabelProvider(codeable);
			return new PartInfo(lp.getText(codeable), lp.getImage(codeable));
		} else {
			return this.getDefaultPartInfo();
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
	public String getHtml(Object objectToLoad, IProgressMonitor monitor) {
		if (objectToLoad instanceof ICode) {
			return this.codeService.loadMemo((ICode) objectToLoad);
		} else if (objectToLoad instanceof ICodeInstance) {
			return this.codeService.loadMemo((ICodeInstance) objectToLoad);
		} else if (objectToLoad instanceof ILocatable) {
			return this.codeService.loadMemo((ILocatable) objectToLoad);
		} else {
			return null;
		}
	}

	@Override
	public void setHtml(Object loadedObject, String html,
			IProgressMonitor monitor) {
		try {
			if (loadedObject instanceof ICode) {
				this.codeService.setMemo((ICode) loadedObject, html);
			} else if (loadedObject instanceof ICodeInstance) {
				this.codeService.setMemo((ICodeInstance) loadedObject, html);
			} else if (loadedObject instanceof ILocatable) {
				this.codeService.setMemo((ILocatable) loadedObject, html);
			}
		} catch (CodeServiceException e) {
			LOGGER.error("Can't save memo for " + loadedObject, e);
		}
	}

	public void loadAndClearHistory(Object objectToLoad) {
		this.history.clear();
		this.history.add(objectToLoad);
		this.updateNavigation();
		this.load(objectToLoad);
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
