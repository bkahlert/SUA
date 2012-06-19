package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
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

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.dnd.CodeTransfer;

public class ResortableCodeViewer extends CodeViewer {

	private static Logger LOGGER = Logger.getLogger(ResortableCodeViewer.class);

	public ResortableCodeViewer(Composite parent, int style) {
		super(parent, style);

		final ISelectionRetriever<ICode> codeRetriever = SelectionRetrieverFactory
				.getSelectionRetriever(ICode.class);
		final ISelectionRetriever<ICodeInstance> instanceRetriever = SelectionRetrieverFactory
				.getSelectionRetriever(ICodeInstance.class);

		int operations = DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { CodeTransfer.getInstance() };

		getViewer().addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					public void dragStart(DragSourceEvent event) {
						if (codeRetriever.getSelection().size() > 0
								|| instanceRetriever.getSelection().size() > 0) {
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					public void dragSetData(DragSourceEvent event) {
						List<Object> objects = new LinkedList<Object>();
						objects.addAll(codeRetriever.getSelection());
						objects.addAll(instanceRetriever.getSelection());
						event.data = objects;
					}

					public void dragFinished(DragSourceEvent event) {

					}
				});

		getViewer().addDropSupport(operations, transferTypes,
				new DropTargetAdapter() {
					public void dragOver(DropTargetEvent event) {
						event.feedback = DND.FEEDBACK_EXPAND
								| DND.FEEDBACK_SCROLL;
						if (event.item != null) {
							Point point = Display.getCurrent().map(null,
									getViewer().getControl(), event.x, event.y);

							Rectangle bounds = null;
							if (event.item instanceof TreeItem)
								bounds = ((TreeItem) event.item).getBounds();
							if (event.item instanceof TableItem)
								bounds = ((TableItem) event.item).getBounds();

							if (event.item.getData() instanceof ICode) {
								if (bounds != null) {
									if (point.y < bounds.y + bounds.height / 3) {
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
							} else if (event.item.getData() instanceof ICodeInstance) {
								if (bounds != null) {
									if (point.y < bounds.y + bounds.height / 2) {
										event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
									} else {
										event.feedback |= DND.FEEDBACK_INSERT_AFTER;
									}
								} else {
									event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
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

						Object[] sourceObjects = ((List<?>) event.data)
								.toArray();
						List<ICode> sourceCodes = ArrayUtils
								.getAdaptableObjects(sourceObjects, ICode.class);
						List<ICodeInstance> sourceCodeInstances = ArrayUtils
								.getAdaptableObjects(sourceObjects,
										ICodeInstance.class);
						if (event.item != null
								&& event.item.getData() instanceof ICode) {
							ICode targetCode = (ICode) event.item.getData();
							for (ICode sourceCode : sourceCodes) {
								try {
									codeService.setParent(sourceCode,
											targetCode);
									LOGGER.info("[CODE][HIERARCHY] Moved "
											+ sourceCodes + " to " + targetCode);
								} catch (CodeServiceException e) {
									LOGGER.error("Coud not make " + targetCode
											+ " the parent of " + sourceCodes);
								}
							}

							if (sourceCodes.size() == 0
									&& sourceCodeInstances.size() > 0) {
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
