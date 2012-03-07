package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt.DoclogCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DoclogFile extends File implements HasDateRange, ICodeable {

	Logger logger = Logger.getLogger(DoclogFile.class);

	private static final long serialVersionUID = 5159431028889474752L;
	public static final Pattern ID_PATTERN = Pattern
			.compile("^([A-Za-z\\d]+)_doclog\\.txt$");
	public static final Pattern FINGERPRINT_PATTERN = Pattern
			.compile("^fingerprint_([A-Za-z\\d]+)_doclog\\.txt$");

	public static ID getId(File file) {
		ID id = null;
		Matcher matcher = ID_PATTERN.matcher(file.getName());
		if (matcher.find())
			id = new ID(matcher.group(1));
		return id;
	}

	public static Fingerprint getFingerprint(File file) {
		Fingerprint fingerprint = null;
		Matcher matcher = FINGERPRINT_PATTERN.matcher(file.getName());
		if (matcher.find())
			fingerprint = new Fingerprint(matcher.group(1));
		return fingerprint;
	}

	public static TimeZoneDateRange getDateRange(File file) {
		String firstLine = FileUtils.readFirstLine(file);
		String lastLine = FileUtils.readLastLines(file, 1);
		return new TimeZoneDateRange(DoclogRecord.getDate(firstLine),
				DoclogRecord.getDate(lastLine));
	}

	public static Token getToken(File file) {
		Pattern surveyEntryPattern = Pattern
				.compile("\tsurvey-([A-Za-z0-9]+)\t"); // action type
		Pattern surveyQueryPattern = Pattern
				.compile("[\\?|&]token=([A-Za-z0-9]+)"); // token in url
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			while ((strLine = br.readLine()) != null) {
				Matcher surveyEntryMatcher = surveyEntryPattern
						.matcher(strLine);
				if (surveyEntryMatcher.find())
					return new Token(surveyEntryMatcher.group(1));

				Matcher surveyQueryMatcher = surveyQueryPattern
						.matcher(strLine);
				if (surveyQueryMatcher.find())
					return new Token(surveyQueryMatcher.group(1));
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return null;
	}

	private ID id;
	private Fingerprint fingerprint;
	private TimeZoneDateRange dateRange;
	private Token token;

	private DoclogRecordList doclogRecords;

	/**
	 * Creates a new {@link DoclogFile} instance. The instance is {@link ID}
	 * based XOR {@link Fingerprint} based depending on the file's name.
	 * 
	 * @param file
	 */
	public DoclogFile(File file, Object key, TimeZoneDateRange dateRange,
			Token token) {
		super(file.getAbsolutePath());

		if (key instanceof ID) {
			this.id = (ID) key;
			this.fingerprint = null;
		} else if (key instanceof Fingerprint) {
			this.id = null;
			this.fingerprint = (Fingerprint) key;
		} else {
			logger.fatal(key + " was not of type " + ID.class.getSimpleName()
					+ " or " + Fingerprint.class.getSimpleName());
		}
		this.dateRange = dateRange;
		this.token = token;

		scanRecords(file);
		calculateRecordMillisecondsPassed();
	}

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI("sua://"
					+ DoclogCodeableProvider.DOCLOG_NAMESPACE
					+ "/"
					+ ((getId() != null) ? getId().toString()
							: getFingerprint().toString()));
		} catch (Exception e) {
			logger.error(
					"Could not create ID for a "
							+ DoclogFile.class.getSimpleName(), e);
		}
		return null;
	}

	public void scanRecords(File doclogFile) {
		this.doclogRecords = new DoclogRecordList();
		try {
			FileInputStream fstream = new FileInputStream(doclogFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			while ((strLine = br.readLine()) != null) {
				try {
					this.doclogRecords.add(new DoclogRecord(this, strLine));
				} catch (DataSourceInvalidException e) {
					// just ignore the record
				}
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			logger.error("Could not open doclog file", e);
		}
	}

	private void calculateRecordMillisecondsPassed() {
		Collections.sort(this.doclogRecords);
		for (DoclogRecord doclogRecord : this.doclogRecords) {
			DoclogRecord successor = this.doclogRecords
					.getSuccessor(doclogRecord);
			if (successor != null) {
				Long millisecondsPassed = successor.getDate().getTime()
						- doclogRecord.getDate().getTime();
				if (millisecondsPassed < 0) {
					logger.error(DoclogRecord.class.getSimpleName()
							+ "'s successor occured in the past");
				} else if (millisecondsPassed > 0) {
					millisecondsPassed--; // the previous action ends one
											// minimal
											// moment before the next action
											// starts
				}
				doclogRecord.setMillisecondsPassed(millisecondsPassed);
			}
		}
	}

	public ID getId() {
		return this.id;
	}

	public Fingerprint getFingerprint() {
		return this.fingerprint;
	}

	/**
	 * Returns the {@link Token} used for the online survey.
	 * 
	 * @return
	 */
	public Token getToken() {
		return this.token;
	}

	public DoclogRecordList getDoclogRecords() {
		return this.doclogRecords;
	}

	/**
	 * Checks all {@link DoclogScreenshot} {@link Status} and returns the worst
	 * one.
	 * <p>
	 * Example: There are proper screenshot for all but one {@link DoclogRecord}
	 * the returned status is {@link Status#MISSING}.
	 * 
	 * @return
	 */
	public Status getScreenshotStatus() {
		Status worstStatus = Status.OK;

		for (DoclogRecord doclogRecord : doclogRecords) {
			Status currStatus = doclogRecord.getScreenshot().getStatus();
			if (Integer.valueOf(worstStatus.ordinal()).compareTo(
					currStatus.ordinal()) < 0) {
				worstStatus = currStatus;
			}
		}

		return worstStatus;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.dateRange;
	}
}