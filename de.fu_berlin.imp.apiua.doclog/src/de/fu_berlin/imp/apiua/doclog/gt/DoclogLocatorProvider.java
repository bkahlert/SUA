package de.fu_berlin.imp.apiua.doclog.gt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.util.WorkbenchUtils;
import de.fu_berlin.imp.apiua.doclog.Activator;
import de.fu_berlin.imp.apiua.doclog.model.Doclog;
import de.fu_berlin.imp.apiua.doclog.model.DoclogRecord;
import de.fu_berlin.imp.apiua.doclog.views.DoclogView;

public class DoclogLocatorProvider extends AdaptingLocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogLocatorProvider.class);

	public static final String DOCLOG_NAMESPACE = "doclog";

	@SuppressWarnings("unchecked")
	public DoclogLocatorProvider() {
		super(Doclog.class, DoclogRecord.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !SUACorePreferences.URI_SCHEME.equalsIgnoreCase(uri.getScheme())
				|| !DOCLOG_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		List<String> trail = URIUtils.getTrail(uri);
		switch (trail.size()) {
		case 0:
			return Doclog.class;
		case 1:
			return DoclogRecord.class;
		}

		LOGGER.error("Unknown " + URI.class.getSimpleName() + " format: " + uri);
		return null;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		IIdentifier identifier = URIUtils.getIdentifier(uri);
		return Activator.getDefault().getDoclogContainer() != null
				&& Activator.getDefault().getDoclogContainer()
						.doclogFileLoaded(identifier);
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)
				|| Activator.getDefault() == null
				|| Activator.getDefault().getDoclogContainer() == null) {
			return null;
		}
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
			LOGGER.error(Doclog.class.getSimpleName()
					+ " missing for coded object retrieval for "
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
	public boolean showInWorkspace(URI[] uris, boolean open,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		if (uris.length > 0) {
			URI[] selected = this.openAndSelectFilesInExplorer(uris,
					(DoclogView) WorkbenchUtils.getView(DoclogView.ID));
			return selected.length == uris.length;
		}
		subMonitor.done();

		return true;
	}

	public URI[] openAndSelectFilesInExplorer(final URI[] uris,
			final DoclogView doclogView) {
		Set<IIdentifier> identifiers = URIUtils.getIdentifiers(uris);

		// open
		Future<URI[]> rt = doclogView.open(identifiers, new Callable<URI[]>() {
			@Override
			public URI[] call() {
				TreeViewer viewer = doclogView.getDoclogFilesViewer();
				viewer.setSelection(new StructuredSelection(uris), true);
				List<URI> selectedLocatables = SelectionUtils
						.getAdaptableObjects(viewer.getSelection(), URI.class);
				return selectedLocatables.toArray(new URI[0]);
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
