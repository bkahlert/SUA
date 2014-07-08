package de.fu_berlin.imp.apiua.groundedtheory;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.util.WorkbenchUtils;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView;

public class AxialCodingModelLocatorProvider extends AdaptingLocatorProvider {

	public static final String AXIAL_CODING_MODEL_NAMESPACE = "axialcodingmodel";

	public static final URI createUniqueURI() {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		try {
			int max = -1;
			for (URI uri : codeService.getAxialCodingModels()) {
				max = Math
						.max(max, Integer.valueOf(URIUtils.getIdentifier(uri)
								.toString()));
			}
			return new URI("apiua://" + AXIAL_CODING_MODEL_NAMESPACE + "/"
					+ (max + 1));
		} catch (CodeStoreReadException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingModelLocatorProvider.class);

	@SuppressWarnings("unchecked")
	public AxialCodingModelLocatorProvider() {
		super(IAxialCodingModel.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !SUACorePreferences.URI_SCHEME.equalsIgnoreCase(uri.getScheme())
				|| !AXIAL_CODING_MODEL_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return IAxialCodingModel.class;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		return true;
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		try {
			return codeService.getAxialCodingModel(uri);
		} catch (CodeStoreReadException e) {
			LOGGER.error("Error retrieving " + IAxialCodingModel.class
					+ " for " + uri);
		}

		return null;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length == 1 && uris[0] != null) {
			// TODO check for all included URIs and zoom to and select them

			AxialCodingView axialCodingView = (AxialCodingView) WorkbenchUtils
					.getView(AxialCodingView.ID);
			if (uris[0].equals(axialCodingView.getOpenedURI())) {
				axialCodingView.setFocus();
			} else if (open) {
				try {
					axialCodingView.open(uris[0]).get();
				} catch (Exception e) {
					LOGGER.error("Error showing "
							+ IAxialCodingModel.class.getSimpleName() + " "
							+ uris[0]);
					return false;
				}
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

}
