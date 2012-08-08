package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
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
	private StyledText text;

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

		public void memoModified(ICodeable codeable) {
			if (codeable.equals(getMemoOwner()))
				refreshPartHeader();
		};
	};

	private ICode code = null;
	private ICodeInstance codeInstance = null;
	private ICodeable codeable = null;

	public MemoComposer(Composite parent, int style,
			final long autosaveAfterMilliseconds, IPartDelegate partDelegate) {
		super(parent, style & ~SWT.BORDER);
		this.setLayout(GridLayoutFactory.fillDefaults().create());

		this.partDelegate = partDelegate;

		this.text = new StyledText(this, style & SWT.BORDER | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		this.text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.text.addModifyListener(new ModifyListener() {
			private Timer timer = null;

			@Override
			public void modifyText(ModifyEvent e) {
				if (timer != null)
					timer.cancel();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						new Job("Auto-Saving Memo") {
							@Override
							protected IStatus run(
									IProgressMonitor progressMonitor) {
								SubMonitor monitor = SubMonitor.convert(
										progressMonitor, 1);
								System.err.println("autosave");
								save(monitor);
								monitor.done();
								return Status.OK_STATUS;
							}
						}.schedule();
					}
				}, autosaveAfterMilliseconds);
			}
		});
		this.text.addLineStyleListener(new HyperlinkLineStyleListener());
		StyledTextHyperlinkHandler.addListenerTo(this.text);
		SelectAllHandler.addListenerTo(this.text);

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
		this.save(monitor.newChild(1));
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
		this.save(monitor.newChild(1));
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
		this.save(monitor.newChild(1));
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
				"Loading Memo for " + codeable.getCodeInstanceID().toString(),
				2);
		this.save(monitor.newChild(1));
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
			ILabelProvider lp = this.codeService.getLabelProvider(coded
					.getCodeInstanceID());
			icon = lp.getImage(coded);
			caption = lp.getText(coded) + " (coded with "
					+ this.codeInstance.getCode().getCaption() + ")";
		} else if (this.codeable != null) {
			ILabelProvider lp = this.codeService.getLabelProvider(this.codeable
					.getCodeInstanceID());
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
		final String finalHtml = html;
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				text.setText(finalHtml != null ? finalHtml : "");
				text.setEnabled(finalEnabled);
				text.setEditable(finalEnabled);
				layout();
				monitor.done();
			}
		});
	}

	synchronized private void save(IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor, "Saving Memo",
				3);
		if (code == null && codeInstance == null && codeable == null)
			return;
		try {
			String html = ExecutorUtil.syncExec(new Callable<String>() {
				@Override
				public String call() throws Exception {
					String s = text.getText();
					// System.out.println(s);
					return s;
				}
			});
			monitor.worked(1);

			if (code != null) {
				try {
					codeService.setMemo(code, html);
				} catch (CodeServiceException e) {
					LOGGER.error("Can't save memo for " + code, e);
				}
				return;
			}
			if (codeInstance != null) {
				try {
					codeService.setMemo(codeInstance, html);
				} catch (CodeServiceException e) {
					LOGGER.error("Can't save memo for " + codeInstance, e);
				}
				return;
			}
			if (codeable != null) {
				try {
					codeService.setMemo(codeable, html);
				} catch (CodeServiceException e) {
					LOGGER.error("Can't save memo for " + codeable, e);
				}
				return;
			}
		} catch (Exception e) {
			LOGGER.error("Error saving memo", e);
		} finally {
			monitor.done();
		}
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

}
