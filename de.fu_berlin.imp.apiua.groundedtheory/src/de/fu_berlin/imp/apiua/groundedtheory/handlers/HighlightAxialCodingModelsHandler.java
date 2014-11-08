package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView;

public class HighlightAxialCodingModelsHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RecolorCodeHandler.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final List<ICode> codes = SelectionRetrieverFactory
				.getSelectionRetriever(ICode.class).getSelection();

		if (InformationControlManagerUtils.getCurrentInput() instanceof ICode) {
			codes.add((ICode) InformationControlManagerUtils.getCurrentInput());
		}

		final List<URI> codeUris = new ArrayList<URI>();
		for (ICode code : codes) {
			codeUris.add(code.getUri());
		}

		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<URI> relevantAcmUris = new ArrayList<URI>();
				if (codes.size() >= 1) {
					try {
						for (URI acmUri : CODE_SERVICE.getAxialCodingModels()) {
							IAxialCodingModel acm = CODE_SERVICE
									.getAxialCodingModel(acmUri);
							for (URI containedCode : acm.getCodes()) {
								if (codeUris.contains(containedCode)) {
									relevantAcmUris.add(acmUri);
								}
							}
						}
					} catch (CodeStoreReadException e) {
						LOGGER.error("Can't read axial coding models");
					}

					AxialCodingView acmView = (AxialCodingView) WorkbenchUtils
							.getView(AxialCodingView.ID, true);
					acmView.open(relevantAcmUris.toArray(new URI[0])).get();
					acmView.highlight(codeUris).get();
				} else {
					LOGGER.warn("Selection did not contain any "
							+ ICode.class.getSimpleName());
				}
				return null;
			}
		});

		return null;
	}

}
