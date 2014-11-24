package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager;
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

		String menuName = "";
		List<Pair<URI, URI>> uris = new LinkedList<>();

		if (codes.size() > 0) {
			menuName = "Create ACM from Code";
			for (ICode code : codes) {
				uris.add(new Pair<>(code.getUri(), null));
			}
		} else if (relations.size() > 0) {
			menuName = "Create ACM from Relation";
			for (IRelation relation : relations) {
				uris.add(new Pair<>(relation.getFrom(), null));
				uris.add(new Pair<>(relation.getTo(), null));
			}
		} else if (relationInstances.size() > 0) {
			menuName = "Create ACM from Phenomenon";
			for (IRelationInstance relationInstance : relationInstances) {
				uris.add(new Pair<>(relationInstance.getRelation().getFrom(),
						relationInstance.getPhenomenon()));
				uris.add(new Pair<>(relationInstance.getRelation().getTo(),
						relationInstance.getPhenomenon()));
			}
		}

		if (uris.size() > 0) {
			Menu createAcmSubMenu = null;

			for (Pair<URI, URI> uri : uris) {
				String phenomenonName = this.labelProviderService.getText(uri
						.getFirst());

				MenuItem menuItem;
				if (uris.size() == 1) {
					menuItem = new MenuItem(menu, SWT.PUSH, index);
					menuItem.setText(menuName + " using " + phenomenonName);
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
					menuItem.setText("using " + phenomenonName);
				}

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							CreateAxialCondingModelContribution.this
									.createAcmFrom(uri.getFirst(),
											uri.getSecond());
						} catch (InterruptedException | ExecutionException e1) {
							LOGGER.error("Error creating ACM for " + uri);
						}
					}
				});
			}
		}
	}

	private void createAcmFrom(URI uri, URI phenomenon)
			throws InterruptedException, ExecutionException {
		Set<URI> codes = new HashSet<>();
		codes.add(uri);
		boolean codesAdded = true;
		while (codesAdded) {
			codesAdded = false;
			for (IRelation relation : this.codeService.getRelations()) {
				if (codes.contains(relation.getFrom())
						&& !codes.contains(relation.getTo())) {
					codes.add(relation.getTo());
					codesAdded = true;
				}
				if (codes.contains(relation.getTo())
						&& !codes.contains(relation.getFrom())) {
					codes.add(relation.getFrom());
					codesAdded = true;
				}
			}
		}
		List<URI> relations = this.codeService
				.getRelations()
				.stream()
				.filter(r -> codes.contains(r.getFrom())
						|| codes.contains(r.getTo())).map(r -> r.getUri())
				.collect(Collectors.toList());

		for (AxialCodingView axialCodingView : WorkbenchUtils
				.getViews(AxialCodingView.class)) {
			axialCodingView.createAxialCodingModel(
					this.labelProviderService.getText(uri), codes, relations);
			break;
		}
	}

}
