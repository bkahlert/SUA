package de.fu_berlin.imp.seqan.usability_analyzer.survey;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.InformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocumentField;

public class SurveyLabelProvider extends InformationLabelProvider {
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public String getText(Object element) {
		if (element instanceof CDDocument) {
			CDDocument cdDocument = (CDDocument) element;
			return "CD of " + cdDocument.getIdentifier().toString();
		}
		if (element instanceof CDDocumentField) {
			CDDocumentField cdDocumentField = (CDDocumentField) element;
			return "CD field "
					+ cdDocumentField.getKey()
					+ " of "
					+ cdDocumentField.getCdDocument().getIdentifier()
							.toString();
		}
		return "";
	}

	// private boolean hasCodedChildren(IDiff diff) {
	// for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
	// try {
	// if (this.codeService.getCodes(diffRecord).size() > 0) {
	// return true;
	// }
	// } catch (CodeServiceException e) {
	// }
	// }
	// return false;
	// }

	@Override
	public Image getImage(Object element) {
		if (element instanceof CDDocument) {
			CDDocument cdDocument = (CDDocument) element;
			return null;
		}
		return super.getImage(element);
	}

	@Override
	public boolean hasInformation(Object element) {
		return element instanceof CDDocument
				|| element instanceof CDDocumentField;
	}

	@Override
	public List<IllustratedText> getMetaInformation(Object element) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		// if (element instanceof IDiff) {
		// metaEntries.add(new IllustratedText(ImageManager.DIFF, IDiff.class
		// .getSimpleName()));
		// }
		// if (element instanceof IDiffRecord) {
		// metaEntries.add(new IllustratedText(ImageManager.DIFFRECORD,
		// DiffRecord.class.getSimpleName()));
		// }
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(Object element) {
		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		// if (element instanceof IDiffRecord) {
		// IDiffRecord diffRecord = (IDiffRecord) element;
		// detailEntries.add(new DetailEntry("Filename", diffRecord
		// .getFilename() != null ? diffRecord.getFilename() : "-"));
		// detailEntries.add(new DetailEntry("Is Temporary", diffRecord
		// .isTemporary() ? "Yes" : "No"));
		// detailEntries.add(new DetailEntry("Source Exists", diffRecord
		// .sourceExists() ? "Yes" : "No"));
		//
		// detailEntries.add(new DetailEntry("Date", (diffRecord
		// .getDateRange() != null && diffRecord.getDateRange()
		// .getStartDate() != null) ? diffRecord.getDateRange()
		// .getStartDate().toISO8601() : "-"));
		//
		// Long milliSecondsPassed = diffRecord.getDateRange() != null ?
		// diffRecord
		// .getDateRange().getDifference() : null;
		// detailEntries.add(new DetailEntry("Time Passed",
		// (milliSecondsPassed != null) ? DurationFormatUtils
		// .formatDuration(milliSecondsPassed,
		// new SUACorePreferenceUtil()
		// .getTimeDifferenceFormat(), true)
		// : "unknown"));
		// }
		return detailEntries;
	}

	@Override
	public Control fillInformation(Object element, Composite composite) {
		return super.fillInformation(element, composite);
	}

}
