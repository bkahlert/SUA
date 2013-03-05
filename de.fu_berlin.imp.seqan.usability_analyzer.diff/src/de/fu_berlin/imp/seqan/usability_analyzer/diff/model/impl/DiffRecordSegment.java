package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment;

public class DiffRecordSegment implements IDiffRecordSegment {

	private static final long serialVersionUID = 3956746799123197525L;

	private static final Logger LOGGER = Logger
			.getLogger(DiffRecordSegment.class);

	private IDiffRecord diffRecord;
	private long segmentStart;
	private long segmentEnd;

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
	 * #getID()
	 */
	@Override
	public ID getID() {
		return this.diffRecord.getID();
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
		try {
			return new URI(diffRecord.getUri().toString() + "#"
					+ segmentStart + "+" + segmentEnd);
		} catch (URISyntaxException e) {
			LOGGER.fatal(
					"Could not create ID for a "
							+ DiffRecordSegment.class.getSimpleName(), e);
			return null;
		}
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
		return diffRecord;
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
		return segmentStart;
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
		return segmentEnd;
	}

}
