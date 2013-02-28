package de.fu_berlin.imp.seqan.usability_analyzer.diff.gt;

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

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.DiffLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffRecordUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffListsViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeableUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;

public class DiffCodeableProvider extends CodeableProvider {

	private static final Logger LOGGER = Logger
			.getLogger(DiffCodeableProvider.class);

	public static final String DIFF_NAMESPACE = "diff";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(DIFF_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			final AtomicReference<IProgressMonitor> monitor,
			final URI codeInstanceID) {
		return new Callable<ICodeable>() {
			@Override
			public ICodeable call() throws Exception {
				String[] path = codeInstanceID.getRawPath().substring(1)
						.split("/");

				// 0: ID
				ID id = new ID(path[0]);
				IDiffs diffFiles = Activator.getDefault()
						.getDiffDataContainer().getDiffFiles(id, monitor.get());

				// 1: Revision
				Integer revision = (path.length >= 1) ? Integer
						.parseInt(path[1]) : null;
				if (revision == null) {
					LOGGER.error("Revision not specified for coded object retrieval for "
							+ ID.class.getSimpleName() + " " + id);
					return null;
				}
				if (diffFiles.length() <= revision) {
					LOGGER.error("There is no revision " + revision
							+ " of the " + Diff.class.getSimpleName()
							+ "s with " + ID.class.getSimpleName() + " " + id);
					return null;
				}

				// 2: Record
				IDiff diff = diffFiles.get(revision);
				if (path.length <= 2)
					return diff;
				String diffFileRecordName;
				try {

					diffFileRecordName = URLDecoder.decode(path[2], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Could no decode name of "
							+ DiffRecord.class.getSimpleName());
					return null;
				}
				for (DiffRecord diffRecord : diff.getDiffFileRecords()) {
					if (diffRecord.getFilename().equals(diffFileRecordName)) {
						if (codeInstanceID.getFragment() != null) {
							try {
								String[] segment = codeInstanceID.getFragment()
										.split("\\+");
								int segmentStart = Integer.valueOf(segment[0]);
								int segmentLength = Integer.valueOf(segment[1]);
								return new DiffRecordSegment(diffRecord,
										segmentStart, segmentLength);
							} catch (Exception e) {
								LOGGER.error(
										"Could not calculate the "
												+ DiffRecordSegment.class
														.getSimpleName()
												+ " from " + codeInstanceID, e);
								return diffRecord;
							}
						}
						return diffRecord;
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
			DiffExplorerView diffExplorerView = (DiffExplorerView) WorkbenchUtils
					.getView(DiffExplorerView.ID);
			if (diffExplorerView == null)
				return null;
			if (!openFiles(codedObjects, diffExplorerView))
				return null;
			if (!openSegments(codedObjects, diffExplorerView))
				return null;
		}

		return codedObjects.toArray(new ICodeable[0]);
	}

	public boolean openFiles(final List<ICodeable> codedObjects,
			final DiffExplorerView diffExplorerView) {
		Set<ID> ids = CodeableUtils.getIDs(codedObjects);

		codedObjects.addAll(DiffRecordUtils
				.getRecordsFromSegments(codedObjects));

		// open
		try {
			Future<Boolean> future = diffExplorerView.open(ids,
					new Callable<Boolean>() {
						public Boolean call() {
							final DiffListsViewer viewer = diffExplorerView
									.getDiffFileListsViewer();
							try {
								List<ICodeable> selectedCodeables;
								selectedCodeables = ExecutorUtil
										.syncExec(new Callable<List<ICodeable>>() {
											@Override
											public List<ICodeable> call()
													throws Exception {
												viewer.setSelection(
														new StructuredSelection(
																codedObjects),
														true);
												return SelectionUtils.getAdaptableObjects(
														viewer.getSelection(),
														ICodeable.class);
											}
										});
								return selectedCodeables.size() == codedObjects
										.size();
							} catch (Exception e) {
								return false;
							}
						}
					});
			Boolean rt = future.get();
			return rt != null ? rt : false;
		} catch (InterruptedException e) {
			LOGGER.error(e);
			return false;
		} catch (ExecutionException e) {
			LOGGER.error(e);
			return false;
		}
	}

	public boolean openSegments(final List<ICodeable> codedObjects,
			final DiffExplorerView diffExplorerView) {
		for (ICodeable codeable : codedObjects) {
			if (codeable instanceof DiffRecordSegment) {
				DiffRecordSegment segment = (DiffRecordSegment) codeable;
				DiffFileEditorUtils.openCompareEditor(segment
						.getDiffFileRecord());
				// TODO: Highlight segment
			}
		}
		return true; // TODO: make it only return true if at least one of the
						// existing segements could be opened
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new DiffLabelProvider();
	}
}
