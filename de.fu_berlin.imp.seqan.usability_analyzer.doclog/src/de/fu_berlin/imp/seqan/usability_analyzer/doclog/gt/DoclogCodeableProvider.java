package de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.DoclogLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogExplorerView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeableUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;

public class DoclogCodeableProvider extends CodeableProvider {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogCodeableProvider.class);

	public static final String DOCLOG_NAMESPACE = "doclog";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(DOCLOG_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			final AtomicReference<IProgressMonitor> monitorReference,
			final URI codeInstanceID) {
		return new Callable<ICodeable>() {
			@Override
			public ICodeable call() throws Exception {
				String[] path = codeInstanceID.getRawPath().substring(1)
						.split("/");

				// 0: ID / Fingerprint
				IIdentifier identifier = IdentifierFactory.createFrom(path[0]);
				Doclog doclog = Activator.getDefault().getDoclogContainer()
						.getDoclogFile(identifier, monitorReference.get());
				if (doclog == null) {
					identifier = IdentifierFactory.createFrom(path[0]);
					doclog = Activator.getDefault().getDoclogContainer()
							.getDoclogFile(identifier, monitorReference.get());
				}
				if (doclog == null) {
					LOGGER.error(IIdentifier.class.getSimpleName()
							+ " not specified for coded object retrieval for "
							+ " " + identifier.toString());
					return null;
				}

				// 1: Record
				if (path.length <= 1) {
					return doclog;
				}
				String doclogRecordRawContent;
				try {
					doclogRecordRawContent = URLDecoder
							.decode(path[1], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Could no decode name of "
							+ Doclog.class.getSimpleName());
					return null;
				}
				for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
					if (doclogRecord.getRawContent().equals(
							doclogRecordRawContent)) {
						return doclogRecord;
					}
				}
				return null;
			}
		};
	}

	@Override
	public ICodeable[] showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		if (codedObjects.size() > 0) {
			return this.openAndSelectFilesInExplorer(codedObjects,
					(DoclogExplorerView) WorkbenchUtils
							.getView(DoclogExplorerView.ID));
		}
		return null;
	}

	public ICodeable[] openAndSelectFilesInExplorer(
			final List<ICodeable> codedObjects,
			final DoclogExplorerView doclogExplorerView) {
		Set<IIdentifier> identifiers = CodeableUtils
				.getIdentifiers(codedObjects);

		// open
		Future<ICodeable[]> rt = doclogExplorerView.open(identifiers,
				new Callable<ICodeable[]>() {
					@Override
					public ICodeable[] call() {
						TreeViewer viewer = doclogExplorerView
								.getDoclogFilesViewer();
						viewer.setSelection(new StructuredSelection(
								codedObjects), true);
						List<ICodeable> selectedCodeables = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										ICodeable.class);
						return selectedCodeables.toArray(new ICodeable[0]);
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

	@Override
	public ILabelProvider getLabelProvider() {
		return new DoclogLabelProvider();
	}
}
