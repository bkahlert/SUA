package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.information;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;

import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.extender.IInformationControlExtender;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeInstanceViewer;

public class CodeInformationControlExtender implements
		IInformationControlExtender<URI> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CodeInformationControlExtender.class);

	private CodeInstanceViewer codeInstanceViewer = null;

	public CodeInformationControlExtender() {
	}

	@Override
	public void extend(InformationControl<URI> informationControl,
			Composite parent) {
		this.codeInstanceViewer = new CodeInstanceViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).hint(350, 350)
				.minSize(350, 300).applyTo(this.codeInstanceViewer);

		final MenuManager menuManager = new MenuManager();
		Menu popupMenu = menuManager.createContextMenu(this.codeInstanceViewer);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IMenuService menuService = (IMenuService) PlatformUI
						.getWorkbench().getService(IMenuService.class);
				// TODO index out of bounds exception thrown for unknown reason
				// TODO missing contributions (possibly due to thrown exception)
				manager.add(new GroupMarker(
						IWorkbenchActionConstants.MB_ADDITIONS));
				menuService.populateContributionManager(menuManager,
						"popup:org.eclipse.ui.popup.any");
			}
		});

		this.codeInstanceViewer.setMenu(popupMenu);
	}

	@Override
	public void extend(InformationControl<URI> informationControl,
			URI information) {
		List<URI> uris = new ArrayList<URI>();
		uris.add(information);
		this.codeInstanceViewer.setInput(uris);
	}

}
