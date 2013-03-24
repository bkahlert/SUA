package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.dialogs.ShowArtefactIDDialog;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class ShowArtifactIDHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(ShowArtifactIDHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICodeable> codeables = SelectionRetrieverFactory
				.getSelectionRetriever(ICodeable.class).getSelection();
		if (InformationControlManagerUtils.getCurrentInput() instanceof ICodeable) {
			codeables.add((ICodeable) InformationControlManagerUtils
					.getCurrentInput());
		}

		if (codeables.size() != 1) {
			LOGGER.warn(ShowArtifactIDHandler.class.getSimpleName()
					+ " called with " + codeables.size() + " "
					+ ICodeable.class.getSimpleName() + "s. Should be 1");
			return null;
		}

		boolean doCopy = true;

		String justCopy = event
				.getParameter("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.commands.showArtifactID.justCopy");
		if (justCopy == null || !justCopy.equalsIgnoreCase("true")) {
			ShowArtefactIDDialog artefactIDDialog = new ShowArtefactIDDialog(
					HandlerUtil.getActiveShell(event), codeables.get(0));
			doCopy = artefactIDDialog.open() == ShowArtefactIDDialog.COPY_AND_CLOSE_ID;
		}

		if (doCopy) {
			URI id = codeables.get(0).getUri();

			final Clipboard cb = new Clipboard(HandlerUtil
					.getActiveShell(event).getDisplay());
			TextTransfer transfer = TextTransfer.getInstance();
			cb.setContents(new Object[] { id.toString() },
					new Transfer[] { transfer });
		}

		return null;
	}
}
