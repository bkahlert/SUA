package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionEntity;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.IBoldViewer;

public class StartWorkSessionHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(StartWorkSessionHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IWorkSessionEntity> workSessionEntities = SelectionRetrieverFactory
				.getSelectionRetriever(IWorkSessionEntity.class).getSelection();

		if (workSessionEntities.size() > 0) {
			IWorkSessionService workSessionService = (IWorkSessionService) PlatformUI
					.getWorkbench().getService(IWorkSessionService.class);
			if (workSessionService != null) {
				IWorkbenchPart part = HandlerUtil.getActivePart(event);
				ISelectionProvider selectionProvider = part.getSite()
						.getSelectionProvider();
				if (selectionProvider instanceof IBoldViewer) {
					IBoldViewer boldViewer = (IBoldViewer) selectionProvider;
					boldViewer.setBold(workSessionEntities);
				}
				workSessionService.startWorkSession(workSessionEntities);
			}
		}

		return null;
	}
}