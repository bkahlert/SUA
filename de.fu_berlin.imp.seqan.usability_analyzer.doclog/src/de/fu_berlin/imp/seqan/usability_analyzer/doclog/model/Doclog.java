package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.net.URI;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.WrappingData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt.DoclogLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class Doclog extends WrappingData implements IData, HasDateRange,
		ILocatable, HasIdentifier {

	Logger logger = Logger.getLogger(Doclog.class);

	private static final long serialVersionUID = 5159431028889474752L;
	public static final Pattern IDENTIFIER_PATTERN = Pattern
			.compile("^(!?[A-Za-z\\d]+)\\.doclog$");

	public static IIdentifier getIdentifier(IData doclogDataResource) {
		IIdentifier identifier = null;
		Matcher matcher = IDENTIFIER_PATTERN.matcher(doclogDataResource
				.getName());
		if (matcher.find()) {
			identifier = IdentifierFactory.createFrom(matcher.group(1));
		}
		return identifier;
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
				if (surveyEntryMatcher.find()) {
					return new Token(surveyEntryMatcher.group(1));
				}

				Matcher surveyQueryMatcher = surveyQueryPattern
						.matcher(strLine);
				if (surveyQueryMatcher.find()) {
					return new Token(surveyQueryMatcher.group(1));
				}
			}
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return null;
	}

	private IIdentifier identifier;
	private TimeZoneDateRange dateRange;
	private Token token;

	private DoclogRecordList doclogRecords;

	/**
	 * Creates a new {@link Doclog} instance.
	 * 
	 * @param data
	 * @param i
	 */
	public Doclog(IData data, IIdentifier identifier,
			TimeZoneDateRange dateRange, Token token,
			Integer consolideTypingIfLessThanMillies) {
		super(data);

		this.identifier = identifier;
		this.dateRange = dateRange;
		this.token = token;

		this.scanRecords();
		this.calculateRecordMillisecondsPassed();
		if (consolideTypingIfLessThanMillies != null) {
			this.consolidateTypings(consolideTypingIfLessThanMillies);
		}
	}

	@Override
	public URI getUri() {
		try {
			return new URI("sua://" + DoclogLocatorProvider.DOCLOG_NAMESPACE
					+ "/" + this.getIdentifier().toString());
		} catch (Exception e) {
			this.logger
					.error("Could not create ID for a "
							+ Doclog.class.getSimpleName(), e);
		}
		return null;
	}

	public void scanRecords() {
		this.doclogRecords = new DoclogRecordList();
		try {
			for (String line : this) {
				try {
					DoclogRecord record = new DoclogRecord(this, line);
					this.doclogRecords.add(record);
				} catch (DataSourceInvalidException e) {
					this.logger.warn(
							"Skipped " + DoclogRecord.class.getSimpleName(), e);
				}
			}
		} catch (Exception e) {// Catch exception if any
			this.logger.error("Could not open doclog file", e);
		}
	}

	private void calculateRecordMillisecondsPassed() {
		Collections.sort(this.doclogRecords);
		for (int i = 0; i < this.doclogRecords.size(); i++) {
			this.calculateRecordMillisecondsPassed(i);
		}
	}

	public void calculateRecordMillisecondsPassed(int i) {
		DoclogRecord doclogRecord = this.doclogRecords.get(i);
		DoclogRecord successor = (this.doclogRecords.size() > i + 1) ? this.doclogRecords
				.get(i + 1) : null;
		if (successor != null) {
			Long millisecondsPassed = successor.getDate().getTime()
					- doclogRecord.getDate().getTime();
			if (millisecondsPassed < 0) {
				this.logger.error(DoclogRecord.class.getSimpleName()
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

	private void consolidateTypings(int consolideTypingIfLessThanMillies) {
		Long lastTime = null;
		for (int i = 0; i < this.doclogRecords.size(); i++) {
			DoclogRecord currentRecord = this.doclogRecords.get(i);
			if (currentRecord.getAction() != DoclogAction.TYPING) {
				continue;
			}
			while (this.doclogRecords.size() > i + 1) {
				DoclogRecord nextRecord = this.doclogRecords.get(i + 1);
				if (nextRecord.getAction() != DoclogAction.TYPING) {
					break;
				}
				if (lastTime == null) {
					lastTime = currentRecord.getDate().getTime();
				}
				if (nextRecord.getDate().getTime() - lastTime >= consolideTypingIfLessThanMillies) {
					lastTime = null;
					break;
				}
				currentRecord.setActionParameter(nextRecord
						.getActionParameter());
				this.doclogRecords.remove(i + 1);
				this.calculateRecordMillisecondsPassed(i);
				lastTime = nextRecord.getDate().getTime();
			}
		}
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.identifier;
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

		for (DoclogRecord doclogRecord : this.doclogRecords) {
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

	@Override
	public String toString() {
		return Doclog.class.getSimpleName() + "(" + this.doclogRecords.size()
				+ ")";
	}
}