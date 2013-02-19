package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

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

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class RecolorCodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RecolorCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICode> codes = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();

		if (codes.size() == 1) {
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
			LOGGER.warn("Selection did not only contain a single "
					+ ICode.class.getSimpleName());
		}

		return null;
	}

}
