package de.fu_berlin.imp.seqan.usability_analyzer.diff.gt;

import java.io.File;
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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffFileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeableUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

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
				DiffFileList diffFiles = Activator.getDefault()
						.getDiffFileDirectory().getDiffFiles(id, monitor.get());

				// 1: Revision
				Integer revision = (path.length >= 1) ? Integer
						.parseInt(path[1]) : null;
				if (revision == null) {
					LOGGER.error("Revision not specified for coded object retrieval for "
							+ ID.class.getSimpleName() + " " + id);
					return null;
				}
				if (diffFiles.size() <= revision) {
					LOGGER.error("There is no revision " + revision
							+ " of the " + DiffFile.class.getSimpleName()
							+ "s with " + ID.class.getSimpleName() + " " + id);
					return null;
				}

				// 2: Record
				DiffFile diffFile = diffFiles.get(revision);
				if (path.length <= 2)
					return diffFile;
				String diffFileRecordName;
				try {

					diffFileRecordName = URLDecoder.decode(path[2], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Could no decode name of "
							+ DiffFileRecord.class.getSimpleName());
					return null;
				}
				for (DiffFileRecord diffFileRecord : diffFile
						.getDiffFileRecords()) {
					if (diffFileRecord.getFilename().equals(diffFileRecordName)) {
						if (codeInstanceID.getFragment() != null) {
							try {
								String[] segment = codeInstanceID.getFragment()
										.split("\\+");
								int segmentStart = Integer.valueOf(segment[0]);
								int segmentLength = Integer.valueOf(segment[1]);
								return new DiffFileRecordSegment(
										diffFileRecord, segmentStart,
										segmentLength);
							} catch (Exception e) {
								LOGGER.error(
										"Could not calculate the "
												+ DiffFileRecordSegment.class
														.getSimpleName()
												+ " from " + codeInstanceID, e);
								return diffFileRecord;
							}
						}
						return diffFileRecord;
					}
				}
				return null;
			}
		};
	}

	@Override
	public boolean showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		if (codedObjects.size() > 0) {
			DiffExplorerView diffExplorerView = (DiffExplorerView) WorkbenchUtils
					.getView(DiffExplorerView.ID);
			if (diffExplorerView == null)
				return false;
			if (!openFiles(codedObjects, diffExplorerView))
				return false;
			if (!openSegments(codedObjects, diffExplorerView))
				return false;
		}

		if (codedObjects.size() > 0) {

		}
		return true;
	}

	public boolean openFiles(final List<ICodeable> codedObjects,
			final DiffExplorerView diffExplorerView) {
		Set<ID> ids = CodeableUtils.getIDs(codedObjects);

		codedObjects.addAll(DiffFileUtils.getRecordsFromSegments(codedObjects));

		// open
		try {
			Future<Boolean> future = diffExplorerView.open(ids,
					new Callable<Boolean>() {
						public Boolean call() {
							DiffFileListsViewer viewer = diffExplorerView
									.getDiffFileListsViewer();
							viewer.setSelection(new StructuredSelection(
									codedObjects), true);
							List<ICodeable> selectedCodeables = SelectionUtils
									.getAdaptableObjects(viewer.getSelection(),
											ICodeable.class);
							return selectedCodeables.size() == codedObjects
									.size();
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
			if (codeable instanceof DiffFileRecordSegment) {
				DiffFileRecordSegment segment = (DiffFileRecordSegment) codeable;
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
		return new LabelProvider() {

			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			@Override
			public String getText(Object element) {
				if (element instanceof DiffFileList) {
					DiffFileList diffFileList = (DiffFileList) element;
					ID id = null;
					if (diffFileList.size() > 0) {
						id = diffFileList.get(0).getID();
					}
					return (id != null) ? id.toString() : "";
				}
				if (element instanceof DiffFile) {
					DiffFile diffFile = (DiffFile) element;
					TimeZoneDate date = diffFile.getDateRange().getStartDate();
					return (date != null) ? date
							.format(new SUACorePreferenceUtil().getDateFormat())
							: "";
				}
				if (element instanceof DiffFileRecord) {
					DiffFileRecord diffFileRecord = (DiffFileRecord) element;
					String name = diffFileRecord.getFilename();
					return (name != null) ? new File(name).getName()
							+ "@"
							+ Integer.parseInt(diffFileRecord.getDiffFile()
									.getRevision()) : "";
				}
				if (element instanceof DiffFileRecordSegment) {
					DiffFileRecordSegment diffFileRecordSegment = (DiffFileRecordSegment) element;
					String name = diffFileRecordSegment.getDiffFileRecord()
							.getFilename();
					return (name != null) ? new File(name).getName() + ": "
							+ diffFileRecordSegment.getSegmentStart() + "+"
							+ diffFileRecordSegment.getSegmentLength() : "";
				}
				return "";
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof DiffFileList) {
					return ImageManager.DIFFFILELIST;
				}
				if (element instanceof DiffFile) {
					// DiffFile diffFile = (DiffFile) element;
					// try {
					// return (codeService.getCodes(diffFile).size() > 0) ?
					// (codeService
					// .isMemo(diffFile) ? ImageManager.DIFFFILE_CODED_MEMO
					// : ImageManager.DIFFFILE_CODED)
					// : (codeService.isMemo(diffFile) ?
					// ImageManager.DIFFFILE_MEMO
					// : ImageManager.DIFFFILE);
					// } catch (CodeServiceException e) {
					// return ImageManager.DIFFFILE;
					// }
					return ImageManager.DIFFFILE;
				}
				if (element instanceof DiffFileRecord) {
					// DiffFileRecord diffFileRecord = (DiffFileRecord) element;
					// try {
					// return (codeService.getCodes(diffFileRecord).size() > 0)
					// ? (codeService
					// .isMemo(diffFileRecord) ?
					// ImageManager.DIFFFILERECORD_CODED_MEMO
					// : ImageManager.DIFFFILERECORD_CODED)
					// : (codeService.isMemo(diffFileRecord) ?
					// ImageManager.DIFFFILERECORD_MEMO
					// : ImageManager.DIFFFILERECORD);
					// } catch (CodeServiceException e) {
					// return ImageManager.DIFFFILERECORD;
					// }
					return ImageManager.DIFFFILERECORD;
				}
				if (element instanceof DiffFileRecordSegment) {
					// DiffFileRecordSegment diffFileRecordSegment =
					// (DiffFileRecordSegment) element;
					// try {
					// return (codeService.getCodes(diffFileRecordSegment)
					// .size() > 0) ? (codeService
					// .isMemo(diffFileRecordSegment) ?
					// ImageManager.DIFFFILERECORDSEGMENT_CODED_MEMO
					// : ImageManager.DIFFFILERECORDSEGMENT_CODED)
					// : (codeService.isMemo(diffFileRecordSegment) ?
					// ImageManager.DIFFFILERECORDSEGMENT_MEMO
					// : ImageManager.DIFFFILERECORDSEGMENT);
					// } catch (CodeServiceException e) {
					// return ImageManager.DIFFFILERECORDSEGMENT;
					// }
					return ImageManager.DIFFFILERECORDSEGMENT;
				}
				return super.getImage(element);
			}
		};
	}
}
