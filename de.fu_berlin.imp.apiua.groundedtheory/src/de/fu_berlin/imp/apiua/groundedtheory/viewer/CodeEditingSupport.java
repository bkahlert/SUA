package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIEditingSupport;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class CodeEditingSupport extends URIEditingSupport {

	private static final Logger LOGGER = Logger
			.getLogger(CodeEditingSupport.class);

	public CodeEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(URI element, Composite composite)
			throws Exception {
		if (LocatorService.INSTANCE.resolve(element, ICode.class, null).get() != null) {
			return new TextCellEditor(composite);
		}
		return null;
	}

	@Override
	protected Object getInitValue(URI element) throws Exception {
		ICode code = LocatorService.INSTANCE
				.resolve(element, ICode.class, null).get();
		if (code != null) {
			return code.getCaption();
		}
		return "ERROR";
	}

	@Override
	protected void setEditedValue(URI element, Object value) throws Exception {
		ICode code = LocatorService.INSTANCE
				.resolve(element, ICode.class, null).get();
		if (code != null && value instanceof String) {
			String newCaption = (String) value;
			if (code.getCaption().equals(newCaption)) {
				return;
			}
			try {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				codeService.renameCode(code, newCaption);
				ViewerUtils.refresh(this.getViewer(), true);
			} catch (Exception e) {
				LOGGER.error(
						"Could not save changed " + ICode.class.getSimpleName()
								+ " " + value, e);
			}
		}
	}

}
