package de.fu_berlin.imp.apiua.diff.gt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.diff.Activator;
import de.fu_berlin.imp.apiua.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.apiua.diff.model.IDiff;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecordSegment;
import de.fu_berlin.imp.apiua.diff.model.IDiffs;
import de.fu_berlin.imp.apiua.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.apiua.diff.model.impl.DiffRecordSegment;
import de.fu_berlin.imp.apiua.diff.util.DiffRecordUtils;
import de.fu_berlin.imp.apiua.diff.viewer.DiffViewer;
import de.fu_berlin.imp.apiua.diff.views.DiffView;

public class DiffLocatorProvider extends AdaptingLocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(DiffLocatorProvider.class);

	public static final String DIFF_NAMESPACE = "diff";

	@SuppressWarnings("unchecked")
	public DiffLocatorProvider() {
		super(IDiffs.class, IDiff.class, IDiffRecord.class,
				IDiffRecordSegment.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !SUACorePreferences.URI_SCHEME.equalsIgnoreCase(uri.getScheme())
				|| !DIFF_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		// apiua://diff/o6lmo5tpxvn3b6fg/00000048/bin%2Fsandbox%2Fmy_sandbox%2Fapps%2Fmy_app%2Fmy_app.dir%2FDebug%2Fmy_app.log#25+134

		List<String> trail = URIUtils.getTrail(uri);
		switch (trail.size()) {
		case 0:
			return IDiffs.class;
		case 1:
			return IDiff.class;
		case 2:
			if (uri.getFragment() == null) {
				return IDiffRecord.class;
			} else {
				return IDiffRecordSegment.class;
			}
		}

		LOGGER.error("Unknown " + URI.class.getSimpleName() + " format: " + uri);
		return null;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		IIdentifier id = URIUtils.getIdentifier(uri);
		return Activator.getDefault().getDiffDataContainer() != null
				&& Activator.getDefault().getDiffDataContainer()
						.diffsLoaded(id);
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)
				|| Activator.getDefault() == null
				|| Activator.getDefault().getDiffDataContainer() == null) {
			return null;
		}

		List<String> trail = URIUtils.getTrail(uri);

		// 0: ID
		IIdentifier id = URIUtils.getIdentifier(uri);
		IDiffs diffFiles = Activator.getDefault().getDiffDataContainer()
				.getDiffFiles(id, monitor);
		if (diffFiles == null) {
			return null;
		}

		// 1: Revision
		String revision = (trail.size() > 0) ? trail.get(0) : null;
		if (revision == null) {
			return diffFiles;
		}

		// 2: Record
		IDiff diff = null;
		for (IDiff diffFile : diffFiles) {
			if (diffFile.getRevision().equals(revision)) {
				diff = diffFile;
				break;
			}
		}
		if (trail.size() < 2 || diff == null) {
			return diff;
		}
		String diffFileRecordName;
		try {
			diffFileRecordName = URLDecoder.decode(trail.get(1), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Could no decode name of "
					+ DiffRecord.class.getSimpleName());
			return null;
		}
		for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
			if (diffRecord.getFilename().equals(diffFileRecordName)) {
				if (uri.getFragment() != null) {
					try {
						String[] segment = uri.getFragment().split("\\+");
						int segmentStart = Integer.valueOf(segment[0]);
						int segmentLength = Integer.valueOf(segment[1]);
						return new DiffRecordSegment(diffRecord, segmentStart,
								segmentLength);
					} catch (Exception e) {
						LOGGER.error("Could not calculate the "
								+ DiffRecordSegment.class.getSimpleName()
								+ " from " + uri, e);
						return diffRecord;
					}
				}
				return diffRecord;
			}
		}
		return null;
	}

	@Override
	public boolean showInWorkspace(URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length > 0) {
			DiffView diffView = (DiffView) WorkbenchUtils.getView(DiffView.ID);
			if (diffView == null) {
				return false;
			}
			if (this.openFiles(uris, diffView).length != uris.length) {
				return false;
			}
			if (!this.openSegments(uris, diffView, monitor)) {
				return false;
			}
		}
		return true;
	}

	public URI[] openFiles(final URI[] uris, final DiffView diffView) {
		final List<URI> open = new ArrayList<URI>();
		open.addAll(Arrays.asList(uris));
		open.addAll(DiffRecordUtils.getRecordsFromSegments(uris));

		// open
		try {
			Set<IIdentifier> ids = URIUtils.getIdentifiers(uris);
			Future<URI[]> future = diffView.open(ids, new Callable<URI[]>() {
				@Override
				public URI[] call() {
					final DiffViewer viewer = diffView.getDiffFileListsViewer();
					try {
						List<URI> selectedLocatables = ExecUtils
								.syncExec(new Callable<List<URI>>() {
									@Override
									public List<URI> call() throws Exception {
										viewer.setSelection(
												new StructuredSelection(open),
												true);
										return SelectionUtils
												.getAdaptableObjects(
														viewer.getSelection(),
														URI.class);
									}
								});
						return selectedLocatables.toArray(new URI[0]);
					} catch (Exception e) {
						return new URI[0];
					}
				}
			});
			return future.get();
		} catch (InterruptedException e) {
			LOGGER.error(e);
			return new URI[0];
		} catch (ExecutionException e) {
			LOGGER.error(e);
			return new URI[0];
		}
	}

	public boolean openSegments(final URI[] uris, final DiffView diffView,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, uris.length);
		ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);
		for (URI uri : uris) {
			try {
				ILocatable locatable = locatorService.resolve(uri, null).get();
				if (locatable instanceof IDiffRecordSegment) {
					IDiffRecordSegment segment = (IDiffRecordSegment) locatable;
					DiffFileEditorUtils.openCompareEditor(segment
							.getDiffFileRecord());
					// TODO: Highlight segment
				}
			} catch (Exception e) {
				LOGGER.error("Error opening " + uri, e);
			}
			subMonitor.worked(1);
		}
		subMonitor.done();
		return true; // TODO: make it only return true if at least one of the
						// existing segements could be opened
	}

}
