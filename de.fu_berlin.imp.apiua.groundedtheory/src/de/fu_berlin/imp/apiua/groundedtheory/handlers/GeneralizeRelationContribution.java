package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.Arrays;
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
		if (relations.size() != 1)
			return;

		IRelation relation = relations.get(0);

		MenuItem menuItem = new MenuItem(menu, SWT.CASCADE, index);
		menuItem.setText("Generalize Relation");
		menuItem.setImage(ImageManager.RELATION);
		Menu itemMenu = new Menu(menuItem);
		menuItem.setMenu(itemMenu);

		Pair<EndPoint, URI> from = new Pair<>(EndPoint.FROM, relation.getFrom());
		Pair<EndPoint, URI> to = new Pair<>(EndPoint.TO, relation.getTo());
		for (Pair<EndPoint, URI> endpoint : Arrays.asList(from, to)) {
			MenuItem subMenuItem = new MenuItem(itemMenu, SWT.CASCADE);
			subMenuItem.setText(labelProviderService.getText(endpoint
					.getSecond()));
			subMenuItem.setImage(labelProviderService.getImage(endpoint
					.getSecond()));
			Menu subMenuItemMenu = new Menu(subMenuItem);
			subMenuItem.setMenu(subMenuItemMenu);

			List<URI> ancestors = codeService.getCodeStore()
					.getCodeHierarchyView().getAncestors(endpoint.getSecond());
			for (int i = 0; i < ancestors.size(); i++) {
				URI ancestor = ancestors.get(ancestors.size() - i - 1);
				MenuItem ancestorSubMenuItem = new MenuItem(subMenuItemMenu,
						SWT.PUSH);
				ancestorSubMenuItem.setText(StringUtils.repeat("-", i)
						+ (i == 0 ? "" : " ")
						+ labelProviderService.getText(ancestor));
				ancestorSubMenuItem.setImage(labelProviderService
						.getImage(ancestor));
				ancestorSubMenuItem
						.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									generalizeRelation(relation,
											endpoint.getFirst(), ancestor);
								} catch (InterruptedException
										| ExecutionException e1) {
									LOGGER.error("Error generlizing relation "
											+ relation.toString());
								}
							}
						});
			}

			MenuItem selfSubMenuItem = new MenuItem(subMenuItemMenu, SWT.PUSH);
			selfSubMenuItem.setText(StringUtils.repeat("-", ancestors.size())
					+ " " + labelProviderService.getText(endpoint.getSecond()));
			selfSubMenuItem.setImage(labelProviderService.getImage(endpoint
					.getSecond()));
			selfSubMenuItem.setEnabled(false);
		}
	}

	/**
	 * Generalizes a {@link IRelation}.
	 * 
	 * @param relation
	 *            to be generalized
	 * @param endPoint
	 *            to or from field
	 * @param newEndPoint
	 *            new end point
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void generalizeRelation(IRelation relation, EndPoint endPoint,
			URI newEndPoint) throws InterruptedException, ExecutionException {
		Job job = new NamedJob(CreateAxialCondingModelContribution.class,
				"Generalizing " + relation) {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 1);

				try {
					codeService.updateRelation(relation, endPoint, newEndPoint);
					subMonitor.done();
				} catch (Exception e) {
					LOGGER.error("Error generalizing " + relation, e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}