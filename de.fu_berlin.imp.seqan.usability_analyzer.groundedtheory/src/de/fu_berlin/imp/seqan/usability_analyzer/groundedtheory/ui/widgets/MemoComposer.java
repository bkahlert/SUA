package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.composer.IAnkerLabelProvider;
import com.bkahlert.devel.nebula.widgets.editor.AutosaveEditor;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class MemoComposer extends AutosaveEditor<Object> {

	private static final Logger LOGGER = Logger.getLogger(MemoComposer.class);

	public static interface IPartDelegate {
		public void setImage(Image image);

		public void setName(String name);
	}

	private ICodeService codeService = null;
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			if (codeables.contains(getLoadedObject()))
				refreshPartHeader();
		}

		public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
			if (codeables.contains(getLoadedObject()))
				refreshPartHeader();
		};

		private void reloadIfNecessary(Object object) {
			if (object.equals(getLoadedObject())) {
				refreshPartHeader();
				if (lastSaveBy != MemoComposer.this) {
					load(null);
				}
			}
		}

		public void memoAdded(ICode code) {
			reloadIfNecessary(code);
		};

		public void memoAdded(ICodeable codeable) {
			reloadIfNecessary(codeable);
		};

		public void memoModified(ICode code) {
			reloadIfNecessary(code);
		};

		public void memoModified(ICodeable codeable) {
			reloadIfNecessary(codeable);
		};

		public void memoRemoved(ICode code) {
			reloadIfNecessary(code);
		};

		public void memoRemoved(ICodeable codeable) {
			reloadIfNecessary(codeable);
		};
	};

	// TODO PropertyChangeService implementieren; header wird nicht
	// aktualisiert, wenn sich der compilation state verändert hat, da keine
	// Abhängigkeit zum Diff-Plugin eingeführt werden kann (sonst: zyklische
	// Abhängigkeit).

	/**
	 * Reference to the {@link MemoComposer} that executed the last save action.
	 * <p>
	 * Used to distinguish the saving {@link MemoComposer} from the others which
	 * need to reload their contents if they loaded the same object.
	 */
	private static MemoComposer lastSaveBy = null;

	private IPartDelegate partDelegate;

	public MemoComposer(Composite parent, int style, IPartDelegate partDelegate) {
		super(parent, style & ~SWT.BORDER, 2000);

		this.partDelegate = partDelegate;

		this.composer.addAnkerLabelProvider(new IAnkerLabelProvider() {
			@Override
			public boolean isResponsible(IAnker anker) {
				if (anker.getHref() != null) {
					try {
						URI uri = new URI(anker.getHref());
						ICodeable codeable = codeService.getCodedObject(uri);
						if (codeable != null)
							return true;
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
						ICodeable codeable = codeService.getCodedObject(uri);
						ILabelProvider labelProvider = codeService
								.getLabelProvider(uri);
						if (codeable != null && labelProvider != null)
							return labelProvider.getText(codeable);
					} catch (URISyntaxException e) {

					}
				}
				return "!!! " + anker.getHref() + " !!!";
			}
		});
		this.composer.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerClicked(IAnker anker) {
				this.clicked(anker, false);
			}

			@Override
			public void ankerClickedSpecial(IAnker anker) {
				this.clicked(anker, true);
			}

			private void clicked(final IAnker anker, final boolean special) {
				ExecutorUtil.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							final URI uri = new URI(anker.getHref());
							if (uri.getScheme() == null)
								return;
							if (uri.getScheme().equalsIgnoreCase("SUA")) {
								ICodeService codeService = (ICodeService) PlatformUI
										.getWorkbench().getService(
												ICodeService.class);
								if (!codeService.showCodedObjectInWorkspace(
										uri, special)) {
									ExecutorUtil.asyncExec(new Runnable() {
										@Override
										public void run() {
											MessageDialog
													.openInformation(
															PlatformUI
																	.getWorkbench()
																	.getActiveWorkbenchWindow()
																	.getShell(),
															"Artefact not found",
															"The artefact "
																	+ uri.toString()
																	+ " could not be found.");
										}
									});
								}
							} else {
								try {
									IWebBrowser browser = PlatformUI
											.getWorkbench().getBrowserSupport()
											.getExternalBrowser();
									browser.openURL(new URL(uri.toString()));
								} catch (PartInitException e) {
									LOGGER.error(
											"Can't open external browser to open "
													+ uri.toString(), e);
								} catch (MalformedURLException e) {
									LOGGER.error("Can't convert "
											+ URI.class.getSimpleName()
											+ " to "
											+ URL.class.getSimpleName() + ": "
											+ uri.toString());
								}
							}
						} catch (URISyntaxException e) {
							LOGGER.fatal("Invalid URI in "
									+ MemoComposer.class.getSimpleName() + ": "
									+ anker);
						}
					}
				});
			}
		});

		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);
		this.codeService.addCodeServiceListener(codeServiceListener);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				codeService.removeCodeServiceListener(codeServiceListener);
			}
		});
	}

	/**
	 * May be called from any thread.
	 */
	synchronized private void refreshPartHeader() {
		if (partDelegate == null)
			return;

		Image icon = null;
		String caption = null;
		if (this.getLoadedObject() instanceof ICode) {
			icon = ImageManager.CODE;
			caption = ((ICode) this.getLoadedObject()).getCaption();
		} else if (this.getLoadedObject() instanceof ICodeInstance) {
			ICodeInstance codeInstance = (ICodeInstance) this.getLoadedObject();
			ICodeable coded = this.codeService.getCodedObject(codeInstance
					.getId());
			if (coded != null) {
				ILabelProvider lp = this.codeService.getLabelProvider(coded
						.getUri());
				icon = lp.getImage(coded);
				caption = lp.getText(coded) + " (coded with "
						+ codeInstance.getCode().getCaption() + ")";
			} else {
				icon = PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				caption = codeInstance.getId().toString();
			}
		} else if (this.getLoadedObject() instanceof ICodeable) {
			ICodeable codeable = (ICodeable) this.getLoadedObject();
			ILabelProvider lp = this.codeService.getLabelProvider(codeable
					.getUri());
			icon = lp.getImage(codeable);
			caption = lp.getText(codeable);
		} else {
			icon = ImageManager.CODE;
			caption = "No Memo Support";
		}

		final Image finalIcon = icon;
		final String finalCaption = caption;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				partDelegate.setImage(finalIcon);
				partDelegate.setName(finalCaption);
			}
		};
		if (ExecutorUtil.isUIThread())
			runnable.run();
		else
			ExecutorUtil.syncExec(runnable);
	}

	public void setSourceMode(boolean on) {
		if (on)
			this.composer.showSource();
		else
			this.composer.hideSource();
	}

	// VEREINHEITLICHEN AUF EDITORVIEW
	@Override
	public String getHtml(Object objectToLoad, IProgressMonitor monitor) {
		if (objectToLoad instanceof ICode)
			return this.codeService.loadMemo((ICode) objectToLoad);
		else if (objectToLoad instanceof ICodeInstance)
			return this.codeService.loadMemo((ICodeInstance) objectToLoad);
		else if (objectToLoad instanceof ICodeable)
			return this.codeService.loadMemo((ICodeable) objectToLoad);
		else {
			return null;
		}
	}

	@Override
	public void setHtml(Object loadedObject, String html,
			IProgressMonitor monitor) {
		try {
			lastSaveBy = this;
			if (loadedObject instanceof ICode)
				this.codeService.setMemo((ICode) loadedObject, html);
			else if (loadedObject instanceof ICodeInstance)
				this.codeService.setMemo((ICodeInstance) loadedObject, html);
			else if (loadedObject instanceof ICodeable)
				this.codeService.setMemo((ICodeable) loadedObject, html);
		} catch (CodeServiceException e) {
			LOGGER.error("Can't save memo for " + loadedObject, e);
		}
	}

}
