package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.IRangeable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DiffFile extends File implements IRangeable {

	private static final long serialVersionUID = 5159431028889474742L;
	public static final String PATTERN = "([A-Za-z\\d]+)_r([\\d]{8})_([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})\\.diff";

	private ID id;
	private String revision;
	private Date date;

	private long millisecondsPassed;

	public DiffFile(String filename) {
		super(filename);

		Matcher matcher = Pattern.compile(PATTERN).matcher(filename);
		if (matcher.find()) {
			this.id = new ID(matcher.group(1));
			this.revision = matcher.group(2);
			this.date = DateUtil.getDate(Integer.valueOf(matcher.group(3)),
					Integer.valueOf(matcher.group(4)) - 1,
					Integer.valueOf(matcher.group(5)),
					Integer.valueOf(matcher.group(6)),
					Integer.valueOf(matcher.group(7)),
					Integer.valueOf(matcher.group(8)));
		}
	}

	public ID getId() {
		return id;
	}

	public String getRevision() {
		return revision;
	}

	public Date getDate() {
		return date;
	}

	void setMillisecondsPassed(long millisecondsPassed) {
		this.millisecondsPassed = millisecondsPassed;
	}

	public long getMillisecondsPassed() {
		return this.millisecondsPassed;
	}

	@Override
	public boolean isInRange(DateRange dateRange) {
		if (this.getDate() != null)
			return dateRange.isInRange(this.getDate());
		else
			return false;
	}

}
