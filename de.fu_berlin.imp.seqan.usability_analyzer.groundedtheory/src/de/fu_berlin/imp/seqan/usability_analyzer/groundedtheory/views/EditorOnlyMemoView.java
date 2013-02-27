package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class EditorOnlyMemoView extends MemoView {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EditorOnlyMemoView";
	private IPartListener partListener = new IPartListener() {

		private ICodeable getCodeable(IWorkbenchPart part) {
			ISelection selection = SelectionUtils.getSelection(part.getSite()
					.getWorkbenchWindow());
			if (selection == null)
				return null;
			return (ICodeable) Platform.getAdapterManager().getAdapter(
					selection, ICodeable.class);
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.getClass() == EditorOnlyMemoView.class)
				return;
			ICodeable codeable = getCodeable(part);
			if (codeable != null)
				load(codeable);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}
	};

	public EditorOnlyMemoView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(partListener);
		super.dispose();
	}

}
