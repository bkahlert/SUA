package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffFileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DiffFile extends File implements HasDateRange, ICodeable {

	private static final Logger LOGGER = Logger.getLogger(DiffFile.class);

	private static final long serialVersionUID = 5159431028889474742L;
	public static final Pattern PATTERN = Pattern
			.compile("([A-Za-z\\d]+)_r([\\d]{8})_([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})(([\\+-][\\d]{2})([\\d]{2}))?(_manual)?\\.diff");

	public static ID getId(File file) {
		ID id = null;
		Matcher matcher = PATTERN.matcher(file.getAbsolutePath());
		if (matcher.find())
			id = new ID(matcher.group(1));
		return id;
	}

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI("sua://diff/" + getId().toString() + "/"
					+ Integer.parseInt(getRevision()));
		} catch (Exception e) {
			LOGGER.error(
					"Could not create ID for a "
							+ DiffFile.class.getSimpleName(), e);
		}
		return null;
	}

	public static String getRevision(File file) {
		String revision = null;
		Matcher matcher = PATTERN.matcher(file.getAbsolutePath());
		if (matcher.find())
			revision = matcher.group(2);
		return revision;
	}

	public static TimeZoneDate getDate(File file) {
		TimeZoneDate date = null;
		Matcher matcher = PATTERN.matcher(file.getAbsolutePath());
		if (matcher.find()) {
			if (matcher.group(9) != null) {
				// Date contains time zone
				date = new TimeZoneDate(matcher.group(3) + "-"
						+ matcher.group(4) + "-" + matcher.group(5) + "T"
						+ matcher.group(6) + ":" + matcher.group(7) + ":"
						+ matcher.group(8) + matcher.group(10) + ":"
						+ matcher.group(11));
			} else {
				// Date does not contain a time zone
				Date rawDate = DateUtil.getDate(
						Integer.valueOf(matcher.group(3)),
						Integer.valueOf(matcher.group(4)) - 1,
						Integer.valueOf(matcher.group(5)),
						Integer.valueOf(matcher.group(6)),
						Integer.valueOf(matcher.group(7)),
						Integer.valueOf(matcher.group(8)));

				TimeZone timeZone;
				try {
					timeZone = new SUACorePreferenceUtil().getDefaultTimeZone();
				} catch (Exception e) {
					timeZone = TimeZone.getDefault();
				}
				rawDate.setTime(rawDate.getTime()
						- timeZone.getOffset(rawDate.getTime()));
				date = new TimeZoneDate(rawDate, timeZone);
			}
		}
		return date;
	}

	private DiffFile prevDiffFile;

	private ID id;
	private String revision;
	private TimeZoneDateRange dateRange;

	private DiffFileRecordList diffFileRecords = null;

	public DiffFile(File file, DiffFile prevDiffFile, ID id, String revision,
			TimeZoneDateRange dateRange, SourceOrigin sourceOrigin,
			SourceCache sourceCache, IProgressMonitor progressMonitor) {
		super(file.getAbsolutePath());

		this.prevDiffFile = prevDiffFile;

		Assert.isNotNull(sourceCache);

		this.id = id;
		this.revision = revision;
		this.dateRange = dateRange;

		this.diffFileRecords = DiffFileUtils.readRecords(this, sourceOrigin,
				sourceCache, progressMonitor);
	}

	public DiffFile getPrevDiffFile() {
		return prevDiffFile;
	}

	public ID getId() {
		return id;
	}

	public String getRevision() {
		return revision;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.dateRange;
	}

	public DiffFileRecordList getDiffFileRecords() {
		return diffFileRecords;
	}

	public int compareTo(DiffFile diffFile) {
		if (this.getDateRange() == null
				|| this.getDateRange().getStartDate() == null) {
			if (diffFile.getDateRange() == null
					|| diffFile.getDateRange().getStartDate() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (diffFile.getDateRange() == null
				|| diffFile.getDateRange().getStartDate() == null) {
			return 1;
		} else {
			return this.getDateRange().getStartDate()
					.compareTo(diffFile.getDateRange().getStartDate());
		}
	}

	public boolean sourcesExist() {
		DiffFileRecordList diffFileRecords = this.getDiffFileRecords();
		if (diffFileRecords != null) {
			for (DiffFileRecord diffFileRecord : diffFileRecords) {
				if (!diffFileRecord.sourceExists())
					return false;
			}
		}
		return true;
	}

	public List<String> getContent(long contentStart, long contentEnd) {
		String content = new String(FileUtils.readBytesFromTo(this,
				contentStart, contentEnd));
		return Arrays.asList(content.split("\n"));
	}

}
