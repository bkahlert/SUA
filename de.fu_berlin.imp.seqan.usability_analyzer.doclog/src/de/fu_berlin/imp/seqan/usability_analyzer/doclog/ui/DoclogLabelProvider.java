package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DoclogLabelProvider extends LabelProvider {

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public String getText(Object element) {
		if (element instanceof Doclog) {
			Doclog doclog = (Doclog) element;
			if (doclog.getID() != null)
				return doclog.getID().toString();
			else
				return doclog.getFingerprint().toString();
		}
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			TimeZoneDate date = doclogRecord.getDateRange().getStartDate();
			return (date != null) ? date.format(new SUACorePreferenceUtil()
					.getDateFormat()) : "";
		}
		return "";
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Doclog) {
			Doclog doclog = (Doclog) element;
			try {
				return (codeService.getCodes(doclog).size() > 0) ? (codeService
						.isMemo(doclog) ? ImageManager.DOCLOGFILE_CODED_MEMO
						: ImageManager.DOCLOGFILE_CODED) : (codeService
						.isMemo(doclog) ? ImageManager.DOCLOGFILE_MEMO
						: ImageManager.DOCLOGFILE);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGFILE;
			}
		}
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			try {
				return (codeService.getCodes(doclogRecord).size() > 0) ? (codeService
						.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_CODED_MEMO
						: ImageManager.DOCLOGRECORD_CODED)
						: (codeService.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_MEMO
								: ImageManager.DOCLOGRECORD);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGRECORD;
			}
		}
		return super.getImage(element);
	}

}
