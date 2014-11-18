package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.dialogs.RenameDialog;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.DuplicateRelationException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.RelationDoesNotExistException;

public class CreateLinkContribution extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(CreateLinkContribution.class);

	private ILabelProviderService labelProviderService;
	private ICodeService codeService;

	public CreateLinkContribution() {
		this.init();
	}

	public CreateLinkContribution(String id) {
		super(id);
		this.init();
	}

	private void init() {
		this.labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);
	}

	@Override
	public void fill(Menu menu, int index) {
		ISelection selection = SelectionUtils.getSelection();

		List<ICode> codes = SelectionUtils.getAdaptableObjects(selection,
				ICode.class);
		List<ICodeInstance> codeInstances = SelectionUtils.getAdaptableObjects(
				selection, ICodeInstance.class);

		if (codes.size() >= 2) {
			for (ICode code1 : codes) {
				for (ICode code2 : codes) {
					if (code1.equals(code2)) {
						continue;
					}

					MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);

					AtomicReference<String> caption1 = new AtomicReference<>();
					try {
						caption1.set(this.labelProviderService
								.getLabelProvider(code1.getUri()).getText(
										code1.getUri()));
					} catch (Exception e1) {
						caption1.set("ERROR");
					}

					AtomicReference<String> caption2 = new AtomicReference<>();
					try {
						caption2.set(this.labelProviderService
								.getLabelProvider(code2.getUri()).getText(
										code2.getUri()));
					} catch (Exception e1) {
						caption1.set("ERROR");
					}

					menuItem.setText(caption1 + " → " + caption2);
					menuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							RenameDialog renameDialog = new RenameDialog(menu
									.getShell(), "");
							renameDialog.create();
							if (renameDialog.open() == Window.OK) {
								final String relationName = renameDialog
										.getCaption();
								ExecUtils.nonUIAsyncExec(new Runnable() {
									@Override
									public void run() {
										try {
											CreateLinkContribution.this.codeService
													.createRelation(
															code1.getUri(),
															code2.getUri(),
															relationName);
										} catch (CodeStoreWriteException
												| RelationDoesNotExistException
												| DuplicateRelationException e) {
											LOGGER.error("Error creating relation\n\t "
													+ code1.getUri()
													+ ": "
													+ caption1.get()
													+ "\n\t"
													+ code2.getUri()
													+ ": "
													+ caption2.get());
										}
									}
								});
							}
						}
					});
				}
			}
		}
	}

}
