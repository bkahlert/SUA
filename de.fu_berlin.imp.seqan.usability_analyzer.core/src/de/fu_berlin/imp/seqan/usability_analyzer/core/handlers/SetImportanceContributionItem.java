package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

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

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.ImageManager;

public class SetImportanceContributionItem extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(SetImportanceContributionItem.class);

	private Image high;
	private Image default_;
	private Image low;

	public SetImportanceContributionItem() {
		this.createImages();
	}

	public SetImportanceContributionItem(String id) {
		super(id);
		this.createImages();
	}

	private void createImages() {
		this.high = ImageManager.OVERLAY_ERROR.createImage();
		this.default_ = ImageManager.OVERLAY_DIRTY.createImage();
		this.low = ImageManager.OVERLAY_OK.createImage();
	}

	@Override
	public void fill(Menu menu, int index) {
		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart().getSite().getService(ICommandService.class);
		final Command setImportanceCommand = commandService
				.getCommand("de.fu_berlin.imp.seqan.usability_analyzer.core.setImportance");

		final IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart().getSite().getService(IHandlerService.class);

		for (final Object[] item : Arrays.asList(new Object[] { "High",
				Importance.HIGH.toString(), this.high }, new Object[] {
				"Default", Importance.DEFAULT.toString(), this.default_ },
				new Object[] { "Low", Importance.LOW.toString(), this.low })) {
			MenuItem menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText((String) item[0]);
			menuItem.setImage((Image) item[2]);
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						IParameter viewIdParm = setImportanceCommand
								.getParameter("de.fu_berlin.imp.seqan.usability_analyzer.core.setImportance.importance");
						Parameterization parm = new Parameterization(
								viewIdParm, (String) item[1]);
						ParameterizedCommand parmCommand = new ParameterizedCommand(
								setImportanceCommand,
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
		for (Image image : Arrays.asList(this.high, this.default_, this.low)) {
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
		}
		super.dispose();
	}
}
