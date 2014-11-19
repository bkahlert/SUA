package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.dialogs.RenameDialog;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.Triple;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager;

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
		List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
				URI.class);

		List<Pair<URI, URI>> creatableLinks = new LinkedList<>();
		List<Pair<URI, IRelation>> groundableLinks = new LinkedList<>();
		List<Triple<URI, URI, URI>> creatableGroundedLinks = new LinkedList<>();

		if (codes.size() >= 2) {
			for (ICode code1 : codes) {
				for (ICode code2 : codes) {
					if (code1.equals(code2)) {
						continue;
					}
					creatableLinks.add(new Pair<>(code1.getUri(), code2
							.getUri()));
				}
			}
		}

		if (uris.size() == 1) {
			URI uri = uris.get(0);
			List<ICodeInstance> codeInstances = this.codeService
					.getInstances(uri);
			for (ICodeInstance codeInstance1 : codeInstances) {
				for (ICodeInstance codeInstance2 : codeInstances) {
					URI fromUri = codeInstance1.getCode().getUri();
					URI toUri = codeInstance2.getCode().getUri();
					if (fromUri.equals(toUri)) {
						continue;
					}

					creatableGroundedLinks
							.add(new Triple<>(uri, fromUri, toUri));

					for (IRelation relation : this.codeService.getRelations()) {
						if (relation.getFrom().equals(fromUri)
								&& relation.getTo().equals(toUri)) {
							groundableLinks.add(new Pair<URI, IRelation>(uri,
									relation));
						}
					}
				}
			}
		}

		if (creatableLinks.size() == 0 && groundableLinks.size() == 0
				&& creatableGroundedLinks.size() == 0) {
			return;
		}

		MenuItem createLinkItem = new MenuItem(menu, SWT.CASCADE, index);
		createLinkItem.setText("Create Link");
		createLinkItem.setImage(ImageManager.RELATION);
		final Menu createLinkSubMenu = new Menu(createLinkItem);
		createLinkItem.setMenu(createLinkSubMenu);

		if (creatableLinks.size() > 0) {
			this.createSpacer(createLinkSubMenu, "New Link:");

			for (Pair<URI, URI> creatableLink : creatableLinks) {
				MenuItem menuItem = new MenuItem(createLinkSubMenu, SWT.PUSH);

				String fromName = this.labelProviderService
						.getText(creatableLink.getFirst());
				String toName = this.labelProviderService.getText(creatableLink
						.getSecond());

				menuItem.setText(fromName + " → " + toName);
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						CreateLinkContribution.this.createRelation(
								menu.getShell(), creatableLink.getFirst(),
								creatableLink.getSecond());
					}
				});
			}
		}

		if (creatableGroundedLinks.size() > 0) {
			this.createSpacer(createLinkSubMenu, "New Grounded Link:");

			for (Triple<URI, URI, URI> creatableGroundedLink : creatableGroundedLinks) {
				MenuItem menuItem = new MenuItem(createLinkSubMenu, SWT.PUSH);

				String fromName = this.labelProviderService
						.getText(creatableGroundedLink.getSecond());
				String toName = this.labelProviderService
						.getText(creatableGroundedLink.getThird());

				menuItem.setText(fromName + " → " + toName);
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						IRelation relation = CreateLinkContribution.this
								.createRelation(menu.getShell(),
										creatableGroundedLink.getSecond(),
										creatableGroundedLink.getThird());
						if (relation != null) {
							CreateLinkContribution.this.groundRelation(
									menu.getShell(),
									creatableGroundedLink.getFirst(), relation);
						}
					}
				});

				if (this.codeService.isGrounded(
						creatableGroundedLink.getFirst(),
						creatableGroundedLink.getSecond(),
						creatableGroundedLink.getThird())) {
					menuItem.setText(menuItem.getText() + " (already grounded)");
					menuItem.setEnabled(false);
					break;
				}
			}
		}

		if (groundableLinks.size() > 0) {
			this.createSpacer(createLinkSubMenu, "Ground Link:");

			for (Pair<URI, IRelation> groundableLink : groundableLinks) {
				MenuItem menuItem = new MenuItem(createLinkSubMenu, SWT.PUSH);

				String fromName = this.labelProviderService
						.getText(groundableLink.getSecond().getFrom());
				String toName = this.labelProviderService
						.getText(groundableLink.getSecond().getTo());

				menuItem.setText(fromName + " → "
						+ groundableLink.getSecond().getName() + " → " + toName);
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						CreateLinkContribution.this.groundRelation(
								menu.getShell(), groundableLink.getFirst(),
								groundableLink.getSecond());
					}
				});

				if (this.codeService.isGrounded(groundableLink.getFirst(),
						groundableLink.getSecond())) {
					menuItem.setText(menuItem.getText() + " (already grounded)");
					menuItem.setEnabled(false);
					break;
				}
			}
		}
	}

	private void createSpacer(Menu menu, String text) {
		if (menu.getItemCount() > 0) {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setEnabled(false);
		item.setText(text);
	}

	private IRelation createRelation(Shell shell, URI from, URI to) {
		RenameDialog renameDialog = new RenameDialog(shell, "");
		renameDialog.create();
		if (renameDialog.open() == Window.OK) {
			final String relationName = renameDialog.getCaption();
			try {
				return CreateLinkContribution.this.codeService.createRelation(
						from, to, relationName);
			} catch (Exception e) {
				LOGGER.error("Error creating relation " + from + " → " + to);
			}
		}
		return null;
	}

	private void groundRelation(Shell shell, URI uri, IRelation relation) {
		try {
			CreateLinkContribution.this.codeService.createRelationInstance(uri,
					relation);
		} catch (Exception e) {
			LOGGER.error("Error grounding relation " + uri + " → " + relation);
		}
	}

}
