package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.AddCodeWizard;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;

public class AddCodeInstanceHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(AddCodeInstanceHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICodeable> codeables = SelectionRetrieverFactory
				.getSelectionRetriever(ICodeable.class).getSelection();

		if (codeables.size() > 0) {
			AddCodeWizard addCodeWizard = WizardUtils
					.openAddCodeWizard(codeables);
			if (addCodeWizard != null) {
				List<ICode> codes = addCodeWizard.getAffectedCodes();
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				for (ICode code : codes) {
					for (ICodeable codeable : codeables) {
						try {
							codeService.addCode(code, codeable);
							LOGGER.info("Code " + code.getCaption()
									+ " added to " + codeable);
						} catch (CodeServiceException e) {
							LOGGER.error("Code " + code.getCaption()
									+ " couldn't be added to " + codeable);
						}
					}
				}
			}
		}

		return null;
	}
}
