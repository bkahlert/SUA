package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.ResortableCodeViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CodeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView";
	private CodeViewer codeViewer;
	private MemoComposer memoComposer;

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICode> codes = SelectionUtils.getAdaptableObjects(selection,
					ICode.class);
			if (codes.size() > 0) {
				CodeView.this.memoComposer.load(codes.get(0));
				return;
			}

			List<ICodeInstance> codeInstances = SelectionUtils
					.getAdaptableObjects(selection, ICodeInstance.class);
			if (codeInstances.size() > 0) {
				CodeView.this.memoComposer.load(codeInstances.get(0));
				return;
			}

			List<ICodeable> codeables = SelectionUtils.getAdaptableObjects(
					selection, ICodeable.class);
			if (codeables.size() > 0) {
				CodeView.this.memoComposer.load(codeables.get(0));
				return;
			}
		}
	};

	public CodeView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		sash.setSashWidth(10);

		this.codeViewer = new ResortableCodeViewer(sash, SWT.NONE);
		this.memoComposer = new MemoComposer(sash, SWT.BORDER,
				new SUAGTPreferenceUtil().getMemoAutosaveAfterMilliseconds());

		new ContextMenu(this.codeViewer.getViewer(), getSite()) {
			@Override
			protected String getDefaultCommandID() {
				// TODO Auto-generated method stub
				return null;
			}
		};

		SelectionUtils.getSelectionService().addPostSelectionListener(
				selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				selectionListener);
		super.dispose();
	}

	public CodeViewer getCodeViewer() {
		return this.codeViewer;
	}

	@Override
	public void setFocus() {
		if (this.codeViewer != null && !this.codeViewer.isDisposed())
			this.codeViewer.setFocus();
	}

}
