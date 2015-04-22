package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation.EndPoint;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager;

public class MergeRelationsContribution extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(CreateAxialCondingModelContribution.class);

	private ICodeService codeService;

	public MergeRelationsContribution() {
		this.init();
	}

	public MergeRelationsContribution(String id) {
		super(id);
		this.init();
	}

	private void init() {
		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);
	}

	@Override
	public void fill(Menu menu, int index) {
		ISelection selection = SelectionUtils.getSelection();

		List<IRelation> relations = SelectionUtils.getAdaptableObjects(
				selection, IRelation.class);
		if (relations.size() < 2) {
			return;
		}

		Pair<EndPoint, URI> from = new Pair<>(EndPoint.FROM, relations.get(0)
				.getFrom());
		Pair<EndPoint, URI> to = new Pair<>(EndPoint.FROM, relations.get(0)
				.getFrom());
		for (IRelation relation : relations) {
			if (from != null && !from.getSecond().equals(relation.getFrom())) {
				from = null;
			}
			if (to != null && !to.getSecond().equals(relation.getTo())) {
				to = null;
			}
		}

		MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);
		menuItem.setText("Merge Relations");
		menuItem.setImage(ImageManager.RELATION);

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

		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					mergeRelations(relations);
				} catch (InterruptedException | ExecutionException e1) {
					LOGGER.error("Error generlizing relation "
							+ relations.toString());
				}
			}
		});
	}

	/**
	 * Generalizes a {@link IRelation}.
	 *
	 * @param relations
	 *            to be generalized
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void mergeRelations(List<IRelation> relations)
			throws InterruptedException, ExecutionException {
		Job job = new NamedJob(CreateAxialCondingModelContribution.class,
				"Generalizing " + relations) {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 1);

				Set<IRelation> rest = new HashSet<IRelation>();
				rest.addAll(relations.subList(1, relations.size()));

				try {
					MergeRelationsContribution.this.codeService.merge(rest,
							relations.get(0));
				} catch (Exception e) {
					LOGGER.error("Error merging " + relations, e);
					return Status.CANCEL_STATUS;
				}

				subMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}