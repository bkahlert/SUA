package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.net.URI;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment;

public class DiffRecordSegment implements IDiffRecordSegment {

	private static final long serialVersionUID = 3956746799123197525L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(DiffRecordSegment.class);

	private URI uri;
	private final IDiffRecord diffRecord;
	private final long segmentStart;
	private final long segmentEnd;

	public DiffRecordSegment(IDiffRecord diffRecord, long segmentStart,
			long segmentEnd) {
		assert diffRecord != null;
		this.diffRecord = diffRecord;
		this.segmentStart = segmentStart;
		this.segmentEnd = segmentEnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment
	 * #getIdentifier()
	 */
	@Override
	public IIdentifier getIdentifier() {
		return this.diffRecord.getIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment
	 * #getDateRange()
	 */
	@Override
	public TimeZoneDateRange getDateRange() {
		return this.diffRecord.getDateRange();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment
	 * #getCodeInstanceID()
	 */
	@Override
	public URI getUri() {
		if (this.uri == null) {
			try {
				this.uri = new URI(this.diffRecord.getUri().toString() + "#"
						+ this.segmentStart + "+" + this.segmentEnd);
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + DiffRecordSegment.class, e);
			}
		}
		return this.uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment
	 * #getDiffFileRecord()
	 */
	@Override
	public IDiffRecord getDiffFileRecord() {
		return this.diffRecord;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment
	 * #getSegmentStart()
	 */
	@Override
	public long getSegmentStart() {
		return this.segmentStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment
	 * #getSegmentLength()
	 */
	@Override
	public long getSegmentLength() {
		return this.segmentEnd;
	}

}
