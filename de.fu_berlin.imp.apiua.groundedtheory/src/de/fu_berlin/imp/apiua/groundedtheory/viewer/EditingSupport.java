package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIEditingSupport;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class EditingSupport extends URIEditingSupport {

	private static final Logger LOGGER = Logger.getLogger(EditingSupport.class);

	public EditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(URI element, Composite composite)
			throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(element, null)
				.get();
		if (locatable instanceof ICode || locatable instanceof IRelation) {
			return new TextCellEditor(composite);
		}
		return null;
	}

	@Override
	protected Object getInitValue(URI element) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(element, null)
				.get();
		if (locatable instanceof ICode) {
			return ((ICode) locatable).getCaption();
		}
		if (locatable instanceof IRelation) {
			return ((IRelation) locatable).getName();
		}
		return "ERROR";
	}

	@Override
	protected void setEditedValue(URI element, Object value) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(element, null)
				.get();
		if (locatable instanceof ICode && value instanceof String) {
			ICode code = (ICode) locatable;
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
		if (locatable instanceof IRelation && value instanceof String) {
			IRelation relation = (IRelation) locatable;
			String newName = (String) value;
			if (relation.getName().equals(newName)) {
				return;
			}
			try {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				codeService.renameRelation(relation, newName);
				ViewerUtils.refresh(this.getViewer(), true);
			} catch (Exception e) {
				LOGGER.error(
						"Could not save changed "
								+ IRelation.class.getSimpleName() + " " + value,
						e);
			}
		}
	}

}
