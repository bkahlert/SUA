package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class RemoveCodeHandlerInstanceBased extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RemoveCodeHandlerInstanceBased.class);

	@SuppressWarnings("serial")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICodeInstance> codeInstances = SelectionRetrieverFactory
				.getSelectionRetriever(ICodeInstance.class).getSelection();

		if (codeInstances.size() == 0) {
			return null;
		}

		boolean delete = MessageDialog
				.openQuestion(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(),
						"Remove Code"
								+ ((codeInstances.size() != 1) ? "s" : ""),
						"Do you really want to remove code \""
								+ codeInstances.get(0).getCode()
								+ "\" from the following objects:\n"
								+ StringUtils.join(codeInstances.toArray(),
										"\n"));

		if (delete) {
			ILocatorService locatorService = (ILocatorService) PlatformUI
					.getWorkbench().getService(ILocatorService.class);
			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			for (final ICodeInstance codeInstance : codeInstances) {
				try {
					ILocatable locatable = locatorService.resolve(
							codeInstance.getId(), null).get();
					if (locatable == null) {
						locatable = new ILocatable() {
							@Override
							public URI getUri() {
								return codeInstance.getId();
							}
						};
					}
					codeService.removeCodes(
							Arrays.asList(codeInstance.getCode()), locatable);
				} catch (Exception e) {
					LOGGER.error("Error removing code", e);
				}
			}
		}

		return null;
	}
}
