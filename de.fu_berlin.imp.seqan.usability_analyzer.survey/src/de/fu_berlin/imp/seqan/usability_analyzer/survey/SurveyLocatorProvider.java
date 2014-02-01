package de.fu_berlin.imp.seqan.usability_analyzer.survey;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocumentField;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.views.CDView;

public class SurveyLocatorProvider extends AdaptingLocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(SurveyLocatorProvider.class);

	public static final String SURVEY_NAMESPACE = "survey";

	@SuppressWarnings("unchecked")
	public SurveyLocatorProvider() {
		super(CDDocument.class, CDDocumentField.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !"sua".equalsIgnoreCase(uri.getScheme())
				|| !SURVEY_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		List<String> trail = URIUtils.getTrail(uri);
		switch (trail.size()) {
		case 0:
			return null;
		case 1:
			return CDDocument.class;
		case 2:
			return CDDocumentField.class;
		}

		LOGGER.error("Unknown " + URI.class.getSimpleName() + " format: " + uri);
		return null;
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		Collection<CDDocument> cdDocuments = Activator.getDefault()
				.getSurveyContainer().getCDDocuments();
		SubMonitor subMonitor = SubMonitor.convert(monitor, cdDocuments.size());
		for (CDDocument cdDocument : cdDocuments) {
			if (cdDocument.getUri().equals(uri)) {
				subMonitor.done();
				return cdDocument;
			}
			for (CDDocumentField cdDocumentField : cdDocument) {
				if (cdDocumentField.getUri().equals(uri)) {
					subMonitor.done();
					return cdDocumentField;
				}
			}
			subMonitor.worked(1);
		}
		subMonitor.done();
		return null;
	}

	@Override
	public boolean showInWorkspace(URI[] uris, boolean open,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);
		ILocatable[] locatables;
		try {
			locatables = locatorService.resolve(uris, subMonitor.newChild(1))
					.get();
		} catch (Exception e) {
			LOGGER.error("Error resolving " + uris);
			return false;
		}

		if (locatables.length > 0) {
			ILocatable[] selected = this.selectInCDView(locatables,
					(CDView) WorkbenchUtils.getView(CDView.ID));
			return selected != null && selected.length == uris.length;
		}
		return true;
	}

	public ILocatable[] selectInCDView(final ILocatable[] locatables,
			final CDView cdView) {

		// open
		Future<ILocatable[]> rt = cdView.open(locatables,
				new Callable<ILocatable[]>() {
					@Override
					public ILocatable[] call() {
						// TreeViewer viewer = cdView.getDoclogFilesViewer();
						// viewer.setSelection(
						// new StructuredSelection(URIS), true);
						// List<ILocatable> selectedLocatables = SelectionUtils
						// .getAdaptableObjects(viewer.getSelection(),
						// ILocatable.class);
						// return selectedLocatables.toArray(new ILocatable[0]);
						return null;
					}
				});
		try {
			return rt.get();
		} catch (InterruptedException e) {
			LOGGER.error(e);
			return null;
		} catch (ExecutionException e) {
			LOGGER.error(e);
			return null;
		}
	}

}
