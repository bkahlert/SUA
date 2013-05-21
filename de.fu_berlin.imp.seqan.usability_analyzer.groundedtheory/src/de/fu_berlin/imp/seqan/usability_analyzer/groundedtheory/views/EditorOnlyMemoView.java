package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class EditorOnlyMemoView extends AbstractMemoView {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EditorOnlyMemoView";
	private IPartListener partListener = new IPartListener() {

		private ILocatable getCodeable(IWorkbenchPart part) {
			ISelection selection = SelectionUtils.getSelection(part.getSite()
					.getWorkbenchWindow());
			if (selection == null) {
				return null;
			}
			return (ILocatable) Platform.getAdapterManager().getAdapter(
					selection, ILocatable.class);
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.getClass() == EditorOnlyMemoView.class) {
				return;
			}
			ILocatable codeable = this.getCodeable(part);
			if (codeable != null) {
				EditorOnlyMemoView.this.loadAndClearHistory(codeable);
			}
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
	public void postInit() {
		this.getSite().getPage().addPartListener(this.partListener);
		super.postInit();
	};

	@Override
	public void dispose() {
		this.getSite().getPage().removePartListener(this.partListener);
		super.dispose();
	}

}
