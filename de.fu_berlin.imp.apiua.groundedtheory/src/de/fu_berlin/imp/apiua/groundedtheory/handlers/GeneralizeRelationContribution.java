package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.dialogs.SelectCodeDialog;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation.EndPoint;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager;

public class GeneralizeRelationContribution extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(GeneralizeRelationContribution.class);

	private ILabelProviderService labelProviderService;
	private ICodeService codeService;

	public GeneralizeRelationContribution() {
		this.init();
	}

	public GeneralizeRelationContribution(String id) {
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

		List<IRelation> relations = SelectionUtils.getAdaptableObjects(
				selection, IRelation.class);
		if (relations.size() < 1) {
			return;
		}

		Pair<EndPoint, URI> from = new Pair<>(EndPoint.FROM, relations.get(0)
				.getFrom());
		Pair<EndPoint, URI> to = new Pair<>(EndPoint.TO, relations.get(0)
				.getTo());
		for (IRelation relation : relations) {
			if (from != null && !from.getSecond().equals(relation.getFrom())) {
				from = null;
			}
			if (to != null && !to.getSecond().equals(relation.getTo())) {
				to = null;
			}
		}

		MenuItem menuItem = new MenuItem(menu, SWT.CASCADE, index);
		menuItem.setText("Generalize Relation");
		menuItem.setImage(ImageManager.RELATION);
		Menu itemMenu = new Menu(menuItem);
		menuItem.setMenu(itemMenu);

		List<Pair<EndPoint, URI>> endpoints = new LinkedList<>();
		if (from != null) {
			endpoints.add(from);
		}
		if (to != null) {
			endpoints.add(to);
		}

		if (endpoints.isEmpty()) {
			menuItem.setText(menuItem.getText()
					+ " Not Available Due to Different Froms and Tos");
			menuItem.setEnabled(false);
		}

		for (Pair<EndPoint, URI> endpoint : endpoints) {
			MenuItem subMenuItem = new MenuItem(itemMenu, SWT.CASCADE);
			subMenuItem.setText(this.labelProviderService.getText(endpoint
					.getSecond()));
			subMenuItem.setImage(this.labelProviderService.getImage(endpoint
					.getSecond()));
			Menu subMenuItemMenu = new Menu(subMenuItem);
			subMenuItem.setMenu(subMenuItemMenu);

			List<URI> ancestors = this.codeService.getCodeStore()
					.getCodeHierarchyView().getAncestors(endpoint.getSecond());
			for (int i = 0; i < ancestors.size(); i++) {
				URI ancestor = ancestors.get(ancestors.size() - i - 1);
				MenuItem ancestorSubMenuItem = new MenuItem(subMenuItemMenu,
						SWT.PUSH);
				ancestorSubMenuItem.setText(StringUtils.repeat("-", i)
						+ (i == 0 ? "" : " ")
						+ this.labelProviderService.getText(ancestor));
				ancestorSubMenuItem.setImage(this.labelProviderService
						.getImage(ancestor));
				ancestorSubMenuItem
						.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									generalizeRelation(relations,
											endpoint.getFirst(), ancestor);
								} catch (InterruptedException
										| ExecutionException e1) {
									LOGGER.error("Error generlizing relation "
											+ relations.toString());
								}
							}
						});
			}

			MenuItem selfSubMenuItem = new MenuItem(subMenuItemMenu, SWT.PUSH);
			selfSubMenuItem.setText(StringUtils.repeat("-", ancestors.size())
					+ " "
					+ this.labelProviderService.getText(endpoint.getSecond()));
			selfSubMenuItem.setImage(this.labelProviderService
					.getImage(endpoint.getSecond()));
			selfSubMenuItem.setEnabled(false);

			new MenuItem(subMenuItemMenu, SWT.SEPARATOR);

			MenuItem otherSubMenuItem = new MenuItem(subMenuItemMenu, SWT.PUSH);
			otherSubMenuItem.setText("Other...");
			otherSubMenuItem.setImage(ImageManager.CODE);
			otherSubMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SelectCodeDialog d = new SelectCodeDialog(PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), endpoint.getSecond());
					if (d.open() == Window.OK) {
						URI uri = d.getUri();
						try {
							if (LocatorService.INSTANCE.resolve(uri, null)
									.get() instanceof ICode) {
								generalizeRelation(relations,
										endpoint.getFirst(), uri);
							}
						} catch (InterruptedException | ExecutionException e1) {
							LOGGER.error("Error generlizing relation "
									+ relations.toString());
						}
					}
				}
			});
		}
	}

	/**
	 * Generalizes a {@link IRelation}.
	 *
	 * @param relations
	 *            to be generalized
	 * @param endPoint
	 *            to or from field
	 * @param newEndPoint
	 *            new end point
	 *
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void generalizeRelation(List<IRelation> relations,
			EndPoint endPoint, URI newEndPoint) throws InterruptedException,
			ExecutionException {
		Job job = new NamedJob(CreateAxialCondingModelContribution.class,
				"Generalizing " + relations) {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						relations.size());

				for (IRelation relation : relations) {
					try {
						GeneralizeRelationContribution.this.codeService
								.updateRelation(relation, endPoint, newEndPoint);
					} catch (Exception e) {
						LOGGER.error("Error generalizing " + relation, e);
						return Status.CANCEL_STATUS;
					}
				}
				subMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}