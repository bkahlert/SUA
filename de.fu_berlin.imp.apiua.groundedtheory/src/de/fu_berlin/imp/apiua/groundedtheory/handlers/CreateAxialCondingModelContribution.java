package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.LinkedList;
import java.util.List;
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

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.apiua.groundedtheory.ui.Utils;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView;

public class CreateAxialCondingModelContribution extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(CreateAxialCondingModelContribution.class);

	private ILabelProviderService labelProviderService;
	private ICodeService codeService;

	public CreateAxialCondingModelContribution() {
		this.init();
	}

	public CreateAxialCondingModelContribution(String id) {
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
		List<IRelation> relations = SelectionUtils.getAdaptableObjects(
				selection, IRelation.class);
		List<IRelationInstance> relationInstances = SelectionUtils
				.getAdaptableObjects(selection, IRelationInstance.class);
		List<URI> phenomena = SelectionUtils.getAdaptableObjects(selection,
				URI.class);

		String menuName = "";
		List<URI> uris = new LinkedList<>();

		if (codes.size() > 0) {
			menuName = "Create ACM from Code";
			for (ICode code : codes) {
				uris.add(code.getUri());
			}
		} else if (relations.size() > 0) {
			menuName = "Create ACM from Relation";
			for (IRelation relation : relations) {
				uris.add(relation.getUri());
			}
		} else if (relationInstances.size() > 0) {
			menuName = "Create ACM from Relation Grounded by";
			for (IRelationInstance relationInstance : relationInstances) {
				uris.add(relationInstance.getUri());
			}
		} else if (phenomena.size() > 0) {
			menuName = "Create ACM from Phenomenon";
			uris.add(phenomena.get(0));
		}

		if (uris.size() > 0) {
			Menu createAcmSubMenu = null;

			for (URI uri : uris) {
				String name = Utils
						.removeGroundingInformation(CreateAxialCondingModelContribution.this.labelProviderService
								.getText(uri));

				MenuItem menuItem;
				if (uris.size() == 1) {
					menuItem = new MenuItem(menu, SWT.PUSH, index);
					menuItem.setText(menuName + " \"" + name + "\"");
					menuItem.setImage(ImageManager.RELATION);
				} else {
					if (createAcmSubMenu == null) {
						MenuItem createAcmItem = new MenuItem(menu,
								SWT.CASCADE, index);
						createAcmItem.setText(menuName);
						createAcmItem.setImage(ImageManager.RELATION);
						createAcmSubMenu = new Menu(createAcmItem);
						createAcmItem.setMenu(createAcmSubMenu);
					}
					menuItem = new MenuItem(createAcmSubMenu, SWT.PUSH);
					menuItem.setText(name);
				}

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							CreateAxialCondingModelContribution.this
									.createAcmFrom(uri);
						} catch (InterruptedException | ExecutionException e1) {
							LOGGER.error("Error creating ACM for " + uri);
						}
					}
				});
			}
		}
	}

	/**
	 * Creates a new ACM in the ACM view and creates the model itself with the
	 * given {@link URI} as the {@link ICode} in the center of the graph.
	 *
	 * @param uri
	 *            the core element; the {@link IAxialCodingModel} will contain
	 *            all links and elements connected with this one
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void createAcmFrom(URI uri) throws InterruptedException,
			ExecutionException {
		Job job = new NamedJob(CreateAxialCondingModelContribution.class,
				"Creating new axial coding model") {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				try {
					ILocatable locatable = LocatorService.INSTANCE.resolve(uri,
							null).get();

					String title = null;
					if (locatable instanceof IRelationInstance) {
						IRelationInstance relationInstance = (IRelationInstance) locatable;
						title = CreateAxialCondingModelContribution.this.labelProviderService
								.getText(relationInstance.getRelation()
										.getUri());
						title += " (grounded by "
								+ Utils.removeGroundingInformation(CreateAxialCondingModelContribution.this.labelProviderService
										.getText(uri)) + ")";
					} else {
						title = CreateAxialCondingModelContribution.this.labelProviderService
								.getText(uri);
					}
					subMonitor.worked(5);

					IAxialCodingModel acm = CreateAxialCondingModelContribution.this.codeService
							.createAxialCodingModelFrom(uri, title.toString())
							.get();
					subMonitor.worked(80);
					ExecUtils.syncExec(() -> {
						CreateAxialCondingModelContribution.this.codeService
								.addAxialCodingModel(acm);
						return null;
					});
					subMonitor.worked(5);
					for (AxialCodingView axialCodingView : WorkbenchUtils
							.getViews(AxialCodingView.class)) {
						axialCodingView.open(acm.getUri()).get();
						axialCodingView.autoLayoutFocussedACM();
						axialCodingView.fitOnScreenFocussedACM();
						break;
					}
					subMonitor.done();
				} catch (Exception e) {
					LOGGER.error(
							"Error creating "
									+ IAxialCodingModel.class.getSimpleName(),
							e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
