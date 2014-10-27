package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class HighlightAxialCodingModelsHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RecolorCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICode> codes = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();

		if (InformationControlManagerUtils.getCurrentInput() instanceof ICode) {
			codes.add((ICode) InformationControlManagerUtils.getCurrentInput());
		}

		if (codes.size() >= 1) {
			ICode code = codes.get(0);
			ColorDialog dialog = new ColorDialog(new Shell(
					Display.getDefault(), SWT.SHELL_TRIM));
			dialog.setRGB(code.getColor().toClassicRGB());
			org.eclipse.swt.graphics.RGB newColor = dialog.open();
			if (newColor != null) {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				try {
					codeService.recolorCode(code, new RGB(newColor));
				} catch (CodeServiceException e) {
					LOGGER.error("Error replacing the "
							+ ICode.class.getSimpleName() + "'s color");
				}
			}
		} else {
			LOGGER.warn("Selection did not contain any "
					+ ICode.class.getSimpleName());
		}

		return null;
	}

}
