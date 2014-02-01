package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import java.net.URI;
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
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.IBoldViewer;

public class StartWorkSessionHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(StartWorkSessionHandler.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<URI> uris = SelectionRetrieverFactory.getSelectionRetriever(
				URI.class).getSelection();

		try {
			List<IWorkSessionEntity> workSessionEntities = locatorService
					.resolve(uris, IWorkSessionEntity.class, null).get();
			if (workSessionEntities.size() > 0) {
				final IWorkSessionService workSessionService = (IWorkSessionService) PlatformUI
						.getWorkbench().getService(IWorkSessionService.class);
				if (workSessionService != null) {
					IWorkbenchPart part = HandlerUtil.getActivePart(event);
					ISelectionProvider selectionProvider = part.getSite()
							.getSelectionProvider();
					if (selectionProvider instanceof IBoldViewer) {
						@SuppressWarnings("unchecked")
						IBoldViewer<URI> boldViewer = (IBoldViewer<URI>) selectionProvider;
						boldViewer.setBold(uris);
					}
					workSessionService.startWorkSession(workSessionEntities
							.toArray(new IWorkSessionEntity[0]));
				}
			}
		} catch (Exception e) {
			throw new ExecutionException("Could not start work session for "
					+ uris, e);
		}

		return null;
	}

}