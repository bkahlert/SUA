package de.ralfebert.rcputils.menus;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * ContextMenu creates a context menu for a structured viewer and registers it
 * with the workbench. It can assist with handling the default menu item.
 * 
 * @author Ralf Ebert
 * @see http://www.ralfebert.de/blog/eclipsercp/commands_context_menu/
 */
public abstract class ContextMenu {

	private static final Logger LOGGER = Logger.getLogger(ContextMenu.class);

	private final Menu menu;
	private final IWorkbenchPartSite site;
	private final Viewer viewer;
	private final boolean defaultItemHandling = false;

	/**
	 * Creates an empty context menu for a structured viewer and registers it
	 * for the given workbench part site. The created menu is intended to be
	 * filled using contributions to "org.eclipse.ui.menus" with locationURI
	 * "popup:<viewid>". If defaultItemHandling = true,
	 */
	public ContextMenu(Viewer viewer, final IWorkbenchPartSite site) {
		this.viewer = viewer;
		this.site = site;

		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		this.menu = menuManager.createContextMenu(viewer.getControl());

		viewer.getControl().setMenu(this.menu);

		site.registerContextMenu(menuManager, viewer);
		site.setSelectionProvider(viewer);

		this.setDefaultItemHandling(true);
	}

	/**
	 * Activates default item handling. When set to true, the menu is extended
	 * so that the command corresponding to the id returned by
	 * {@link #getDefaultCommandID()} is set as default item for the menu. The
	 * double click event is handled by executing the default command.
	 */
	public void setDefaultItemHandling(boolean defaultItemHandling) {
		if (this.defaultItemHandling != defaultItemHandling) {
			if (defaultItemHandling) {
				if (this.viewer instanceof StructuredViewer) {
					((StructuredViewer) this.viewer)
							.addDoubleClickListener(this.listenerExecuteDefaultCommand);
				}
			} else {
				if (this.viewer instanceof StructuredViewer) {
					((StructuredViewer) this.viewer)
							.removeDoubleClickListener(this.listenerExecuteDefaultCommand);
				}
			}
		}
	}

	protected abstract String getDefaultCommandID();

	private final IDoubleClickListener listenerExecuteDefaultCommand = new IDoubleClickListener() {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			ContextMenu.this.menu.notifyListeners(SWT.Show, new Event());
			IHandlerService handlerService = (IHandlerService) ContextMenu.this.site
					.getService(IHandlerService.class);
			try {
				String defaultCommandID = ContextMenu.this
						.getDefaultCommandID();
				if (defaultCommandID != null) {
					handlerService.executeCommand(
							ContextMenu.this.getDefaultCommandID(), null);
				}
			} catch (CommandException e) {
				LOGGER.error("Could not execute command", e);
			}
		}
	};

}