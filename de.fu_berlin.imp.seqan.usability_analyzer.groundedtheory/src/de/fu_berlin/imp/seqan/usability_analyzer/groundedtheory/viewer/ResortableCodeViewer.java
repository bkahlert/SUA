package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
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

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class ResortableCodeViewer extends CodeViewer {

	private static Logger LOGGER = Logger.getLogger(ResortableCodeViewer.class);

	public ResortableCodeViewer(Composite parent, int style,
			ShowInstances showInstances, String saveExpandedElementsKey,
			Filterable filterable, QuickSelectionMode quickSelectionMode) {
		super(parent, style, showInstances, saveExpandedElementsKey,
				filterable, quickSelectionMode);

		int operations = DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };

		final ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		if (this.getViewer() == null) {
			return;
		}

		this.getViewer().addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					final ISelectionRetriever<URI> uriRetriever = SelectionRetrieverFactory
							.getSelectionRetriever(URI.class);

					@Override
					public void dragStart(DragSourceEvent event) {
						List<URI> uris = this.uriRetriever.getSelection();
						if (uris.size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									new StructuredSelection(uris));
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					@Override
					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {

					}
				});

		this.getViewer().addDropSupport(operations, transferTypes,
				new DropTargetAdapter() {
					@Override
					public void dragOver(DropTargetEvent event) {
						List<URI> sourceUris = SelectionUtils
								.getAdaptableObjects(LocalSelectionTransfer
										.getTransfer().getSelection(),
										URI.class);
						List<URI> sourceCodeUris = URIUtils.filterByResource(
								sourceUris, CodeLocatorProvider.CODE_NAMESPACE);
						sourceUris.removeAll(sourceCodeUris);
						List<URI> sourceCodeInstanceUris = URIUtils
								.filterByResource(
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
										| DND.FEEDBACK_EXPAND
										| DND.FEEDBACK_SCROLL;
								event.detail = DND.DROP_MOVE;
							}
						} else if (CodeLocatorProvider.CODE_NAMESPACE
								.equals(URIUtils.getResource(destUri))) {
							// target: Code

							if (sourceCodeUris.size() > 0) {
								// TODO sortierung von Codes erlauben
								Rectangle bounds = event.item instanceof TreeItem ? ((TreeItem) event.item)
										.getBounds()
										: event.item instanceof TableItem ? ((TableItem) event.item)
												.getBounds() : null;
								if (bounds != null) {
									Point point = Display.getCurrent().map(
											null,
											ResortableCodeViewer.this
													.getViewer().getControl(),
											event.x, event.y);
									event.feedback = DND.FEEDBACK_EXPAND
											| DND.FEEDBACK_SCROLL;
									if (point.y < bounds.y + bounds.height / 3) {
										event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
									} else if (point.y > bounds.y + 2
											* bounds.height / 3) {
										event.feedback |= DND.FEEDBACK_INSERT_AFTER;
									} else {
										event.feedback |= DND.FEEDBACK_SELECT;
									}
								}

								event.feedback = DND.FEEDBACK_SELECT
										| DND.FEEDBACK_EXPAND
										| DND.FEEDBACK_SCROLL;
								event.detail = DND.DROP_MOVE;
							} else if (sourceCodeInstanceUris.size() > 0) {
								try {
									for (ICodeInstance sourceCodeInstance : locatorService
											.resolve(sourceCodeInstanceUris,
													ICodeInstance.class, null)
											.get()) {
										if (destUri.equals(sourceCodeInstance
												.getCode().getUri())) {
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
										| DND.FEEDBACK_EXPAND
										| DND.FEEDBACK_SCROLL;
								event.detail = DND.DROP_MOVE;
							} else {
								event.feedback = DND.FEEDBACK_SELECT
										| DND.FEEDBACK_EXPAND
										| DND.FEEDBACK_SCROLL;
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

						List<URI> sourceUris = SelectionUtils
								.getAdaptableObjects(LocalSelectionTransfer
										.getTransfer().getSelection(),
										URI.class);
						List<URI> sourceCodeUris = URIUtils.filterByResource(
								sourceUris, CodeLocatorProvider.CODE_NAMESPACE);
						sourceUris.removeAll(sourceCodeUris);
						List<URI> sourceCodeInstanceUris = URIUtils
								.filterByResource(
										sourceUris,
										CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE);
						sourceUris.removeAll(sourceCodeInstanceUris);

						URI destUri = event.item != null
								&& event.item.getData() instanceof URI ? (URI) event.item
								.getData() : null;

						try {
							List<ICode> sourceCodes = locatorService.resolve(
									sourceCodeUris, ICode.class, null).get();
							List<ICodeInstance> sourceCodeInstances = locatorService
									.resolve(sourceCodeInstanceUris,
											ICodeInstance.class, null).get();
							if (!(sourceCodeUris.size() != 0
									^ sourceCodeInstanceUris.size() != 0 ^ sourceUris
									.size() != 0)) {
							} else if (destUri == null) {
								// target: nothing

								for (ICode sourceCode : sourceCodes) {
									try {
										codeService.setParent(sourceCode, null);
										LOGGER.info("[CODE][HIERARCHY] Made "
												+ sourceCodes + " top level");
									} catch (CodeServiceException e) {
										LOGGER.error("Coud not make "
												+ sourceCodes + " a top level "
												+ ICode.class.getSimpleName());
									}
								}
							} else if (CodeLocatorProvider.CODE_NAMESPACE
									.equals(URIUtils.getResource(destUri))) {
								// target: Code

								ICode targetCode = locatorService.resolve(
										destUri, ICode.class, null).get();
								if (sourceCodeUris.size() > 0) {
									for (ICode sourceCode : sourceCodes) {
										try {
											codeService.setParent(sourceCode,
													targetCode);
											LOGGER.info("[CODE][HIERARCHY] Moved "
													+ sourceCodes
													+ " to "
													+ targetCode);
										} catch (CodeServiceException e) {
											LOGGER.error("Coud not make "
													+ targetCode
													+ " the parent of "
													+ sourceCodes);
										}
									}
								} else if (sourceCodeInstanceUris.size() > 0) {
									for (ICodeInstance sourceCodeInstance : sourceCodeInstances) {
										if (sourceCodeInstance.getCode()
												.equals(targetCode)) {
											continue;
										}

										try {
											URI oldCodeInstanceUri = sourceCodeInstance
													.getUri();
											String memo = codeService
													.loadMemo(oldCodeInstanceUri);

											URI coded = sourceCodeInstance
													.getId();
											codeService
													.deleteCodeInstance(sourceCodeInstance);

											URI newCodeInstanceUri = codeService
													.addCode(targetCode, coded);
											codeService.setMemo(
													newCodeInstanceUri, memo);
											codeService.setMemo(
													oldCodeInstanceUri, null);
										} catch (CodeServiceException e) {
											LOGGER.error(e);
										}
									}
								} else {
									for (URI sourceUri : sourceUris) {
										try {
											codeService.addCode(targetCode,
													sourceUri);
											LOGGER.info("[CODE][ASSIGN] "
													+ sourceUri
													+ " assigned to "
													+ targetCode);
										} catch (CodeServiceException e) {
											LOGGER.error("Coud not assign "
													+ sourceUri + " to "
													+ targetCode);
										}
									}
								}
							}
						} catch (Exception e) {
							LOGGER.error("Couln't complete drop action", e);
						}

					}
				});
	}
}
