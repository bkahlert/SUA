package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class CodeEditingSupport extends EditingSupport {

	private static final Logger LOGGER = Logger
			.getLogger(CodeEditingSupport.class);

	public CodeEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected boolean canEdit(Object element) {
		return getCellEditor(element) != null;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		Composite composite = null;
		if (getViewer() instanceof TableViewer)
			composite = ((TableViewer) getViewer()).getTable();
		if (getViewer() instanceof TreeViewer)
			composite = ((TreeViewer) getViewer()).getTree();
		if (composite == null)
			return null;

		if (element instanceof ICode)
			return new TextCellEditor(composite);

		return null;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof ICode) {
			ICode code = (ICode) element;
			return code.getCaption();
		}
		return "ERROR";
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof ICode && value instanceof String) {
			ICode code = (ICode) element;
			String newCaption = (String) value;
			try {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				codeService.renameCode(code, newCaption);
				ViewerUtils.refresh(getViewer(), true);
			} catch (Exception e) {
				LOGGER.error(
						"Could not save changed " + ICode.class.getSimpleName()
								+ " " + value, e);
			}
		}
	}

}
