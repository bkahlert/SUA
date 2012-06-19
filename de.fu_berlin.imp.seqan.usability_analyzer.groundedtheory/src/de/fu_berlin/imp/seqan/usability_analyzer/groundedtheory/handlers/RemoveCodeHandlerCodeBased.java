package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView;

public class RemoveCodeHandlerCodeBased extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RemoveCodeHandlerCodeBased.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICode> codes = getSelectedCodes();
		if (codes == null)
			return null;

		CodeInstancesView codeInstanceView = getCodeInstancesView(event);
		if (codeInstanceView == null)
			return null;

		ICodeable codeable = getIndirectlySelectedCodeable(codeInstanceView);
		if (codeable == null)
			return null;

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		for (ICode code : codes) {
			try {
				codeService.removeCode(code, codeable);
			} catch (Exception e) {
				LOGGER.error("Error removing code", e);
			}
		}

		return null;
	}

	private List<ICode> getSelectedCodes() {
		List<ICode> code = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();
		if (code.size() == 0)
			return null;
		return code;
	}

	private CodeInstancesView getCodeInstancesView(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part == null) {
			LOGGER.error(RemoveCodeHandlerCodeBased.class.getSimpleName()
					+ " was activated but the sending part is null");
			return null;
		}

		if (!(part instanceof CodeInstancesView)) {
			LOGGER.error(RemoveCodeHandlerCodeBased.class.getSimpleName()
					+ " was activated but the sending part was not "
					+ CodeInstancesView.class.getSimpleName());
			return null;
		}

		return (CodeInstancesView) part;
	}

	private ICodeable getIndirectlySelectedCodeable(
			CodeInstancesView codeInstanceView) {
		ICodeable codeable = codeInstanceView.getCodeable();
		if (codeable == null) {
			LOGGER.error(RemoveCodeHandlerCodeBased.class.getSimpleName()
					+ " was activated but the sending "
					+ CodeInstancesView.class.getSimpleName()
					+ " has no selected " + ICodeable.class.getSimpleName());
			return null;
		}
		return codeable;
	}
}
