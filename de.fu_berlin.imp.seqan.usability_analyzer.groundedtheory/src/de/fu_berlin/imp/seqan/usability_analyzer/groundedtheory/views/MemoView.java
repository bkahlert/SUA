package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

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

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets.MemoComposer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets.MemoComposer.IPartDelegate;

public class MemoView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.MemoView";
	private MemoComposer memoComposer;

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

	synchronized public void load(final Object object) {
		MemoView.this.memoComposer.load(object);
	}

	@Override
	public void setFocus() {
		if (this.memoComposer != null && !this.memoComposer.isDisposed())
			this.memoComposer.setFocus();
	}

	public void setSourceMode(boolean on) {
		this.memoComposer.setSourceMode(on);
	}

}
