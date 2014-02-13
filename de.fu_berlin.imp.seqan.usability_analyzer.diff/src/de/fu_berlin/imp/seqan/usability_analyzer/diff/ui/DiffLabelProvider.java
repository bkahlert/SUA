package de.fu_berlin.imp.seqan.usability_analyzer.diff.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.StyledUriLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DiffLabelProvider extends StyledUriLabelProvider {
	private static final Logger LOGGER = Logger
			.getLogger(DiffLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		if (locatable == null) {
			return new StyledString(uri.toString());
		}

		if (locatable instanceof IDiffs) {
			IDiffs diffList = (IDiffs) locatable;
			IIdentifier identifier = null;
			if (diffList.length() > 0) {
				identifier = diffList.get(0).getIdentifier();
			}
			return new StyledString(
					(identifier != null) ? identifier.toString() : "");
		}
		if (locatable instanceof IDiff) {
			IDiff diff = (IDiff) locatable;
			TimeZoneDateRange range = diff.getDateRange();
			String duration = range != null ? range.formatDuration() : "?";
			return new StyledString("Iteration #"
					+ diff.getCalculatedRevision() + " - " + duration);
		} else if (locatable instanceof IDiffRecord
				|| locatable instanceof IDiffRecordSegment) {
			IDiffRecord diffRecord = locatable instanceof IDiffRecord ? (IDiffRecord) locatable
					: ((IDiffRecordSegment) locatable).getDiffFileRecord();
			String prefix = Activator.getDefault().getDiffDataContainer()
					.getDiffFiles(diffRecord.getIdentifier(), null)
					.getLongestCommonPrefix();
			String filename = diffRecord.getFilename();
			String shortenedFilename = filename.startsWith(prefix) ? filename
					.substring(prefix.length()) : filename;

			if (shortenedFilename.isEmpty()) {
				shortenedFilename = FilenameUtils.getName(filename);
			}

			if (locatable instanceof IDiffRecordSegment) {
				shortenedFilename += ": "
						+ ((IDiffRecordSegment) locatable).getSegmentStart()
						+ "+"
						+ ((IDiffRecordSegment) locatable).getSegmentLength();
			}
			return new StyledString(shortenedFilename);
		}
		return super.getStyledText(locatable);
	}

	private boolean hasCodedChildren(IDiff diff) {
		for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
			try {
				if (this.codeService.getCodes(diffRecord.getUri()).size() > 0) {
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
	public Image getImage(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		if (locatable == null) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		}

		if (locatable instanceof IDiffs) {
			return ImageManager.DIFFS;
		}
		if (locatable instanceof IDiff) {
			IDiff diff = (IDiff) locatable;
			try {
				Boolean compiles = this.compilationService.compiles(diff);
				if (compiles == null) {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFF_CODED_MEMO
								: ImageManager.DIFF_CODED;
					} else {
						if (this.hasCodedChildren(diff)) {
							return this.codeService.isMemo(uri) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO
									: ImageManager.DIFF_PARTIALLY_CODED;
						} else {
							return (this.codeService.isMemo(uri) ? ImageManager.DIFF_MEMO
									: ImageManager.DIFF);
						}
					}
				} else if (compiles == true) {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFF_CODED_MEMO_WORKING
								: ImageManager.DIFF_CODED_WORKING;
					} else {
						if (this.hasCodedChildren(diff)) {
							return this.codeService.isMemo(uri) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO_WORKING
									: ImageManager.DIFF_PARTIALLY_CODED_WORKING;
						} else {
							return (this.codeService.isMemo(uri) ? ImageManager.DIFF_MEMO_WORKING
									: ImageManager.DIFF_WORKING);
						}
					}
				} else {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFF_CODED_MEMO_NOTWORKING
								: ImageManager.DIFF_CODED_NOTWORKING;
					} else {
						if (this.hasCodedChildren(diff)) {
							return this.codeService.isMemo(uri) ? ImageManager.DIFF_PARTIALLY_CODED_MEMO_NOTWORKING
									: ImageManager.DIFF_PARTIALLY_CODED_NOTWORKING;
						} else {
							return (this.codeService.isMemo(uri) ? ImageManager.DIFF_MEMO_NOTWORKING
									: ImageManager.DIFF_NOTWORKING);
						}
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFF;
			}
		}
		if (locatable instanceof IDiffRecord) {
			IDiffRecord diffRecord = (IDiffRecord) locatable;
			try {
				Boolean compiles = this.compilationService.compiles(diffRecord);
				if (compiles == null) {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_CODED_MEMO
								: ImageManager.DIFFRECORD_CODED;
					} else {
						if (this.hasCodedChildren(diffRecord)) {
							return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO
									: ImageManager.DIFFRECORD_PARTIALLY_CODED;
						} else {
							return (this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_MEMO
									: ImageManager.DIFFRECORD);
						}
					}
				} else if (compiles == true) {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_CODED_MEMO_WORKING
								: ImageManager.DIFFRECORD_CODED_WORKING;
					} else {
						if (this.hasCodedChildren(diffRecord)) {
							return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO_WORKING
									: ImageManager.DIFFRECORD_PARTIALLY_CODED_WORKING;
						} else {
							return (this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_MEMO_WORKING
									: ImageManager.DIFFRECORD_WORKING);
						}
					}
				} else {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_CODED_MEMO_NOTWORKING
								: ImageManager.DIFFRECORD_CODED_NOTWORKING;
					} else {
						if (this.hasCodedChildren(diffRecord)) {
							return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_PARTIALLY_CODED_MEMO_NOTWORKING
									: ImageManager.DIFFRECORD_PARTIALLY_CODED_NOTWORKING;
						} else {
							return (this.codeService.isMemo(uri) ? ImageManager.DIFFRECORD_MEMO_NOTWORKING
									: ImageManager.DIFFRECORD_NOTWORKING);
						}
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFFRECORD;
			}
		}
		if (locatable instanceof IDiffRecordSegment) {
			IDiffRecordSegment diffRecordSegment = (IDiffRecordSegment) locatable;
			try {
				Boolean compiles = this.compilationService
						.compiles(diffRecordSegment);
				if (compiles == null) {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO
								: ImageManager.DIFFRECORDSEGMENT_CODED;
					} else {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORDSEGMENT_MEMO
								: ImageManager.DIFFRECORDSEGMENT;
					}
				} else if (compiles == true) {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO_WORKING
								: ImageManager.DIFFRECORDSEGMENT_CODED_WORKING;
					} else {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORDSEGMENT_MEMO_WORKING
								: ImageManager.DIFFRECORDSEGMENT_WORKING;
					}
				} else {
					if (this.codeService.getCodes(uri).size() > 0) {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORDSEGMENT_CODED_MEMO_NOTWORKING
								: ImageManager.DIFFRECORDSEGMENT_CODED_NOTWORKING;
					} else {
						return this.codeService.isMemo(uri) ? ImageManager.DIFFRECORDSEGMENT_MEMO_NOTWORKING
								: ImageManager.DIFFRECORDSEGMENT_NOTWORKING;
					}
				}
			} catch (CodeServiceException e) {
				return ImageManager.DIFFRECORDSEGMENT;
			}
		}
		return super.getImage(locatable);
	}

	@Override
	public boolean hasInformation(URI uri) {
		ILocatable locatable;
		try {
			locatable = this.locatorService.resolve(uri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error checking information for " + uri);
			return false;
		}

		return locatable instanceof IDiff || locatable instanceof IDiffRecord;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof IDiff) {
			metaEntries.add(new IllustratedText(ImageManager.DIFF, IDiff.class
					.getSimpleName()));
		}
		if (locatable instanceof IDiffRecord) {
			metaEntries.add(new IllustratedText(ImageManager.DIFFRECORD,
					DiffRecord.class.getSimpleName()));
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof IDiff) {
			IDiff diff = (IDiff) locatable;
			detailEntries.add(new DetailEntry("Name",
					diff.getName() != null ? diff.getName() : "-"));
			detailEntries.add(new DetailEntry("Revision", diff.getRevision()
					+ ""));
			detailEntries.add(new DetailEntry("Calculated Revision", diff
					.getCalculatedRevision() + ""));
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
							: "?"));
		}
		if (locatable instanceof IDiffRecord) {
			IDiffRecord diffRecord = (IDiffRecord) locatable;
			detailEntries.add(new DetailEntry("Filename", diffRecord
					.getFilename() != null ? diffRecord.getFilename() : "-"));
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
							: "?"));
		}
		return detailEntries;
	}

	@Override
	public Control fillInformation(URI uri, Composite composite)
			throws Exception {
		return super.fillInformation(uri, composite);
	}

}
