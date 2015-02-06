package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.HLS;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class RecolorCodesHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RecolorCodeHandler.class);

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICode> codes = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();

		if (InformationControlManagerUtils.getCurrentInput() instanceof ICode) {
			codes.add((ICode) InformationControlManagerUtils.getCurrentInput());
		}

		if (codes.size() == 1) {

			ICode code = codes.get(0);
			List<ICode> childCodes = this.codeService.getChildren(code);
			List<HLS> colors = ColorUtils.rainbow(childCodes.size());

			try {
				List<Pair<ICode, RGB>> newColors = new ArrayList<>();
				this.recolor(newColors, childCodes, colors);
				this.codeService.recolorCode(newColors);
			} catch (CodeServiceException e) {
				LOGGER.error("Error recoloring codes", e);
			}
		} else {
			LOGGER.warn("Selection did not only contain a single "
					+ ICode.class.getSimpleName());
		}

		return null;
	}

	public void recolor(List<Pair<ICode, RGB>> newColors, List<ICode> codes,
			List<HLS> colors) throws CodeServiceException {
		Assert.isLegal(codes.size() == colors.size());
		for (int i = 0; i < colors.size(); i++) {
			ICode code = codes.get(i);
			newColors.add(new Pair<ICode, RGB>(code, ColorSpaceConverter
					.HLStoRGB(colors.get(i))));

			List<ICode> childCodes = this.codeService.getChildren(code);
			this.recolor(newColors, childCodes,
					ColorUtils.rainbow(childCodes.size(), colors, i));
		}
	}

}
