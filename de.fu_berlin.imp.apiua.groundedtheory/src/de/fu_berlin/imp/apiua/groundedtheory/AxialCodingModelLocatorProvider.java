package de.fu_berlin.imp.apiua.groundedtheory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.WorkbenchUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingComposite;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView;

public class AxialCodingModelLocatorProvider extends AdaptingLocatorProvider {

	public static final String AXIAL_CODING_MODEL_NAMESPACE = "axialcodingmodel";

	public static final URI createUniqueAxialCodingModelURI() {
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
			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			Set<URI> openACMs = new HashSet<URI>();
			Set<URI> highlightACMs = new HashSet<URI>();
			Set<URI> highlightNodes = new HashSet<URI>();
			try {
				for (URI acmUri : codeService.getAxialCodingModels()) {
					if (ArrayUtils.contains(uris, acmUri)) {
						highlightACMs.add(acmUri);
						if (open) {
							highlightACMs.add(acmUri);
						}
					}

					for (URI code : codeService.getAxialCodingModel(acmUri)
							.getCodes()) {
						if (ArrayUtils.contains(uris, code)) {
							highlightNodes.add(code);
							if (open) {
								openACMs.add(acmUri);
							}
						}
					}

				}

				AxialCodingView axialCodingView = (AxialCodingView) WorkbenchUtils
						.getView(AxialCodingView.ID);

				// open ACMs
				Set<URI> load = axialCodingView.getOpenedURIs().keySet();
				load.addAll(openACMs);
				axialCodingView.open(load.toArray(new URI[0])).get();

				// highlight ACMs
				for (URI acmUri : axialCodingView.getOpenedURIs().keySet()) {
					if (highlightACMs.contains(acmUri)) {
						axialCodingView.getOpenedURIs().get(acmUri).setFocus();
					}
				}

				// highlight nodes
				for (AxialCodingComposite axialCodingComposite : axialCodingView
						.getOpenedURIs().values()) {
					axialCodingComposite.highlight(new LinkedList<URI>(
							highlightNodes));
				}

				return true;
			} catch (Exception e1) {
				LOGGER.error(
						"Error loading "
								+ IAxialCodingModel.class.getSimpleName()
								+ "s to show " + uris + " in workspace", e1);
			}

			return false;
		}
		return true;
	}

}
