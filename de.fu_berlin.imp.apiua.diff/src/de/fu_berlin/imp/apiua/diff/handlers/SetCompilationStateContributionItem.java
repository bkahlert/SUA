package de.fu_berlin.imp.apiua.diff.handlers;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import de.fu_berlin.imp.apiua.core.ui.ImageManager;

public class SetCompilationStateContributionItem extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(SetCompilationStateContributionItem.class);

	private Image working;
	private Image notworking;
	private Image unknown;

	public SetCompilationStateContributionItem() {
		this.createImages();
	}

	public SetCompilationStateContributionItem(String id) {
		super(id);
		this.createImages();
	}

	private void createImages() {
		this.working = ImageManager.OVERLAY_OK.createImage();
		this.notworking = ImageManager.OVERLAY_ERROR.createImage();
		this.unknown = ImageManager.OVERLAY_DIRTY.createImage();
	}

	@Override
	public void fill(Menu menu, int index) {
		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart().getSite().getService(ICommandService.class);
		final Command setCompilationStateCommand = commandService
				.getCommand("de.fu_berlin.imp.apiua.diff.setCompilationState");

		final IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart().getSite().getService(IHandlerService.class);

		for (final Object[] item : Arrays.asList(new Object[] { "Compiles",
				"true", this.working }, new Object[] { "Not Compiles", "false",
				this.notworking }, new Object[] { "Unknown", "null",
				this.unknown })) {
			MenuItem menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText((String) item[0]);
			menuItem.setImage((Image) item[2]);
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						IParameter viewIdParm = setCompilationStateCommand
								.getParameter("de.fu_berlin.imp.apiua.diff.setCompilationState.compiles");
						Parameterization parm = new Parameterization(
								viewIdParm, (String) item[1]);
						ParameterizedCommand parmCommand = new ParameterizedCommand(
								setCompilationStateCommand,
								new Parameterization[] { parm });
						handlerService.executeCommand(parmCommand, null);
					} catch (NotDefinedException e) {
						LOGGER.error(e);
					} catch (ExecutionException e) {
						LOGGER.error(e);
					} catch (NotEnabledException e) {
						LOGGER.error(e);
					} catch (NotHandledException e) {
						LOGGER.error(e);
					}
				}
			});
		}
	}

	@Override
	public void dispose() {
		for (Image image : Arrays.asList(this.working, this.notworking,
				this.unknown)) {
			if (image != null && !image.isDisposed())
				image.dispose();
		}
		super.dispose();
	}
}
