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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.bkahlert.devel.nebula.dialogs.PopupDialog;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.KeyboardUtils;
import com.bkahlert.devel.nebula.utils.history.History;
import com.bkahlert.devel.nebula.utils.history.IHistory;
import com.bkahlert.devel.nebula.views.EditorView;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.BrowserOpeningAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.IURIListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.SchemeAnkerListener;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.composer.IAnkerLabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeableProvider.IDetailedLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class AbstractMemoView extends EditorView<Object> {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractMemoView.class);

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			if (codeables.contains(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
			if (codeables.contains(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
			}
		};

		private void reloadIfNecessary(Object object) {
			if (object.equals(AbstractMemoView.this.getLoadedObject())) {
				AbstractMemoView.this.refreshHeader();
				if (lastSaveBy != AbstractMemoView.this) {
					de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AbstractMemoView.this
							.load(null);
				}
			}
		}

		@Override
		public void memoAdded(ICode code) {
			this.reloadIfNecessary(code);
		};

		@Override
		public void memoAdded(ICodeable codeable) {
			this.reloadIfNecessary(codeable);
		};

		@Override
		public void memoModified(ICode code) {
			this.reloadIfNecessary(code);
		};

		@Override
		public void memoModified(ICodeable codeable) {
			this.reloadIfNecessary(codeable);
		};

		@Override
		public void memoRemoved(ICode code) {
			this.reloadIfNecessary(code);
		};

		@Override
		public void memoRemoved(ICodeable codeable) {
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
		this.addAnkerLabelProvider(new IAnkerLabelProvider() {
			@Override
			public boolean isResponsible(IAnker anker) {
				if (anker.getHref() != null) {
					try {
						URI uri = new URI(anker.getHref());
						ICodeable codeable = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AbstractMemoView.this.codeService
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
						ICodeable codeable = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AbstractMemoView.this.codeService
								.getCodedObject(uri);
						ILabelProvider labelProvider = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AbstractMemoView.this.codeService
								.getLabelProvider(uri);
						if (codeable != null && labelProvider != null) {
							return labelProvider.getText(codeable);
						}
					} catch (URISyntaxException e) {

					}
				}
				return "!!! " + anker.getHref() + " !!!";
			}
		});
		this.addAnkerListeners();
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	private void addAnkerListeners() {
		Map<String, IAnkerListener> listeners = new HashMap<String, IAnkerListener>();
		listeners.put("SUA", new AnkerAdaptingListener(new IURIListener() {
			@Override
			public void uriClicked(final URI uri) {
				this.closePopup();

				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				final ICodeable codeable = codeService.getCodedObject(uri);
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
								AbstractMemoView.this, range);
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

			private PopupDialog popup = null;

			@Override
			public void uriHovered(URI uri, boolean entered) {
				this.closePopup();

				ILabelProvider labelProvider = AbstractMemoView.this.codeService
						.getLabelProvider(uri);
				final ICodeable codeable = AbstractMemoView.this.codeService
						.getCodedObject(uri);
				if (labelProvider == null || codeable == null) {
					return;
				}

				if (labelProvider instanceof IDetailedLabelProvider) {
					final IDetailedLabelProvider detailedLabelProvider = (IDetailedLabelProvider) labelProvider;
					if (entered && detailedLabelProvider.canFillPopup(codeable)) {
						this.popup = new PopupDialog() {
							@Override
							protected Control createControls(Composite parent) {
								return detailedLabelProvider.fillPopup(
										codeable, parent);
							};
						};
						ExecutorUtil.syncExec(new Runnable() {
							@Override
							public void run() {
								popup.open();
							}
						});
					}
				}
			}

			public void closePopup() {
				if (this.popup != null) {
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							popup.close();
						}
					});
					this.popup = null;
				}
			}
		}));
		this.addAnkerListener(new SchemeAnkerListener(listeners,
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
			ICodeable coded = this.codeService.getCodedObject(codeInstance
					.getId());
			if (coded != null) {
				ILabelProvider lp = this.codeService.getLabelProvider(coded
						.getUri());
				return new PartInfo(lp.getText(coded) + " (coded with "
						+ codeInstance.getCode().getCaption() + ")",
						lp.getImage(coded));
			} else {
				return new PartInfo(codeInstance.getId().toString(), PlatformUI
						.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_WARN_TSK));
			}
		} else if (loadedObject instanceof ICodeable) {
			ICodeable codeable = (ICodeable) loadedObject;
			ILabelProvider lp = this.codeService.getLabelProvider(codeable
					.getUri());
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
			this.showSource();
		} else {
			this.hideSource();
		}
	}

	/**
	 * Reference to the {@link AbstractMemoView} that executed the last save
	 * action.
	 * <p>
	 * Used to distinguish the saving {@link AbstractMemoView} from the others
	 * which need to reload their contents if they loaded the same object.
	 */
	private static AbstractMemoView lastSaveBy = null;

	@Override
	public String getHtml(Object objectToLoad, IProgressMonitor monitor) {
		if (objectToLoad instanceof ICode) {
			return this.codeService.loadMemo((ICode) objectToLoad);
		} else if (objectToLoad instanceof ICodeInstance) {
			return this.codeService.loadMemo((ICodeInstance) objectToLoad);
		} else if (objectToLoad instanceof ICodeable) {
			return this.codeService.loadMemo((ICodeable) objectToLoad);
		} else {
			return null;
		}
	}

	@Override
	public void setHtml(Object loadedObject, String html,
			IProgressMonitor monitor) {
		try {
			lastSaveBy = this;
			if (loadedObject instanceof ICode) {
				this.codeService.setMemo((ICode) loadedObject, html);
			} else if (loadedObject instanceof ICodeInstance) {
				this.codeService.setMemo((ICodeInstance) loadedObject, html);
			} else if (loadedObject instanceof ICodeable) {
				this.codeService.setMemo((ICodeable) loadedObject, html);
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
