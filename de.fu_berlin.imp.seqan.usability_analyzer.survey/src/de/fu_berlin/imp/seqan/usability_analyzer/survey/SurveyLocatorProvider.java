package de.fu_berlin.imp.seqan.usability_analyzer.survey;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocumentField;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.views.CDView;

public class SurveyLocatorProvider implements ILocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(SurveyLocatorProvider.class);

	public static final String SURVEY_NAMESPACE = "survey";

	@Override
	public String[] getAllowedNamespaces() {
		return new String[] { SURVEY_NAMESPACE };
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
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
	public boolean showInWorkspace(ILocatable[] locatables, boolean open,
			IProgressMonitor monitor) {
		if (locatables.length > 0) {
			ILocatable[] selected = this.selectInCDView(locatables,
					(CDView) WorkbenchUtils.getView(CDView.ID));
			return selected.length == locatables.length;
		}
		return true;
	}

	public ILocatable[] selectInCDView(
			final ILocatable[] locatables, final CDView cdView) {

		// open
		Future<ILocatable[]> rt = cdView.open(locatables,
				new Callable<ILocatable[]>() {
					@Override
					public ILocatable[] call() {
						// TreeViewer viewer = cdView.getDoclogFilesViewer();
						// viewer.setSelection(
						// new StructuredSelection(locatables), true);
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
