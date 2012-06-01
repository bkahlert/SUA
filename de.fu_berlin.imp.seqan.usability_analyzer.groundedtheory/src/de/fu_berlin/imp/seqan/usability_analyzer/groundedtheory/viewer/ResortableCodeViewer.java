package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class ResortableCodeViewer extends CodeViewer {

	private static Logger LOGGER = Logger.getLogger(ResortableCodeViewer.class);

	public ResortableCodeViewer(Composite parent, int style) {
		super(parent, style);

		int operations = DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };

		getViewer().addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					public void dragStart(DragSourceEvent event) {
						ICode code = getCodeFromSelection();
						if (code != null) {
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					public void dragSetData(DragSourceEvent event) {
						event.data = new Long(getCodeFromSelection().getId())
								.toString();
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
							if (event.item.getData() instanceof ICode) {
								event.feedback |= DND.FEEDBACK_SELECT;
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

						long sourceCodeID = Long.parseLong((String) event.data);
						ICode sourceCode = codeService.getCode(sourceCodeID);
						if (event.item != null
								&& event.item.getData() instanceof ICode) {
							ICode targetCode = (ICode) event.item.getData();
							codeService.setParent(sourceCode, targetCode);
							LOGGER.info("[CODE][HIERARCHY] Moved " + sourceCode
									+ " to " + targetCode);
						} else {
							codeService.setParent(sourceCode, null);
							LOGGER.info("[CODE][HIERARCHY] Made " + sourceCode
									+ " top level");
						}
					}
				});
	}

	private ICode getCodeFromSelection() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof StructuredSelection) {
			Object[] elements = ((StructuredSelection) selection).toArray();
			if (elements.length == 1 && elements[0] instanceof ICode)
				return (ICode) elements[0];
		}
		return null;
	}
}
