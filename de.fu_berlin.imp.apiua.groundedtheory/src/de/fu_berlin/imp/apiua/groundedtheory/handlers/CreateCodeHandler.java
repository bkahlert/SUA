package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.ui.Utils;
import de.fu_berlin.imp.apiua.groundedtheory.ui.wizards.WizardUtils;

public class CreateCodeHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CreateCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Collection<?> menus = HandlerUtil.getActiveMenus(event);

		List<ICode> codes = menus != null && menus.size() > 0 ? SelectionRetrieverFactory
				.getSelectionRetriever(ICode.class).getSelection()
				: new LinkedList<ICode>();
		assert codes.size() < 2;

		WizardUtils.openNewCodeWizard(codes.size() == 1 ? codes.get(0) : null,
				Utils.getFancyCodeColor());

		return null;
	}

}
