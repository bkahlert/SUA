package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;

public class CodeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView";
	private CodeViewer codeViewer;

	public CodeView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.codeViewer = new CodeViewer(parent, SWT.NONE);
		this.getSite().setSelectionProvider(this.codeViewer);
		hookContextMenu();
	}

	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				menuMgr.add(new GroupMarker(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuMgr.createContextMenu(codeViewer.getControl());
		codeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, codeViewer);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
