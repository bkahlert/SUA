package de.fu_berlin.imp.seqan.usability_analyzer.timeline.gt;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView;

// TODO funktioniert nicht, da für keine Datenquelle zuständig; insbesondere showCodedObjectsinWorkspace; eine Art locator einführen
public class TimelineCodeableProvider extends CodeableProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(TimelineCodeableProvider.class);

	public static final String TIMELINE_NAMESPACE = "timeline";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(TIMELINE_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			final AtomicReference<IProgressMonitor> monitorReference,
			final URI codeInstanceID) {
		return null;
		// return new Callable<ICodeable>() {
		// @Override
		// public ICodeable call() throws Exception {
		// String[] path = codeInstanceID.getRawPath().substring(1)
		// .split("/");
		//
		// // 0: ID / Fingerprint
		// Object key = new ID(path[0]);
		// Doclog doclog = Activator.getDefault().getDoclogContainer()
		// .getDoclogFile(key, monitorReference.get());
		// if (doclog == null) {
		// key = new Fingerprint(path[0]);
		// doclog = Activator.getDefault().getDoclogContainer()
		// .getDoclogFile(key, monitorReference.get());
		// }
		// if (doclog == null) {
		// LOGGER.error(ID.class.getSimpleName() + " or "
		// + Fingerprint.class
		// + " not specified for coded object retrieval for "
		// + " " + key.toString());
		// return null;
		// }
		//
		// // 1: Record
		// if (path.length <= 1)
		// return doclog;
		// String doclogRecordRawContent;
		// try {
		// doclogRecordRawContent = URLDecoder
		// .decode(path[1], "UTF-8");
		// } catch (UnsupportedEncodingException e) {
		// LOGGER.error("Could no decode name of "
		// + Doclog.class.getSimpleName());
		// return null;
		// }
		// for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
		// if (doclogRecord.getRawContent().equals(
		// doclogRecordRawContent)) {
		// return doclogRecord;
		// }
		// }
		// return null;
		// }
		// };
	}

	@Override
	public boolean showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		return true;
		// if (codedObjects.size() > 0) {
		// if (!openAndSelectFilesInTimeline(codedObjects,
		// (DoclogTimelineView) WorkbenchUtils
		// .getView(DoclogTimelineView.ID)))
		// return false;
		// }
		// return true;
	}

	public boolean openAndSelectFilesInTimeline(
			final List<ICodeable> codedObjects,
			final TimelineView doclogTimelineView) {
		return true;
		// final Set<Object> keys = CodeableUtils.getKeys(codedObjects);
		//
		// // open
		// Future<Boolean> rt = doclogTimelineView.open(keys,
		// new Callable<Boolean>() {
		// public Boolean call() {
		// // select
		// for (Object key : keys) {
		// DoclogTimeline timeline = doclogTimelineView
		// .getTimeline(key);
		// Set<DoclogRecord> doclogRecords = new HashSet<DoclogRecord>(
		// ArrayUtils.getAdaptableObjects(
		// codedObjects.toArray(),
		// DoclogRecord.class));
		//
		// timeline.center(doclogRecords);
		// timeline.highlight(doclogRecords);
		// }
		// return true; // TODO: check if successfull
		// }
		// });
		// try {
		// return rt.get() != null ? rt.get() : false;
		// } catch (InterruptedException e) {
		// LOGGER.error(e);
		// return false;
		// } catch (ExecutionException e) {
		// LOGGER.error(e);
		// return false;
		// }
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			@Override
			public String getText(Object element) {
				// if (element instanceof Doclog) {
				// Doclog doclog = (Doclog) element;
				// if (doclog.getID() != null)
				// return doclog.getID().toString();
				// else
				// return doclog.getFingerprint().toString();
				// }
				// if (element instanceof DoclogRecord) {
				// DoclogRecord doclogRecord = (DoclogRecord) element;
				// TimeZoneDate date = doclogRecord.getDateRange()
				// .getStartDate();
				// return (date != null) ? date
				// .format(new SUACorePreferenceUtil().getDateFormat())
				// : "";
				// }
				return "";
			}

			@Override
			public Image getImage(Object element) {
				// if (element instanceof DoclogRecord) {
				// DoclogRecord doclogRecord = (DoclogRecord) element;
				// try {
				// return (codeService.getCodes(doclogRecord).size() > 0) ?
				// ImageManager.DOCLOGRECORD_CODED
				// : ImageManager.DOCLOGRECORD;
				// } catch (CodeServiceException e) {
				// return ImageManager.DOCLOGRECORD;
				// }
				// }
				return super.getImage(element);
			}
		};
	}

}
