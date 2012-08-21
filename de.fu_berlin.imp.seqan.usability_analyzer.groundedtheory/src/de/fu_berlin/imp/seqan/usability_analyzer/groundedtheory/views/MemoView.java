package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets.MemoComposer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets.MemoComposer.IPartDelegate;

public class MemoView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.MemoView";
	private MemoComposer memoComposer;

	Job memoLoader = null;

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			final List<ICode> codes = SelectionUtils.getAdaptableObjects(
					selection, ICode.class);
			final List<ICodeInstance> codeInstances = SelectionUtils
					.getAdaptableObjects(selection, ICodeInstance.class);
			final List<ICodeable> codeables = SelectionUtils
					.getAdaptableObjects(selection, ICodeable.class);
			final List<Object> objects = SelectionUtils.getAdaptableObjects(
					selection, Object.class);

			if (memoLoader != null)
				memoLoader.cancel();

			memoLoader = new Job("Loading Memo") {
				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					if (progressMonitor.isCanceled())
						return Status.CANCEL_STATUS;
					SubMonitor monitor = SubMonitor.convert(progressMonitor, 1);
					if (codes.size() > 0)
						MemoView.this.memoComposer.load(codes.get(0), monitor);
					else if (codeInstances.size() > 0)
						MemoView.this.memoComposer.load(codeInstances.get(0),
								monitor);
					else if (codeables.size() > 0)
						MemoView.this.memoComposer.load(codeables.get(0),
								monitor);
					else if (objects.size() > 0)
						MemoView.this.memoComposer.lock(monitor);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			memoLoader.schedule();
		}
	};

	public MemoView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.memoComposer = new MemoComposer(parent, SWT.BORDER,
				new SUAGTPreferenceUtil().getMemoAutosaveAfterMilliseconds(),
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

		SelectionUtils.getSelectionService().addPostSelectionListener(
				selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				selectionListener);
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (this.memoComposer != null && !this.memoComposer.isDisposed())
			this.memoComposer.setFocus();
	}

}
