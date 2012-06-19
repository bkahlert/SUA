package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeInstanceViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CodeInstancesView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView";
	private CodeInstanceViewer codeInstanceViewer;

	private ISelectionRetriever<ICodeable> codeableRetriever = SelectionRetrieverFactory
			.getSelectionRetriever(ICodeable.class);

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICodeable> codeables = codeableRetriever.getSelection();
			if (codeables.size() > 0) {
				if (codeInstanceViewer != null
						&& !codeInstanceViewer.isDisposed()) {
					codeInstanceViewer.setInput(codeables);
				}
			}
		}
	};

	public CodeInstancesView() {
		SelectionUtils.getSelectionService().addSelectionListener(
				selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removeSelectionListener(
				selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.codeInstanceViewer = new CodeInstanceViewer(parent, SWT.NONE);
		this.getSite().setSelectionProvider(this.codeInstanceViewer);
		new ContextMenu(this.codeInstanceViewer.getViewer(), getSite()) {
			@Override
			protected String getDefaultCommandID() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public void setFocus() {
		this.codeInstanceViewer.setFocus();
	}

	public ICodeable getCodeable() {
		if (this.codeInstanceViewer == null)
			return null;
		return this.codeInstanceViewer.getCodeable();
	}

}
