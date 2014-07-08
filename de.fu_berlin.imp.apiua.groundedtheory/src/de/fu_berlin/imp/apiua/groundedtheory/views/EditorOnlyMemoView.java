package de.fu_berlin.imp.apiua.groundedtheory.views;

import de.fu_berlin.imp.apiua.core.model.URI;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.nebula.utils.selection.SelectionUtils;

public class EditorOnlyMemoView extends AbstractMemoView {

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.EditorOnlyMemoView";
	private IPartListener partListener = new IPartListener() {

		private URI getLocatable(IWorkbenchPart part) {
			ISelection selection = SelectionUtils.getSelection(part.getSite()
					.getWorkbenchWindow());
			if (selection == null) {
				return null;
			}
			return (URI) Platform.getAdapterManager().getAdapter(selection,
					URI.class);
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.getClass() == EditorOnlyMemoView.class) {
				return;
			}
			URI uri = this.getLocatable(part);
			if (uri != null) {
				EditorOnlyMemoView.this.loadAndClearHistory(uri);
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
