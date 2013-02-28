package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets.MemoComposer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets.MemoComposer.IPartDelegate;

public class MemoView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.MemoView";
	private MemoComposer memoComposer;

	private Job memoLoader = null;

	public MemoView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.memoComposer = new MemoComposer(parent, SWT.NONE,
				new IPartDelegate() {
					@Override
					public void setName(String name) {
						MemoView.this.setPartName(name);
					}

					@Override
					public void setImage(Image image) {
						MemoView.this.setTitleImage(image);
					}
				});

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
	}

	synchronized public void load(final ICode code) {
		Assert.isNotNull(code);

		if (memoLoader != null)
			memoLoader.cancel();

		memoLoader = new Job("Loading Memo") {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled())
					return Status.CANCEL_STATUS;
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 1);
				MemoView.this.memoComposer.load(code, monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		memoLoader.schedule();
	}

	synchronized public void load(final ICodeInstance codeInstance) {
		Assert.isNotNull(codeInstance);

		if (memoLoader != null)
			memoLoader.cancel();

		memoLoader = new Job("Loading Memo") {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled())
					return Status.CANCEL_STATUS;
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 1);
				MemoView.this.memoComposer.load(codeInstance, monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		memoLoader.schedule();
	}

	synchronized public void load(final ICodeable codeable) {
		Assert.isNotNull(codeable);

		if (memoLoader != null)
			memoLoader.cancel();

		memoLoader = new Job("Loading Memo") {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled())
					return Status.CANCEL_STATUS;
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 1);
				MemoView.this.memoComposer.load(codeable, monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		memoLoader.schedule();
	}

	synchronized public void lock() {
		if (memoLoader != null)
			memoLoader.cancel();

		memoLoader = new Job("Loading Memo") {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled())
					return Status.CANCEL_STATUS;
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 1);
				MemoView.this.memoComposer.lock(monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		memoLoader.schedule();
	}

	@Override
	public void setFocus() {
		if (this.memoComposer != null && !this.memoComposer.isDisposed())
			this.memoComposer.setFocus();
	}

	public void setSourceMode(boolean on) {
		this.memoComposer.getEditor().setSourceMode(on);
	}

}
