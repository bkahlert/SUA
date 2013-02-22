package de.fu_berlin.imp.seqan.usability_analyzer.diff.ui;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DiffLabelProvider extends LabelProvider {
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public String getText(Object element) {
		if (element instanceof IDiffs) {
			IDiffs diffList = (IDiffs) element;
			ID id = null;
			if (diffList.length() > 0) {
				id = diffList.get(0).getID();
			}
			return (id != null) ? id.toString() : "";
		}
		if (element instanceof IDiff) {
			Diff diff = (Diff) element;
			TimeZoneDate date = diff.getDateRange().getStartDate();
			return (date != null) ? date.format(new SUACorePreferenceUtil()
					.getDateFormat()) : "";
		}
		if (element instanceof DiffRecord) {
			DiffRecord diffRecord = (DiffRecord) element;
			String name = diffRecord.getFilename();
			return (name != null) ? new File(name).getName() + "@"
					+ diffRecord.getDiffFile().getRevision() : "";
		}
		if (element instanceof DiffRecordSegment) {
			DiffRecordSegment diffRecordSegment = (DiffRecordSegment) element;
			String name = diffRecordSegment.getDiffFileRecord().getFilename();
			return (name != null) ? new File(name).getName() + ": "
					+ diffRecordSegment.getSegmentStart() + "+"
					+ diffRecordSegment.getSegmentLength() : "";
		}
		return "";
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IDiffs) {
			return ImageManager.DIFFFILELIST;
		}
		if (element instanceof IDiff) {
			Diff diffFile = (Diff) element;
			try {
				return (codeService.getCodes(diffFile).size() > 0) ? (codeService
						.isMemo(diffFile) ? ImageManager.DIFFFILE_CODED_MEMO
						: ImageManager.DIFFFILE_CODED) : (codeService
						.isMemo(diffFile) ? ImageManager.DIFFFILE_MEMO
						: ImageManager.DIFFFILE);
			} catch (CodeServiceException e) {
				return ImageManager.DIFFFILE;
			}
		}
		if (element instanceof DiffRecord) {
			DiffRecord diffFileRecord = (DiffRecord) element;
			try {
				return (codeService.getCodes(diffFileRecord).size() > 0) ? (codeService
						.isMemo(diffFileRecord) ? ImageManager.DIFFFILERECORD_CODED_MEMO
						: ImageManager.DIFFFILERECORD_CODED)
						: (codeService.isMemo(diffFileRecord) ? ImageManager.DIFFFILERECORD_MEMO
								: ImageManager.DIFFFILERECORD);
			} catch (CodeServiceException e) {
				return ImageManager.DIFFFILERECORD;
			}
		}
		if (element instanceof DiffRecordSegment) {
			DiffRecordSegment diffFileRecordSegment = (DiffRecordSegment) element;
			try {
				return (codeService.getCodes(diffFileRecordSegment).size() > 0) ? (codeService
						.isMemo(diffFileRecordSegment) ? ImageManager.DIFFFILERECORDSEGMENT_CODED_MEMO
						: ImageManager.DIFFFILERECORDSEGMENT_CODED)
						: (codeService.isMemo(diffFileRecordSegment) ? ImageManager.DIFFFILERECORDSEGMENT_MEMO
								: ImageManager.DIFFFILERECORDSEGMENT);
			} catch (CodeServiceException e) {
				return ImageManager.DIFFFILERECORDSEGMENT;
			}
		}
		return super.getImage(element);
	}
}
