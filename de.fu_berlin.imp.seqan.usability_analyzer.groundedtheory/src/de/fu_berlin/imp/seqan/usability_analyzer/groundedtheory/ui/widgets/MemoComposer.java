package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.devel.nebula.widgets.editor.IAnkerLabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class MemoComposer extends Composite {

	private static final Logger LOGGER = Logger.getLogger(MemoComposer.class);

	public static interface IPartDelegate {
		public void setImage(Image image);

		public void setName(String name);
	}

	private IPartDelegate partDelegate;
	private Editor editor;

	private ICodeService codeService = null;
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			if (codeables.contains(getMemoOwner()))
				refreshPartHeader();
		}

		public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
			if (codeables.contains(getMemoOwner()))
				refreshPartHeader();
		};

		private void reloadIfNecessary(Object object) {
			if (object.equals(getMemoOwner())) {
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

	private ICode code = null;
	private ICodeInstance codeInstance = null;
	private ICodeable codeable = null;

	/**
	 * Reference to the {@link MemoComposer} that executed the last save action.
	 * <p>
	 * Used to distinguish the saving {@link MemoComposer} from the others which
	 * need to reload their contents if they loaded the same object.
	 */
	private static MemoComposer lastSaveBy = null;

	private String oldHtml = "";

	public MemoComposer(Composite parent, int style, IPartDelegate partDelegate) {
		super(parent, style & ~SWT.BORDER);
		this.setLayout(GridLayoutFactory.fillDefaults().create());

		this.partDelegate = partDelegate;

		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		this.editor = new Editor(this, style & SWT.BORDER, 2000);
		this.editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.editor.addAnkerLabelProvider(new IAnkerLabelProvider() {
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
		this.editor.addAnkerListener(new IAnkerListener() {
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
		this.editor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				new Job("Auto-Saving Memo") {
					@Override
					protected IStatus run(IProgressMonitor progressMonitor) {
						SubMonitor monitor = SubMonitor.convert(
								progressMonitor, 1);
						if (save((String) e.data, monitor)) {
							LOGGER.info("Memo auto-saved");
						}
						/*
						 * Sleep a short time to allow the progress bar to
						 * appear at least shortly.
						 */
						try {
							Thread.sleep(200);
						} catch (InterruptedException e1) {
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		});
		// FIXME
		// this.editor.addLineStyleListener(new HyperlinkLineStyleListener());
		// StyledTextHyperlinkHandler.addListenerTo(this.editor);
		// SelectAllHandler.addListenerTo(this.editor);

		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);
		codeService.addCodeServiceListener(codeServiceListener);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				codeService.removeCodeServiceListener(codeServiceListener);
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				lock(new NullProgressMonitor());
			}
		}).start();
	}

	/**
	 * Locks the {@link MemoComposer} so nothing can be edited.
	 * <p>
	 * Must be called from a non-UI thread.
	 * 
	 * @param codeable
	 */
	synchronized public void lock(IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor,
				"Disabling Memo Editor", 2);
		this.save(null, monitor.newChild(1));
		this.code = null;
		this.codeInstance = null;
		this.codeable = null;
		this.load(monitor.newChild(1));
	}

	/**
	 * Loads a {@link ICode}.
	 * <p>
	 * Must be called from a non-UI thread.
	 * 
	 * @param monitor
	 * 
	 * @param codeable
	 */
	synchronized public void load(ICode code, IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor,
				"Loading Memo for " + code.getCaption(), 2);
		this.save(null, monitor.newChild(1));
		this.code = code;
		this.codeInstance = null;
		this.codeable = null;
		this.load(monitor.newChild(1));
	}

	/**
	 * Loads a {@link ICodeInstance}.
	 * <p>
	 * Must be called from a non-UI thread.
	 * 
	 * @param monitor
	 * 
	 * @param codeable
	 */
	synchronized public void load(ICodeInstance codeInstance,
			IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor,
				"Loading Memo for " + codeInstance.getId().toString(), 2);
		this.save(null, monitor.newChild(1));
		this.code = null;
		this.codeInstance = codeInstance;
		this.codeable = null;
		this.load(monitor.newChild(1));
	}

	/**
	 * Loads a {@link ICodeable}.
	 * <p>
	 * Must be called from a non-UI thread.
	 * 
	 * @param codeable
	 */
	synchronized public void load(ICodeable codeable,
			IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor,
				"Loading Memo for " + codeable.getUri().toString(), 2);
		this.save(null, monitor.newChild(1));
		this.code = null;
		this.codeInstance = null;
		this.codeable = codeable;
		this.load(monitor.newChild(1));
	}

	/**
	 * May be called from any thread.
	 */
	synchronized private void refreshPartHeader() {
		if (partDelegate == null)
			return;

		Image icon = null;
		String caption = null;
		if (this.code != null) {
			icon = ImageManager.CODE;
			caption = this.code.getCaption();
		} else if (this.codeInstance != null) {
			ICodeable coded = this.codeService.getCodedObject(this.codeInstance
					.getId());
			if (coded != null) {
				ILabelProvider lp = this.codeService.getLabelProvider(coded
						.getUri());
				icon = lp.getImage(coded);
				caption = lp.getText(coded) + " (coded with "
						+ this.codeInstance.getCode().getCaption() + ")";
			} else {
				icon = PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				caption = this.codeInstance.getId().toString();
			}
		} else if (this.codeable != null) {
			ILabelProvider lp = this.codeService.getLabelProvider(this.codeable
					.getUri());
			icon = lp.getImage(this.codeable);
			caption = lp.getText(this.codeable);
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

	/**
	 * Must be called from a non-UI thread.
	 */
	synchronized private void load(IProgressMonitor progressMonitor) {
		final SubMonitor monitor = SubMonitor.convert(progressMonitor,
				"Loading Memo", 2);
		refreshPartHeader();

		boolean enabled = true;
		String html = null;
		if (this.code != null)
			html = this.codeService.loadMemo(code);
		else if (this.codeInstance != null)
			html = this.codeService.loadMemo(codeInstance);
		else if (this.codeable != null)
			html = this.codeService.loadMemo(codeable);
		else {
			html = "";
			enabled = false;
		}
		monitor.worked(1);

		final boolean finalEnabled = enabled;
		oldHtml = html != null ? html : "";
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				editor.setSource(oldHtml);
				editor.setEnabled(finalEnabled);
				layout();
				monitor.done();
			}
		});
	}

	/**
	 * Saves the given html to the currently loaded object.
	 * 
	 * @param html
	 *            if null the editor's html is used
	 * @param progressMonitor
	 * @return
	 */
	synchronized private boolean save(String html,
			IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor, "Saving Memo",
				3);
		if (code == null && codeInstance == null && codeable == null)
			return false;
		try {
			if (html == null)
				html = ExecutorUtil.syncExec(new Callable<String>() {
					@Override
					public String call() throws Exception {
						String s = editor.getSource();
						return s;
					}
				});
			if (oldHtml.equals(html))
				return false;
			else
				oldHtml = html;

			monitor.worked(1);

			lastSaveBy = this;
			if (code != null) {
				try {
					codeService.setMemo(code, html);
				} catch (CodeServiceException e) {
					LOGGER.error("Can't save memo for " + code, e);
				}
				return true;
			}
			if (codeInstance != null) {
				try {
					codeService.setMemo(codeInstance, html);
				} catch (CodeServiceException e) {
					LOGGER.error("Can't save memo for " + codeInstance, e);
				}
				return true;
			}
			if (codeable != null) {
				try {
					codeService.setMemo(codeable, html);
				} catch (CodeServiceException e) {
					LOGGER.error("Can't save memo for " + codeable, e);
				}
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("Error saving memo", e);
		} finally {
			monitor.done();
		}
		return false;
	}

	private synchronized Object getMemoOwner() {
		if (code != null)
			return code;
		if (codeInstance != null)
			return codeInstance;
		if (codeable != null)
			return codeable;
		return null;
	}

	public void setSourceMode(boolean on) {
		if (on)
			this.editor.showSource();
		else
			this.editor.hideSource();
	}

}
