package de.fu_berlin.imp.seqan.usability_analyzer.diff.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
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

public class DiffLabelProvider extends LabelProvider {
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);

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
			IDiff diff = (IDiff) element;
			return "Iteration #" + (diff.getRevision());
		} else if (element instanceof IDiffRecord
				|| element instanceof IDiffRecordSegment) {
			IDiffRecord diffRecord = element instanceof DiffRecord ? (DiffRecord) element
					: ((DiffRecordSegment) element).getDiffFileRecord();
			String prefix = Activator.getDefault().getDiffDataContainer()
					.getDiffFiles(diffRecord.getID(), null)
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
				if (codeService.getCodes(diffRecord).size() > 0) {
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
				Boolean compiles = compilationService.compiles(diff);
				if (compiles == null) {
					if (codeService.getCodes(diff).size() > 0) {
						return codeService.isMemo(diff) ? ImageManager.DIFF_CODED_MEMO
								: ImageManager.DIFF_CODED;
					} else {
						if (hasCodedChildren(diff)) {
							return codeService.isMemo(diff) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO
									: ImageManager.DIFF_PARTIALLY_CODED;
						} else {
							return (codeService.isMemo(diff) ? ImageManager.DIFF_MEMO
									: ImageManager.DIFF);
						}
					}
				} else if (compiles == true) {
					if (codeService.getCodes(diff).size() > 0) {
						return codeService.isMemo(diff) ? ImageManager.DIFF_CODED_MEMO_WORKING
								: ImageManager.DIFF_CODED_WORKING;
					} else {
						if (hasCodedChildren(diff)) {
							return codeService.isMemo(diff) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO_WORKING
									: ImageManager.DIFF_PARTIALLY_CODED_WORKING;
						} else {
							return (codeService.isMemo(diff) ? ImageManager.DIFF_MEMO_WORKING
									: ImageManager.DIFF_WORKING);
						}
					}
				} else {
					if (codeService.getCodes(diff).size() > 0) {
						return codeService.isMemo(diff) ? ImageManager.DIFF_CODED_MEMO_NOTWORKING
								: ImageManager.DIFF_CODED_NOTWORKING;
					} else {
						if (hasCodedChildren(diff)) {
							return codeService.isMemo(diff) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO_NOTWORKING
									: ImageManager.DIFF_PARTIALLY_CODED_NOTWORKING;
						} else {
							return (codeService.isMemo(diff) ? ImageManager.DIFF_MEMO_NOTWORKING
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
				Boolean compiles = compilationService.compiles(diffRecord);
				if (compiles == null) {
					if (codeService.getCodes(diffRecord).size() > 0) {
						return codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_CODED_MEMO
								: ImageManager.DIFFRECORD_CODED;
					} else {
						if (hasCodedChildren(diffRecord)) {
							return codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO
									: ImageManager.DIFFRECORD_PARTIALLY_CODED;
						} else {
							return (codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_MEMO
									: ImageManager.DIFFRECORD);
						}
					}
				} else if (compiles == true) {
					if (codeService.getCodes(diffRecord).size() > 0) {
						return codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_CODED_MEMO_WORKING
								: ImageManager.DIFFRECORD_CODED_WORKING;
					} else {
						if (hasCodedChildren(diffRecord)) {
							return codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO_WORKING
									: ImageManager.DIFFRECORD_PARTIALLY_CODED_WORKING;
						} else {
							return (codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_MEMO_WORKING
									: ImageManager.DIFFRECORD_WORKING);
						}
					}
				} else {
					if (codeService.getCodes(diffRecord).size() > 0) {
						return codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_CODED_MEMO_NOTWORKING
								: ImageManager.DIFFRECORD_CODED_NOTWORKING;
					} else {
						if (hasCodedChildren(diffRecord)) {
							return codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO_NOTWORKING
									: ImageManager.DIFFRECORD_PARTIALLY_CODED_NOTWORKING;
						} else {
							return (codeService.isMemo(diffRecord) ? ImageManager.DIFFRECORD_MEMO_NOTWORKING
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
				Boolean compiles = compilationService
						.compiles(diffRecordSegment);
				if (compiles == null) {
					if (codeService.getCodes(diffRecordSegment).size() > 0) {
						return codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO
								: ImageManager.DIFFRECORDSEGMENT_CODED;
					} else {
						return codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_MEMO
								: ImageManager.DIFFRECORDSEGMENT;
					}
				} else if (compiles == true) {
					if (codeService.getCodes(diffRecordSegment).size() > 0) {
						return codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO_WORKING
								: ImageManager.DIFFRECORDSEGMENT_CODED_WORKING;
					} else {
						return codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_MEMO_WORKING
								: ImageManager.DIFFRECORDSEGMENT_WORKING;
					}
				} else {
					if (codeService.getCodes(diffRecordSegment).size() > 0) {
						return codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO_NOTWORKING
								: ImageManager.DIFFRECORDSEGMENT_CODED_NOTWORKING;
					} else {
						return codeService.isMemo(diffRecordSegment) ? ImageManager.DIFFRECORDSEGMENT_MEMO_NOTWORKING
								: ImageManager.DIFFRECORDSEGMENT_NOTWORKING;
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFFRECORDSEGMENT;
			}
		}
		return super.getImage(element);
	}
}
