package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.dialogs.ShowArtefactIDDialog;

public class ShowArtifactIDHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(ShowArtifactIDHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<URI> uris = SelectionRetrieverFactory.getSelectionRetriever(
				URI.class).getSelection();
		if (InformationControlManagerUtils.getCurrentInput() instanceof URI) {
			URI input = (URI) InformationControlManagerUtils.getCurrentInput();
			if (!uris.contains(input)) {
				uris.add(input);
			}
		}

		if (uris.size() != 1) {
			LOGGER.warn(ShowArtifactIDHandler.class.getSimpleName()
					+ " called with " + uris.size() + " "
					+ ILocatable.class.getSimpleName() + "s. Should be 1");
			return null;
		}

		boolean doCopy = true;

		String justCopy = event
				.getParameter("de.fu_berlin.imp.apiua.groundedtheory.commands.showArtifactID.justCopy");
		if (justCopy == null || !justCopy.equalsIgnoreCase("true")) {
			ShowArtefactIDDialog artefactIDDialog = new ShowArtefactIDDialog(
					HandlerUtil.getActiveShell(event), uris.get(0));
			doCopy = artefactIDDialog.open() == ShowArtefactIDDialog.COPY_AND_CLOSE_ID;
		}

		if (doCopy) {
			URI id = uris.get(0);

			final Clipboard cb = new Clipboard(HandlerUtil
					.getActiveShell(event).getDisplay());
			TextTransfer transfer = TextTransfer.getInstance();
			cb.setContents(new Object[] { id.toString() },
					new Transfer[] { transfer });
		}

		return null;
	}
}
