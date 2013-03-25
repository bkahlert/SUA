package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;

public class AddCodeInstanceHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AddCodeInstanceHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICodeable> codeables = SelectionRetrieverFactory
				.getSelectionRetriever(ICodeable.class).getSelection();
		if (InformationControlManagerUtils.getCurrentInput() instanceof ICodeable) {
			ICodeable input = (ICodeable) InformationControlManagerUtils
					.getCurrentInput();
			if (!codeables.contains(input)) {
				codeables.add(input);
			}
		}

		if (codeables.size() > 0) {
			WizardUtils.openAddCodeWizard(codeables, Utils.getFancyCodeColor());
		}

		return null;
	}
}
