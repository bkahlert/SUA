package de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.LocatableUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogExplorerView;

public class DoclogLocatorProvider implements ILocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogLocatorProvider.class);

	public static final String DOCLOG_NAMESPACE = "doclog";

	@Override
	public String[] getAllowedNamespaces() {
		return new String[] { DOCLOG_NAMESPACE };
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		String[] path = uri.getRawPath().substring(1).split("/");

		// 0: ID / Fingerprint
		IIdentifier identifier = IdentifierFactory.createFrom(path[0]);
		Doclog doclog = Activator.getDefault().getDoclogContainer()
				.getDoclogFile(identifier, monitor);
		if (doclog == null) {
			identifier = IdentifierFactory.createFrom(path[0]);
			doclog = Activator.getDefault().getDoclogContainer()
					.getDoclogFile(identifier, monitor);
		}
		if (doclog == null) {
			LOGGER.error(IIdentifier.class.getSimpleName()
					+ " not specified for coded object retrieval for " + " "
					+ identifier.toString());
			return null;
		}

		// 1: Record
		if (path.length <= 1) {
			return doclog;
		}
		String doclogRecordRawContent;
		try {
			doclogRecordRawContent = URLDecoder.decode(path[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Could no decode name of "
					+ Doclog.class.getSimpleName());
			return null;
		}
		for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
			if (doclogRecord.getRawContent().equals(doclogRecordRawContent)) {
				return doclogRecord;
			}
		}
		return null;
	}

	@Override
	public boolean showInWorkspace(ILocatable[] locatables, boolean open,
			IProgressMonitor monitor) {
		if (locatables.length > 0) {
			return this.openAndSelectFilesInExplorer(locatables,
					(DoclogExplorerView) WorkbenchUtils
							.getView(DoclogExplorerView.ID)).length == locatables.length;
		}
		return true;
	}

	public ILocatable[] openAndSelectFilesInExplorer(
			final ILocatable[] locatables,
			final DoclogExplorerView doclogExplorerView) {
		Set<IIdentifier> identifiers = LocatableUtils
				.getIdentifiers(locatables);

		// open
		Future<ILocatable[]> rt = doclogExplorerView.open(identifiers,
				new Callable<ILocatable[]>() {
					@Override
					public ILocatable[] call() {
						TreeViewer viewer = doclogExplorerView
								.getDoclogFilesViewer();
						viewer.setSelection(
								new StructuredSelection(locatables), true);
						List<ILocatable> selectedCodeables = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										ILocatable.class);
						return selectedCodeables.toArray(new ILocatable[0]);
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
