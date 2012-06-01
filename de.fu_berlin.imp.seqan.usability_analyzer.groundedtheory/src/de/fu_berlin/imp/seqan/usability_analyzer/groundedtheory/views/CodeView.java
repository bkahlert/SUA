package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.ResortableCodeViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CodeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView";
	private CodeViewer codeViewer;

	public CodeView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.codeViewer = new ResortableCodeViewer(parent, SWT.NONE);
		new ContextMenu(this.codeViewer.getViewer(), getSite()) {
			@Override
			protected String getDefaultCommandID() {
				// TODO Auto-generated method stub
				return null;
			}
		};
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
