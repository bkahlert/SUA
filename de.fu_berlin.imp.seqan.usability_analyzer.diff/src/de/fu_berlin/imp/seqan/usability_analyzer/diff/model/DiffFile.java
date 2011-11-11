package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DiffFile extends File implements HasDateRange {

	Logger logger = Logger.getLogger(DiffFile.class);

	private static final long serialVersionUID = 5159431028889474742L;
	public static final String PATTERN = "([A-Za-z\\d]+)_r([\\d]{8})_([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})\\.diff";

	private ID id;
	private String revision;
	private Date date;

	private Long millisecondsPassed;

	private DiffFileRecordList diffFileRecords = null;

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

		// this.scanFiles(filename);
	}

	public void scanFiles(String filename) {
		DiffFileRecordList diffFileRecords = new DiffFileRecordList();
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String commandLine = null;
			ArrayList<String> content = null;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (strLine.equals("RESET"))
					return;

				String[] x = strLine.split(" ");
				if (x.length > 0
						&& (x[0].equals("diff") || x[0].equals("Files") || x[0]
								.equals("Binary"))) {
					// create record if new record is found
					if (commandLine != null) {
						diffFileRecords.add(new DiffFileRecord(this,
								commandLine, content));
						commandLine = null;
						content = null;
					}

					if (x[0].equals("diff")) {
						commandLine = strLine;
						content = new ArrayList<String>();
					}
				} else {
					content.add(strLine);
				}
			}

			// create record if eof
			if (commandLine != null) {
				diffFileRecords.add(new DiffFileRecord(this, commandLine,
						content));
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			logger.error("Could not open doclog file", e);
		}
		this.diffFileRecords = diffFileRecords;
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

	public DiffFileRecordList getDiffFileRecords() {
		if (this.diffFileRecords == null)
			this.scanFiles(this.getAbsolutePath());
		return diffFileRecords;
	}

	void setMillisecondsPassed(long millisecondsPassed) {
		this.millisecondsPassed = millisecondsPassed;
	}

	public Long getMillisecondsPassed() {
		return this.millisecondsPassed;
	}

	public DateRange getDateRange() {
		if (this.date == null)
			return null;

		long start = this.date.getTime();
		return new DateRange(start, start
				+ ((this.millisecondsPassed != null) ? this.millisecondsPassed
						: 0));
	}

	public int compareTo(DiffFile diffFile) {
		if (this.getDate() == null) {
			if (diffFile.getDate() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (diffFile.getDate() == null) {
			return 1;
		} else {
			return this.getDate().compareTo(diffFile.getDate());
		}
	}

}
