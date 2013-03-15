package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.DetailedLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DoclogLabelProvider extends DetailedLabelProvider {

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public String getText(Object element) {
		if (element instanceof Doclog) {
			Doclog doclog = (Doclog) element;
			return doclog.getIdentifier().toString();
		}
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			String url = doclogRecord.getUrl();
			if (url != null) {
				url = url.replaceAll(".*://", "");
			}
			Integer scrollY = doclogRecord.getScrollPosition() != null ? doclogRecord
					.getScrollPosition().y : null;
			return url != null ? (scrollY != null ? url + " ⇅" + scrollY : url)
					: "ERROR";
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Doclog) {
			Doclog doclog = (Doclog) element;
			try {
				return (this.codeService.getCodes(doclog).size() > 0) ? (this.codeService
						.isMemo(doclog) ? ImageManager.DOCLOGFILE_CODED_MEMO
						: ImageManager.DOCLOGFILE_CODED) : (this.codeService
						.isMemo(doclog) ? ImageManager.DOCLOGFILE_MEMO
						: ImageManager.DOCLOGFILE);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGFILE;
			}
		}
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			try {
				return (this.codeService.getCodes(doclogRecord).size() > 0) ? (this.codeService
						.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_CODED_MEMO
						: ImageManager.DOCLOGRECORD_CODED)
						: (this.codeService.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_MEMO
								: ImageManager.DOCLOGRECORD);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGRECORD;
			}
		}
		return super.getImage(element);
	}

	@Override
	public boolean canFillPopup(Object element) {
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			if (doclogRecord.getScreenshot() == null) {
				return false;
			}
			DoclogScreenshot screenshot = doclogRecord.getScreenshot();
			if (screenshot == null) {
				return false;
			}
			ImageData imageData = screenshot.getImageData();
			if (imageData == null) {
				return false;
			}
			return true;
		}
		return super.canFillPopup(element);
	}

	@Override
	public Control fillPopup(Object element, Composite composite) {
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			Image image = new Image(Display.getCurrent(), doclogRecord
					.getScreenshot().getImageData());
			Label label = new Label(composite, SWT.NONE);
			label.setImage(image);
			return label;
		}
		return super.fillPopup(element, composite);
	}

}
