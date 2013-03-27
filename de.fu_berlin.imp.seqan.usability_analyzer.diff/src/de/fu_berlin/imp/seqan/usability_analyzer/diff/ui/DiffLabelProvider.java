package de.fu_berlin.imp.seqan.usability_analyzer.diff.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.InformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DiffLabelProvider extends InformationLabelProvider {
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);

	@Override
	public String getText(Object element) {
		if (element instanceof IDiffs) {
			IDiffs diffList = (IDiffs) element;
			IIdentifier identifier = null;
			if (diffList.length() > 0) {
				identifier = diffList.get(0).getIdentifier();
			}
			return (identifier != null) ? identifier.toString() : "";
		}
		if (element instanceof IDiff) {
			IDiff diff = (IDiff) element;
			Long milliSecondsPassed = diff.getDateRange().getDifference();
			String duration = (milliSecondsPassed != null) ? DurationFormatUtils
					.formatDuration(milliSecondsPassed,
							new SUACorePreferenceUtil()
									.getTimeDifferenceFormat(), true)
					: "unknown";
			return "Iteration #" + diff.getRevision() + " - " + duration;
		} else if (element instanceof IDiffRecord
				|| element instanceof IDiffRecordSegment) {
			IDiffRecord diffRecord = element instanceof DiffRecord ? (DiffRecord) element
					: ((DiffRecordSegment) element).getDiffFileRecord();
			String prefix = Activator.getDefault().getDiffDataContainer()
					.getDiffFiles(diffRecord.getIdentifier(), null)
					.getLongestCommonPrefix();
			String filename = diffRecord.getFilename();
			String shortenedFilename = filename.startsWith(prefix) ? filename
					.substring(prefix.length()) : filename;

			String revisionShortenedFilename = shortenedFilename + "@"
					+ diffRecord.getDiffFile().getRevision();
			return element instanceof DiffRecord ? revisionShortenedFilename
					: revisionShortenedFilename + ": "
							+ ((DiffRecordSegment) element).getSegmentStart()
							+ "+"
							+ ((DiffRecordSegment) element).getSegmentLength();
		}
		return "";
	}

	private boolean hasCodedChildren(IDiff diff) {
		for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
			try {
				if (this.codeService.getCodes(diffRecord).size() > 0) {
					return true;
				}
			} catch (CodeServiceException e) {
			}
		}
		return false;
	}

	// TODO: partially coded icon wenn diffrecordsegment
	// existiert
	private boolean hasCodedChildren(IDiffRecord diffRecord) {
		return false;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IDiffs) {
			return ImageManager.DIFFS;
		}
		if (element instanceof IDiff) {
			IDiff diff = (IDiff) element;
			try {
				Boolean compiles = this.compilationService.compiles(diff);
				if (compiles == null) {
					if (this.codeService.getCodes(diff).size() > 0) {
						return this.codeService.isMemo(diff) ? ImageManager.DIFF_CODED_MEMO
								: ImageManager.DIFF_CODED;
					} else {
						if (this.hasCodedChildren(diff)) {
							return this.codeService.isMemo(diff) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO
									: ImageManager.DIFF_PARTIALLY_CODED;
						} else {
							return (this.codeService.isMemo(diff) ? ImageManager.DIFF_MEMO
									: ImageManager.DIFF);
						}
					}
				} else if (compiles == true) {
					if (this.codeService.getCodes(diff).size() > 0) {
						return this.codeService.isMemo(diff) ? ImageManager.DIFF_CODED_MEMO_WORKING
								: ImageManager.DIFF_CODED_WORKING;
					} else {
						if (this.hasCodedChildren(diff)) {
							return this.codeService.isMemo(diff) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO_WORKING
									: ImageManager.DIFF_PARTIALLY_CODED_WORKING;
						} else {
							return (this.codeService.isMemo(diff) ? ImageManager.DIFF_MEMO_WORKING
									: ImageManager.DIFF_WORKING);
						}
					}
				} else {
					if (this.codeService.getCodes(diff).size() > 0) {
						return this.codeService.isMemo(diff) ? ImageManager.DIFF_CODED_MEMO_NOTWORKING
								: ImageManager.DIFF_CODED_NOTWORKING;
					} else {
						if (this.hasCodedChildren(diff)) {
							return this.codeService.isMemo(diff) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO_NOTWORKING
									: ImageManager.DIFF_PARTIALLY_CODED_NOTWORKING;
						} else {
							return (this.codeService.isMemo(diff) ? ImageManager.DIFF_MEMO_NOTWORKING
									: ImageManager.DIFF_NOTWORKING);
						}
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFF;
			}
		}
		if (element instanceof IDiffRecord) {
			IDiffRecord diffRecord = (IDiffRecord) element;
			try {
				Boolean compiles = this.compilationService.compiles(diffRecord);
				if (compiles == null) {
					if (this.codeService.getCodes(diffRecord).size() > 0) {
						return this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_CODED_MEMO
								: ImageManager.DIFFRECORD_CODED;
					} else {
						if (this.hasCodedChildren(diffRecord)) {
							return this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO
									: ImageManager.DIFFRECORD_PARTIALLY_CODED;
						} else {
							return (this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_MEMO
									: ImageManager.DIFFRECORD);
						}
					}
				} else if (compiles == true) {
					if (this.codeService.getCodes(diffRecord).size() > 0) {
						return this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_CODED_MEMO_WORKING
								: ImageManager.DIFFRECORD_CODED_WORKING;
					} else {
						if (this.hasCodedChildren(diffRecord)) {
							return this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO_WORKING
									: ImageManager.DIFFRECORD_PARTIALLY_CODED_WORKING;
						} else {
							return (this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_MEMO_WORKING
									: ImageManager.DIFFRECORD_WORKING);
						}
					}
				} else {
					if (this.codeService.getCodes(diffRecord).size() > 0) {
						return this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_CODED_MEMO_NOTWORKING
								: ImageManager.DIFFRECORD_CODED_NOTWORKING;
					} else {
						if (this.hasCodedChildren(diffRecord)) {
							return this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO_NOTWORKING
									: ImageManager.DIFFRECORD_PARTIALLY_CODED_NOTWORKING;
						} else {
							return (this.codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_MEMO_NOTWORKING
									: ImageManager.DIFFRECORD_NOTWORKING);
						}
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFFRECORD;
			}
		}
		if (element instanceof IDiffRecordSegment) {
			IDiffRecordSegment diffRecordSegment = (IDiffRecordSegment) element;
			try {
				Boolean compiles = this.compilationService
						.compiles(diffRecordSegment);
				if (compiles == null) {
					if (this.codeService.getCodes(diffRecordSegment).size() > 0) {
						return this.codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO
								: ImageManager.DIFFRECORDSEGMENT_CODED;
					} else {
						return this.codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_MEMO
								: ImageManager.DIFFRECORDSEGMENT;
					}
				} else if (compiles == true) {
					if (this.codeService.getCodes(diffRecordSegment).size() > 0) {
						return this.codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO_WORKING
								: ImageManager.DIFFRECORDSEGMENT_CODED_WORKING;
					} else {
						return this.codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_MEMO_WORKING
								: ImageManager.DIFFRECORDSEGMENT_WORKING;
					}
				} else {
					if (this.codeService.getCodes(diffRecordSegment).size() > 0) {
						return this.codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO_NOTWORKING
								: ImageManager.DIFFRECORDSEGMENT_CODED_NOTWORKING;
					} else {
						return this.codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_MEMO_NOTWORKING
								: ImageManager.DIFFRECORDSEGMENT_NOTWORKING;
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFFRECORDSEGMENT;
			}
		}
		return super.getImage(element);
	}

	@Override
	public boolean hasInformation(Object element) {
		return element instanceof IDiff || element instanceof IDiffRecord;
	}

	@Override
	public List<IllustratedText> getMetaInformation(Object element) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (element instanceof IDiff) {
			metaEntries.add(new IllustratedText(ImageManager.DIFF, IDiff.class
					.getSimpleName()));
		}
		if (element instanceof IDiffRecord) {
			metaEntries.add(new IllustratedText(ImageManager.DIFFRECORD,
					DiffRecord.class.getSimpleName()));
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(Object element) {
		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (element instanceof IDiff) {
			IDiff diff = (IDiff) element;
			detailEntries.add(new DetailEntry("Name",
					diff.getName() != null ? diff.getName() : "-"));
			detailEntries.add(new DetailEntry("Revision", diff.getRevision()
					+ ""));
			detailEntries.add(new DetailEntry("File Size", diff.getLength()
					+ " Bytes"));

			detailEntries.add(new DetailEntry("Date",
					(diff.getDateRange() != null && diff.getDateRange()
							.getStartDate() != null) ? diff.getDateRange()
							.getStartDate().toISO8601() : "-"));

			Long milliSecondsPassed = diff.getDateRange() != null ? diff
					.getDateRange().getDifference() : null;
			detailEntries.add(new DetailEntry("Time Passed",
					(milliSecondsPassed != null) ? DurationFormatUtils
							.formatDuration(milliSecondsPassed,
									new SUACorePreferenceUtil()
											.getTimeDifferenceFormat(), true)
							: "unknown"));
		}
		if (element instanceof IDiffRecord) {
			IDiffRecord diffRecord = (IDiffRecord) element;
			detailEntries.add(new DetailEntry("Filename", diffRecord
					.getFilename() != null ? diffRecord.getFilename() : "-"));
			detailEntries.add(new DetailEntry("Source",
					diffRecord.getSource() != null ? diffRecord.getSource()
							: "-"));
			detailEntries.add(new DetailEntry("Is Temporary", diffRecord
					.isTemporary() ? "Yes" : "No"));
			detailEntries.add(new DetailEntry("Source Exists", diffRecord
					.sourceExists() ? "Yes" : "No"));

			detailEntries.add(new DetailEntry("Date", (diffRecord
					.getDateRange() != null && diffRecord.getDateRange()
					.getStartDate() != null) ? diffRecord.getDateRange()
					.getStartDate().toISO8601() : "-"));

			Long milliSecondsPassed = diffRecord.getDateRange() != null ? diffRecord
					.getDateRange().getDifference() : null;
			detailEntries.add(new DetailEntry("Time Passed",
					(milliSecondsPassed != null) ? DurationFormatUtils
							.formatDuration(milliSecondsPassed,
									new SUACorePreferenceUtil()
											.getTimeDifferenceFormat(), true)
							: "unknown"));
		}
		return detailEntries;
	}

	@Override
	public Control fillInformation(Object element, Composite composite) {
		return super.fillInformation(element, composite);
	}

}
