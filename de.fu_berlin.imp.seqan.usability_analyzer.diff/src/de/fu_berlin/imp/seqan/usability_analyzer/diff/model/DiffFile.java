package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DiffFile extends File implements HasDateRange {

	Logger logger = Logger.getLogger(DiffFile.class);

	private static final long serialVersionUID = 5159431028889474742L;
	public static final String PATTERN = "([A-Za-z\\d]+)_r([\\d]{8})_([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2})-([\\d]{2})-([\\d]{2})(([\\+-][\\d]{2})([\\d]{2}))?(_manual)?\\.diff";

	private File logDirectory;

	private ID id;
	private String revision;
	private LocalDate date;

	private Long millisecondsPassed;

	private DiffFileRecordList diffFileRecords = null;

	public DiffFile(File logDirectory, String filename) {
		super(filename);

		this.logDirectory = logDirectory;

		Matcher matcher = Pattern.compile(PATTERN).matcher(filename);
		if (matcher.find()) {
			this.id = new ID(matcher.group(1));
			this.revision = matcher.group(2);

			if (matcher.group(9) != null) {
				// Date contains time zone
				this.date = new LocalDate(matcher.group(3) + "-"
						+ matcher.group(4) + "-" + matcher.group(5) + "T"
						+ matcher.group(6) + ":" + matcher.group(7) + ":"
						+ matcher.group(8) + matcher.group(10) + ":"
						+ matcher.group(11));
			} else {
				// Date does not contain a time zone
				Date date = DateUtil.getDate(Integer.valueOf(matcher.group(3)),
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
				this.date = new LocalDate(date, timeZone);
			}
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
						createRecord(diffFileRecords, commandLine, content);
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
				createRecord(diffFileRecords, commandLine, content);
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			logger.error("Could not open doclog file", e);
		}
		this.diffFileRecords = diffFileRecords;
	}

	/**
	 * Creates a {@link DiffFileRecord} in the given {@link DiffFileRecordList}.
	 * 
	 * @param diffFileRecords
	 * @param commandLine
	 * @param content
	 */
	private void createRecord(DiffFileRecordList diffFileRecords,
			String commandLine, ArrayList<String> content) {
		DiffFileRecord diffFileRecord = new DiffFileRecord(this.logDirectory,
				this, commandLine, content);
		if (!diffFileRecord.isTemporary()) {
			diffFileRecords.add(diffFileRecord);
		}
	}

	public ID getId() {
		return id;
	}

	public String getRevision() {
		return revision;
	}

	public LocalDate getDate() {
		return date;
	}

	public DiffFileRecordList getDiffFileRecords() {
		if (this.diffFileRecords == null)
			this.scanFiles(this.getAbsolutePath());
		return diffFileRecords;
	}

	void setMillisecondsPassed(Long millisecondsPassed) {
		this.millisecondsPassed = millisecondsPassed;
	}

	public LocalDateRange getDateRange() {
		if (this.date == null)
			return null;

		LocalDate endDate = this.date.clone();
		if (this.millisecondsPassed != null)
			endDate.addMilliseconds(this.millisecondsPassed);
		return new LocalDateRange(this.date, endDate);
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

}
