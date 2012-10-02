package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.gt.DiffCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataResourceUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;

/**
 * Instances of this class denote diffs or more precisely are a container for
 * {@link DiffRecord}s. Classically the underlying resource is a file with the
 * <code>.diff</code> extension in unified diff format.
 */
public class DiffData implements IDiffData {

	private static final Logger LOGGER = Logger.getLogger(DiffData.class);

	private static final long serialVersionUID = 5159431028889474742L;
	public static final Pattern PATTERN = Pattern
			.compile("([A-Za-z\\d]+)_r([\\d]{8})_([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})(([\\+-][\\d]{2})([\\d]{2}))?(_manual)?\\.diff");

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI("sua://" + DiffCodeableProvider.DIFF_NAMESPACE + "/"
					+ getID().toString() + "/" + getRevision());
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for a "
							+ DiffData.class.getSimpleName(), e);
		}
		return null;
	}

	private IData data;
	private IDiffData prevDiffFile;

	private ID id;
	private long revision;
	private TimeZoneDateRange dateRange;

	private DiffRecordList diffFileRecords = null;

	public DiffData(IData data, IDiffData prevDiffFile, ITrunk trunk,
			ISourceStore sourceCache, IProgressMonitor progressMonitor) {
		this.data = data;
		this.prevDiffFile = prevDiffFile;

		Assert.isNotNull(sourceCache);

		this.id = DiffDataUtils.getId(data);
		this.revision = DiffDataUtils.getRevision(data);
		TimeZoneDate prevDate = prevDiffFile != null
				&& prevDiffFile.getDateRange() != null ? prevDiffFile
				.getDateRange().getEndDate() : null;
		this.dateRange = new TimeZoneDateRange(prevDate,
				DiffDataUtils.getDate(data));

		this.diffFileRecords = DiffDataResourceUtils.readRecords(this, trunk,
				sourceCache, progressMonitor);
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this.data.getBaseDataContainer();
	}

	@Override
	public IDataContainer getParentDataContainer() {
		return this.data.getParentDataContainer();
	}

	@Override
	public String getName() {
		return this.data.getName();
	}

	@Override
	public String read() {
		return this.data.read();
	}

	@Override
	public String read(long from, long to) {
		return this.data.read(from, to);
	}

	@Override
	public String readFirstLine() {
		return this.data.readFirstLine();
	}

	@Override
	public String readLastLines(int numLines) {
		return this.data.readLastLines(numLines);
	}

	@Override
	public Iterator<String> iterator() {
		return this.data.iterator();
	}

	@Override
	public long getLength() {
		return this.data.getLength();
	}

	@Override
	public File getStaticFile() throws IOException {
		return this.data.getStaticFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffData#
	 * getPrevDiffFile()
	 */
	@Override
	public IDiffData getPrevDiffFile() {
		return prevDiffFile;
	}

	public ID getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffData#getRevision
	 * ()
	 */
	@Override
	public long getRevision() {
		return revision;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.dateRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffData#
	 * getDiffFileRecords()
	 */
	@Override
	public DiffRecordList getDiffFileRecords() {
		return diffFileRecords;
	}

	public int compareTo(DiffData diffData) {
		if (this.getDateRange() == null
				|| this.getDateRange().getStartDate() == null) {
			if (diffData.getDateRange() == null
					|| diffData.getDateRange().getStartDate() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (diffData.getDateRange() == null
				|| diffData.getDateRange().getStartDate() == null) {
			return 1;
		} else {
			return this.getDateRange().getStartDate()
					.compareTo(diffData.getDateRange().getStartDate());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffData#sourcesExist
	 * ()
	 */
	@Override
	public boolean sourcesExist() {
		DiffRecordList diffFileRecords = this.getDiffFileRecords();
		if (diffFileRecords != null) {
			for (DiffRecord diffRecord : diffFileRecords) {
				if (!diffRecord.sourceExists())
					return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffData#getContent
	 * (long, long)
	 */
	@Override
	public List<String> getContent(long contentStart, long contentEnd) {
		return Arrays.asList(read(contentStart, contentEnd).split("\n"));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getRevision()
				+ "@" + this.getID().toString();
	}

}
