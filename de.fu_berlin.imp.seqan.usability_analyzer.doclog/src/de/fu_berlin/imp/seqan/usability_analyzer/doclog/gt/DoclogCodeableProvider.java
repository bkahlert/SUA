package de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets.DoclogTimeline;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogExplorerView;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogTimelineView;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeableUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

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
				Object key = new ID(path[0]);
				DoclogFile doclogFile = Activator.getDefault()
						.getDoclogDirectory()
						.getDoclogFile(key, monitorReference.get());
				if (doclogFile == null) {
					key = new Fingerprint(path[0]);
					doclogFile = Activator.getDefault().getDoclogDirectory()
							.getDoclogFile(key, monitorReference.get());
				}
				if (doclogFile == null) {
					LOGGER.error(ID.class.getSimpleName() + " or "
							+ Fingerprint.class
							+ " not specified for coded object retrieval for "
							+ " " + key.toString());
					return null;
				}

				// 1: Record
				if (path.length <= 1)
					return doclogFile;
				String doclogRecordRawContent;
				try {
					doclogRecordRawContent = URLDecoder
							.decode(path[1], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Could no decode name of "
							+ DoclogFile.class.getSimpleName());
					return null;
				}
				for (DoclogRecord doclogRecord : doclogFile.getDoclogRecords()) {
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
	public boolean showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		if (codedObjects.size() > 0) {
			if (!openAndSelectFilesInExplorer(codedObjects,
					(DoclogExplorerView) WorkbenchUtils
							.getView(DoclogExplorerView.ID)))
				return false;
			if (!openAndSelectFilesInTimeline(codedObjects,
					(DoclogTimelineView) WorkbenchUtils
							.getView(DoclogTimelineView.ID)))
				return false;
		}
		return true;
	}

	public boolean openAndSelectFilesInExplorer(
			final List<ICodeable> codedObjects,
			final DoclogExplorerView doclogExplorerView) {
		Set<Object> keys = CodeableUtils.getKeys(codedObjects);

		// open
		Future<Boolean> rt = doclogExplorerView.open(keys,
				new Callable<Boolean>() {
					public Boolean call() {
						TreeViewer viewer = doclogExplorerView
								.getDoclogFilesViewer();
						viewer.setSelection(new StructuredSelection(
								codedObjects), true);
						List<ICodeable> selectedCodeables = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										ICodeable.class);
						return selectedCodeables.size() == codedObjects.size();
					}
				});
		try {
			return rt.get() != null ? rt.get() : false;
		} catch (InterruptedException e) {
			LOGGER.error(e);
			return false;
		} catch (ExecutionException e) {
			LOGGER.error(e);
			return false;
		}
	}

	public boolean openAndSelectFilesInTimeline(
			final List<ICodeable> codedObjects,
			final DoclogTimelineView doclogTimelineView) {
		final Set<Object> keys = CodeableUtils.getKeys(codedObjects);

		// open
		Future<Boolean> rt = doclogTimelineView.open(keys,
				new Callable<Boolean>() {
					public Boolean call() {
						// select
						for (Object key : keys) {
							DoclogTimeline timeline = doclogTimelineView
									.getTimeline(key);
							Set<DoclogRecord> doclogRecords = new HashSet<DoclogRecord>(
									ArrayUtils.getAdaptableObjects(
											codedObjects.toArray(),
											DoclogRecord.class));

							timeline.center(doclogRecords);
							timeline.highlight(doclogRecords);
						}
						return true; // TODO: check if successfull
					}
				});
		try {
			return rt.get() != null ? rt.get() : false;
		} catch (InterruptedException e) {
			LOGGER.error(e);
			return false;
		} catch (ExecutionException e) {
			LOGGER.error(e);
			return false;
		}
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			@Override
			public String getText(Object element) {
				if (element instanceof DoclogFile) {
					DoclogFile doclogFile = (DoclogFile) element;
					if (doclogFile.getID() != null)
						return doclogFile.getID().toString();
					else
						return doclogFile.getFingerprint().toString();
				}
				if (element instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) element;
					TimeZoneDate date = doclogRecord.getDateRange()
							.getStartDate();
					return (date != null) ? date
							.format(new SUACorePreferenceUtil().getDateFormat())
							: "";
				}
				return "";
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) element;
					try {
						return (codeService.getCodes(doclogRecord).size() > 0) ? ImageManager.DOCLOGRECORD_CODED
								: ImageManager.DOCLOGRECORD;
					} catch (CodeServiceException e) {
						return ImageManager.DOCLOGRECORD;
					}
				}
				return super.getImage(element);
			}
		};
	}

}
