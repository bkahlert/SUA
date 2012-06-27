package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
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

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class ResortableCodeViewer extends CodeViewer {

	private static Logger LOGGER = Logger.getLogger(ResortableCodeViewer.class);

	public ResortableCodeViewer(Composite parent, int style) {
		super(parent, style);

		int operations = DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };

		getViewer().addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					final ISelectionRetriever<ICode> codeRetriever = SelectionRetrieverFactory
							.getSelectionRetriever(ICode.class);
					final ISelectionRetriever<ICodeInstance> instanceRetriever = SelectionRetrieverFactory
							.getSelectionRetriever(ICodeInstance.class);

					public void dragStart(DragSourceEvent event) {
						List<ICode> codes = codeRetriever.getSelection();
						List<ICodeInstance> instances = instanceRetriever
								.getSelection();
						if (codeRetriever.getSelection().size() > 0
								|| instanceRetriever.getSelection().size() > 0) {
							List<Object> elements = new LinkedList<Object>();
							elements.addAll(codes);
							elements.addAll(instances);
							LocalSelectionTransfer.getTransfer().setSelection(
									new StructuredSelection(elements));
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					public void dragFinished(DragSourceEvent event) {

					}
				});

		getViewer().addDropSupport(operations, transferTypes,
				new DropTargetAdapter() {
					public void dragOver(DropTargetEvent event) {
						ISelection selection = LocalSelectionTransfer
								.getTransfer().getSelection();
						List<ICode> sourceCodes = SelectionUtils
								.getAdaptableObjects(selection, ICode.class);
						List<ICodeInstance> sourceCodeInstances = SelectionUtils
								.getAdaptableObjects(selection,
										ICodeInstance.class);
						List<ICodeable> sourceCodeables = SelectionUtils
								.getAdaptableObjects(selection, ICodeable.class);

						if (event.item != null
								&& (sourceCodes.size() != 0
										^ sourceCodeInstances.size() != 0 ^ sourceCodeables
										.size() != 0)) {
							event.feedback = DND.FEEDBACK_EXPAND
									| DND.FEEDBACK_SCROLL;

							Point point = Display.getCurrent().map(null,
									getViewer().getControl(), event.x, event.y);

							Rectangle bounds = null;
							if (event.item instanceof TreeItem)
								bounds = ((TreeItem) event.item).getBounds();
							if (event.item instanceof TableItem)
								bounds = ((TableItem) event.item).getBounds();

							if (event.item.getData() instanceof ICode) {
								if (sourceCodes.size() != 0
										|| sourceCodeInstances.size() != 0) {
									if (bounds != null) {
										if (point.y < bounds.y + bounds.height
												/ 3) {
											event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
										} else if (point.y > bounds.y + 2
												* bounds.height / 3) {
											event.feedback |= DND.FEEDBACK_INSERT_AFTER;
										} else {
											event.feedback |= DND.FEEDBACK_SELECT;
										}
									} else {
										event.feedback |= DND.FEEDBACK_SELECT;
									}
								} else {
									event.feedback |= DND.FEEDBACK_SELECT;
									event.detail = DND.DROP_LINK;
								}
							} else if (event.item.getData() instanceof ICodeInstance) {
								if (sourceCodes.size() != 0
										|| sourceCodeInstances.size() != 0) {
									if (bounds != null) {
										if (point.y < bounds.y + bounds.height
												/ 2) {
											event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
										} else {
											event.feedback |= DND.FEEDBACK_INSERT_AFTER;
										}
									} else {
										event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
									}
								} else {
									event.feedback = DND.FEEDBACK_NONE;
									event.detail = DND.DROP_NONE;
								}
							}
						}
					}

					public void drop(DropTargetEvent event) {
						if (event.data == null) {
							event.detail = DND.DROP_NONE;
							return;
						}

						ICodeService codeService = (ICodeService) PlatformUI
								.getWorkbench().getService(ICodeService.class);
						if (codeService == null)
							return;

						ISelection selection = LocalSelectionTransfer
								.getTransfer().getSelection();
						List<ICode> sourceCodes = SelectionUtils
								.getAdaptableObjects(selection, ICode.class);
						List<ICodeInstance> sourceCodeInstances = SelectionUtils
								.getAdaptableObjects(selection,
										ICodeInstance.class);
						List<ICodeable> sourceCodeables = SelectionUtils
								.getAdaptableObjects(selection, ICodeable.class);

						if (event.item != null
								&& event.item.getData() instanceof ICode) {
							ICode targetCode = (ICode) event.item.getData();

							if (sourceCodes.size() > 0) {
								for (ICode sourceCode : sourceCodes) {
									try {
										codeService.setParent(sourceCode,
												targetCode);
										LOGGER.info("[CODE][HIERARCHY] Moved "
												+ sourceCodes + " to "
												+ targetCode);
									} catch (CodeServiceException e) {
										LOGGER.error("Coud not make "
												+ targetCode
												+ " the parent of "
												+ sourceCodes);
									}
								}
							} else if (sourceCodeInstances.size() > 0) {
								for (ICodeInstance sourceCodeInstance : sourceCodeInstances) {
									ICodeable coded = codeService
											.getCodedObject(sourceCodeInstance
													.getId());
									try {
										codeService
												.deleteCodeInstance(sourceCodeInstance);
										codeService.addCode(targetCode, coded);
									} catch (CodeServiceException e) {
										LOGGER.error(e);
									}
								}
							} else if (sourceCodeables.size() > 0) {
								for (ICodeable sourceCodeable : sourceCodeables) {
									try {
										codeService.addCode(targetCode,
												sourceCodeable);
										LOGGER.info("[CODE][ASSIGN] "
												+ sourceCodeable
												+ " assigned to " + targetCode);
									} catch (CodeServiceException e) {
										LOGGER.error("Coud not assign "
												+ sourceCodeable + " to "
												+ targetCode);
									}
								}
							}
						} else {
							for (ICode sourceCode : sourceCodes) {
								try {
									codeService.setParent(sourceCode, null);
									LOGGER.info("[CODE][HIERARCHY] Made "
											+ sourceCodes + " top level");
								} catch (CodeServiceException e) {
									LOGGER.error("Coud not make " + sourceCodes
											+ " a top level "
											+ ICode.class.getSimpleName());
								}
							}
						}
					}
				});
	}
}
