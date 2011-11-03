package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.IRangeable;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;

public class DoclogFile extends File implements IRangeable {

	Logger logger = Logger.getLogger(DoclogFile.class);

	private static final long serialVersionUID = 5159431028889474752L;
	public static final String ID_PATTERN = "^([A-Za-z\\d]+)_doclog\\.txt$";
	public static final String FINGERPRINT_PATTERN = "^fingerprint_([A-Za-z\\d]+)_doclog\\.txt$";

	private ID id;
	private Fingerprint fingerprint;
	private DoclogRecordList doclogRecords;

	/**
	 * Creates a new {@link DoclogFile} instance. The instance is {@link ID}
	 * based XOR {@link Fingerprint} based depending on the file's name.
	 * 
	 * @param doclogFile
	 */
	public DoclogFile(String doclogFile) {
		super(doclogFile);

		String filename = this.getName();

		Matcher idMatcher = Pattern.compile(ID_PATTERN).matcher(filename);
		if (idMatcher.find())
			this.id = new ID(idMatcher.group(1));

		Matcher fingerprintMatcher = Pattern.compile(FINGERPRINT_PATTERN)
				.matcher(filename);
		if (fingerprintMatcher.find())
			this.fingerprint = new Fingerprint(fingerprintMatcher.group(1));

		scanRecords(doclogFile);
		calculateRecordMillisecondsPassed();
	}

	public void scanRecords(String doclogFile) {
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
	 * @deprecated
	 */
	public Token getToken() {
		Pattern surveyEntryPattern = Pattern
				.compile("\tsurvey-([A-Za-z0-9]+)\t"); // action type
		Pattern surveyQueryPattern = Pattern
				.compile("[\\?|&]token=([A-Za-z0-9]+)"); // token in url
		try {
			FileInputStream fstream = new FileInputStream(this);
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
	public boolean isInRange(DateRange dateRange) {
		if (this.doclogRecords != null)
			return this.doclogRecords.isInRange(dateRange);
		else
			return false;
	}
}