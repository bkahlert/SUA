package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.WrappingData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.gt.DiffCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffRecordUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;

/**
 * Instances of this class denote diffs or more precisely are a container for
 * {@link DiffRecord}s. Classically the underlying resource is a file with the
 * <code>.diff</code> extension in unified diff format.
 */
public class Diff extends WrappingData implements IDiff {

	private static final Logger LOGGER = Logger.getLogger(Diff.class);

	private static final long serialVersionUID = 5159431028889474742L;
	public static final Pattern PATTERN = Pattern
			.compile("([A-Za-z\\d]+)_r([\\d]{8})_([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})(([\\+-][\\d]{2})([\\d]{2}))?(_manual)?\\.diff");

	@Override
	public URI getUri() {
		try {
			return new URI("sua://" + DiffCodeableProvider.DIFF_NAMESPACE + "/"
					+ this.getIdentifier().toString() + "/"
					+ this.getRevision());
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for a " + Diff.class.getSimpleName(),
					e);
		}
		return null;
	}

	private IDiff prevDiffFile;

	private IIdentifier identifier;
	private long revision;
	private TimeZoneDateRange dateRange;

	private DiffRecords diffFileRecords = null;

	public Diff(IData data, IDiff prevDiffFile, ITrunk trunk,
			ISourceStore sourceCache, IProgressMonitor progressMonitor) {
		super(data);
		this.prevDiffFile = prevDiffFile;

		Assert.isNotNull(sourceCache);

		this.identifier = DiffDataUtils.getId(data);
		this.revision = DiffDataUtils.getRevision(data);
		TimeZoneDate prevDate = prevDiffFile != null
				&& prevDiffFile.getDateRange() != null ? prevDiffFile
				.getDateRange().getEndDate() : null;
		this.diffFileRecords = DiffRecordUtils.readRecords(this, trunk,
				sourceCache, progressMonitor);

		TimeZone defaultTimeZone = null;
		for (IDiffRecord diffRecord : this.diffFileRecords) {
			TimeZoneDateRange range = diffRecord.getDateRange();
			if (range != null) {
				if (range.getStartDate() != null) {
					defaultTimeZone = range.getStartDate().getTimeZone();
					break;
				} else if (range.getEndDate() != null) {
					defaultTimeZone = range.getEndDate().getTimeZone();
					break;
				}
			}
		}
		if (defaultTimeZone == null && prevDate != null) {
			defaultTimeZone = prevDate.getTimeZone();
		}

		this.dateRange = new TimeZoneDateRange(prevDate, DiffDataUtils.getDate(
				data, defaultTimeZone));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff#
	 * getPrevDiffFile()
	 */
	@Override
	public IDiff getPrevDiffFile() {
		return this.prevDiffFile;
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff#getRevision ()
	 */
	@Override
	public long getRevision() {
		return this.revision;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.dateRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff#
	 * getDiffFileRecords()
	 */
	@Override
	public DiffRecords getDiffFileRecords() {
		return this.diffFileRecords;
	}

	public int compareTo(Diff diff) {
		if (this.getDateRange() == null
				|| this.getDateRange().getStartDate() == null) {
			if (diff.getDateRange() == null
					|| diff.getDateRange().getStartDate() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (diff.getDateRange() == null
				|| diff.getDateRange().getStartDate() == null) {
			return 1;
		} else {
			return this.getDateRange().getStartDate()
					.compareTo(diff.getDateRange().getStartDate());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff#sourcesExist
	 * ()
	 */
	@Override
	public boolean sourcesExist() {
		IDiffRecords diffFileRecords = this.getDiffFileRecords();
		if (diffFileRecords != null) {
			for (IDiffRecord diffRecord : diffFileRecords) {
				if (!diffRecord.sourceExists()) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff#getContent
	 * (long, long)
	 */
	@Override
	public List<String> getContent(long contentStart, long contentEnd) {
		return Arrays.asList(this.read(contentStart, contentEnd).split("\n"));
	}

	@Override
	public void open() {
		DiffFileEditorUtils.closeCompareEditors();
		for (IDiffRecord diffRecord : this.diffFileRecords) {
			diffRecord.open();
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getRevision()
				+ "@" + this.getIdentifier().toString();
	}

}
