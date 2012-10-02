package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt.DoclogCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class Doclog implements IData, HasDateRange, ICodeable, HasID,
		HasFingerprint {

	Logger logger = Logger.getLogger(Doclog.class);

	private static final long serialVersionUID = 5159431028889474752L;
	public static final Pattern ID_PATTERN = Pattern
			.compile("^([A-Za-z\\d]+)\\.doclog$");
	public static final Pattern FINGERPRINT_PATTERN = Pattern
			.compile("^(![A-Za-z\\d]+)\\.doclog$");

	public static ID getID(IData doclogDataResource) {
		ID id = null;
		Matcher matcher = ID_PATTERN.matcher(doclogDataResource.getName());
		if (matcher.find())
			id = new ID(matcher.group(1));
		return id;
	}

	public static Fingerprint getFingerprint(IData doclogDataResource) {
		Fingerprint fingerprint = null;
		Matcher matcher = FINGERPRINT_PATTERN.matcher(doclogDataResource
				.getName());
		if (matcher.find())
			fingerprint = new Fingerprint(matcher.group(1));
		return fingerprint;
	}

	public static TimeZoneDateRange getDateRange(IData data) {
		return new TimeZoneDateRange(
				DoclogRecord.getDate(data.readFirstLine()),
				DoclogRecord.getDate(data.readLastLines(1)));
	}

	public static Token getToken(IData data) {
		Pattern surveyEntryPattern = Pattern
				.compile("\tsurvey-([A-Za-z0-9]+)\t"); // action type
		Pattern surveyQueryPattern = Pattern
				.compile("[\\?|&]token=([A-Za-z0-9]+)"); // token in url
		try {
			for (String strLine : data) {
				Matcher surveyEntryMatcher = surveyEntryPattern
						.matcher(strLine);
				if (surveyEntryMatcher.find())
					return new Token(surveyEntryMatcher.group(1));

				Matcher surveyQueryMatcher = surveyQueryPattern
						.matcher(strLine);
				if (surveyQueryMatcher.find())
					return new Token(surveyQueryMatcher.group(1));
			}
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return null;
	}

	private IData data;

	private ID id;
	private Fingerprint fingerprint;
	private TimeZoneDateRange dateRange;
	private Token token;

	private DoclogRecordList doclogRecords;

	/**
	 * Creates a new {@link Doclog} instance. The instance is {@link ID} based
	 * XOR {@link Fingerprint} based depending on the file's name.
	 * 
	 * @param data
	 */
	public Doclog(IData data, Object key, TimeZoneDateRange dateRange,
			Token token) {
		org.eclipse.core.runtime.Assert.isNotNull(data.getBaseDataContainer());
		this.data = data;

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

		scanRecords();
		calculateRecordMillisecondsPassed();
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

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI("sua://"
					+ DoclogCodeableProvider.DOCLOG_NAMESPACE
					+ "/"
					+ ((getID() != null) ? getID().toString()
							: getFingerprint().toString()));
		} catch (Exception e) {
			logger.error(
					"Could not create ID for a " + Doclog.class.getSimpleName(),
					e);
		}
		return null;
	}

	public void scanRecords() {
		this.doclogRecords = new DoclogRecordList();
		try {
			for (String line : this.data) {
				try {
					this.doclogRecords.add(new DoclogRecord(this, line));
				} catch (DataSourceInvalidException e) {
					logger.warn(
							"Skipped " + DoclogRecord.class.getSimpleName(), e);
				}
			}
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

	public ID getID() {
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

	/**
	 * Returns all containing {@link DoclogRecord}s.
	 * 
	 * @return
	 */
	public DoclogRecordList getDoclogRecords() {
		return this.doclogRecords;
	}

	/**
	 * @see DoclogRecordList#getSuccessor(DoclogRecord)
	 */
	public DoclogRecord getNextDoclogRecord(DoclogRecord doclogRecord) {
		return this.doclogRecords.getSuccessor(doclogRecord);
	}

	/**
	 * @see DoclogRecordList#getPredecessor(DoclogRecord)
	 */
	public DoclogRecord getPrevDoclogRecord(DoclogRecord doclogRecord) {
		return this.doclogRecords.getPredecessor(doclogRecord);
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