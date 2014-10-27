package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView;

public class RemoveHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(RemoveHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		List<ICode> codes = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();

		if (codes.size() > 0) {
			if (part instanceof AxialCodingView) {
				AxialCodingView axialCodingView = (AxialCodingView) part;
				axialCodingView.remove(codes);
			}
		} else {
			LOGGER.warn("Selection did not contain any "
					+ ICode.class.getSimpleName());
		}

		return null;
	}

}
