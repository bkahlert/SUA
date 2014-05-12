package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.viewer.jointjs.JointJSContentProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSLabelProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSViewer;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.LocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.SelectiveCodingContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.SelectiveCodingLabelProvider;

public class SelectiveCodingView extends ViewPart {

	private static final Logger LOGGER = Logger
			.getLogger(SelectiveCodingView.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private JointJS jointjs = null;
	private JointJSViewer jointjsViewer = null;

	public SelectiveCodingView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.NONE, "sua://code/",
				"sua://code-link");
		JointJSContentProvider contentProvider = new SelectiveCodingContentProvider();
		JointJSLabelProvider labelProvider = new SelectiveCodingLabelProvider();
		this.jointjsViewer = new JointJSViewer(this.jointjs, contentProvider,
				labelProvider);

		this.jointjsViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
		this.jointjsViewer.refresh();

		this.activateDropSupport();
	}

	private void activateDropSupport() {
		int operations = DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };

		DropTarget dropTarget = new DropTarget(this.jointjs.getBrowser().get, // TODO
																				// drop
																				// auf
																				// einem
																				// composer
																				// (oder
																				// enthaltenen
																				// Browser)
																				// hinbekommen
				operations);
		dropTarget.setTransfer(transferTypes);
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				System.err.println(event);
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
				super.dropAccept(event);
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				System.err.println(event);
				List<URI> sourceUris = SelectionUtils.getAdaptableObjects(
						LocalSelectionTransfer.getTransfer().getSelection(),
						URI.class);
				List<URI> sourceCodeUris = URIUtils.filterByResource(
						sourceUris, CodeLocatorProvider.CODE_NAMESPACE);
				sourceUris.removeAll(sourceCodeUris);
				List<URI> sourceCodeInstanceUris = URIUtils.filterByResource(
						sourceUris,
						CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE);
				sourceUris.removeAll(sourceCodeInstanceUris);

				URI destUri = event.item != null
						&& event.item.getData() instanceof URI ? (URI) event.item
						.getData() : null;

				event.feedback = DND.FEEDBACK_SCROLL;
				event.detail = DND.DROP_NONE;
				if (!(sourceCodeUris.size() != 0
						^ sourceCodeInstanceUris.size() != 0 ^ sourceUris
						.size() != 0)) {
				} else if (destUri == null) {
					// target: nothing
					if (sourceCodeUris.size() > 0) {
						event.feedback = DND.FEEDBACK_SELECT
								| DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
						event.detail = DND.DROP_MOVE;
					}
				} else if (CodeLocatorProvider.CODE_NAMESPACE.equals(URIUtils
						.getResource(destUri))) {
					// target: Code

					if (sourceCodeUris.size() > 0) {
						// TODO sortierung von Codes erlauben
						Rectangle bounds = event.item instanceof TreeItem ? ((TreeItem) event.item)
								.getBounds()
								: event.item instanceof TableItem ? ((TableItem) event.item)
										.getBounds() : null;
						if (bounds != null) {
							Point point = Display.getCurrent().map(null,
									SelectiveCodingView.this.jointjs, event.x,
									event.y);
							event.feedback = DND.FEEDBACK_EXPAND
									| DND.FEEDBACK_SCROLL;
							if (point.y < bounds.y + bounds.height / 3) {
								event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
							} else if (point.y > bounds.y + 2 * bounds.height
									/ 3) {
								event.feedback |= DND.FEEDBACK_INSERT_AFTER;
							} else {
								event.feedback |= DND.FEEDBACK_SELECT;
							}
						}

						event.feedback = DND.FEEDBACK_SELECT
								| DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
						event.detail = DND.DROP_MOVE;
					} else if (sourceCodeInstanceUris.size() > 0) {
						try {
							for (ICodeInstance sourceCodeInstance : LocatorService.INSTANCE
									.resolve(sourceCodeInstanceUris,
											ICodeInstance.class, null).get()) {
								if (destUri.equals(sourceCodeInstance.getCode()
										.getUri())) {
									event.feedback = DND.FEEDBACK_EXPAND;
									event.detail = DND.DROP_NONE;
									return;
								}
							}
						} catch (Exception e) {
							LOGGER.error("Could not check if moving "
									+ sourceCodeInstanceUris
									+ " would lead to duplicates", e);
						}

						event.feedback = DND.FEEDBACK_SELECT
								| DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
						event.detail = DND.DROP_MOVE;
					} else {
						event.feedback = DND.FEEDBACK_SELECT
								| DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
						event.detail = DND.DROP_LINK;
					}
				}
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				List<URI> sourceUris = SelectionUtils.getAdaptableObjects(
						LocalSelectionTransfer.getTransfer().getSelection(),
						URI.class);
				List<URI> sourceCodeUris = URIUtils.filterByResource(
						sourceUris, CodeLocatorProvider.CODE_NAMESPACE);
				sourceUris.removeAll(sourceCodeUris);
				List<URI> sourceCodeInstanceUris = URIUtils.filterByResource(
						sourceUris,
						CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE);
				sourceUris.removeAll(sourceCodeInstanceUris);

				URI destUri = event.item != null
						&& event.item.getData() instanceof URI ? (URI) event.item
						.getData() : null;

				try {
					List<ICode> sourceCodes = LocatorService.INSTANCE.resolve(
							sourceCodeUris, ICode.class, null).get();
					List<ICodeInstance> sourceCodeInstances = LocatorService.INSTANCE
							.resolve(sourceCodeInstanceUris,
									ICodeInstance.class, null).get();
					if (!(sourceCodeUris.size() != 0
							^ sourceCodeInstanceUris.size() != 0 ^ sourceUris
							.size() != 0)) {
					} else if (destUri == null) {
						// target: nothing

						for (ICode sourceCode : sourceCodes) {
							// CODE_SERVICE.setParent(sourceCode, null);
							LOGGER.info("[CODE][HIERARCHY] Made " + sourceCodes
									+ " top level");
						}
					} else if (CodeLocatorProvider.CODE_NAMESPACE
							.equals(URIUtils.getResource(destUri))) {
						// target: Code

						ICode targetCode = LocatorService.INSTANCE.resolve(
								destUri, ICode.class, null).get();
						if (sourceCodeUris.size() > 0) {
							for (ICode sourceCode : sourceCodes) {
								// CODE_SERVICE.setParent(sourceCode,
								// targetCode);
								LOGGER.info("[CODE][HIERARCHY] Moved "
										+ sourceCodes + " to " + targetCode);
							}
						} else if (sourceCodeInstanceUris.size() > 0) {
							for (ICodeInstance sourceCodeInstance : sourceCodeInstances) {
								if (sourceCodeInstance.getCode().equals(
										targetCode)) {
									continue;
								}

								URI oldCodeInstanceUri = sourceCodeInstance
										.getUri();
								String memo = CODE_SERVICE
										.loadMemo(oldCodeInstanceUri);

								URI coded = sourceCodeInstance.getId();
								// CODE_SERVICE
								// .deleteCodeInstance(sourceCodeInstance);
							}
						} else {
							for (URI sourceUri : sourceUris) {
								// CODE_SERVICE.addCode(targetCode,
								// sourceUri);
								LOGGER.info("[CODE][ASSIGN] " + sourceUri
										+ " assigned to " + targetCode);
							}
						}
					}
				} catch (Exception e) {
					LOGGER.error("Couln't complete drop action", e);
				}

			}
		});
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	@Override
	public void setFocus() {
		this.jointjs.setFocus();
	}

}
