package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DateRangeTest {

	// -2 years
	private Date muchBeforeRangeDate = DateUtil
			.getDate(1982, 04, 15, 14, 30, 0);

	// -1 year, -1 month
	private Date beforeRangeDate = DateUtil.getDate(1983, 03, 15, 14, 30, 0);

	// -1 year
	private Date rangeStart = DateUtil.getDate(1983, 04, 15, 14, 30, 0);

	// fixed
	private Date inRangeDate = DateUtil.getDate(1984, 04, 15, 14, 30, 0);

	// +1 year
	private Date rangeEnd = DateUtil.getDate(1985, 04, 15, 14, 30, 0);

	// +1 year, +1 month
	private Date afterRangeDate = DateUtil.getDate(1985, 05, 15, 14, 30, 0);

	// -2 years
	private Date muchAfterRangeDate = DateUtil.getDate(1986, 04, 15, 14, 30, 0);

	@Test
	public void boundedRangeDateTest() {
		DateRange boundedRangeDate = new DateRange(rangeStart, rangeEnd);

		Assert.assertTrue(boundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertFalse(boundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(boundedRangeDate.isAfterRange(beforeRangeDate));

		Assert.assertFalse(boundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(boundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(boundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(boundedRangeDate.isBeforeRange(afterRangeDate));
		Assert.assertFalse(boundedRangeDate.isInRange(afterRangeDate));
		Assert.assertTrue(boundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertFalse(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchBeforeRangeDate)));
		Assert.assertFalse(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new DateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, afterRangeDate)));
		Assert.assertFalse(boundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new DateRange(
				muchAfterRangeDate, muchAfterRangeDate)));
	}

	@Test
	public void leftUnboundedRangeDateTest() {
		DateRange leftUnboundedRangeDate = new DateRange(null, rangeEnd);

		Assert.assertFalse(leftUnboundedRangeDate
				.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(leftUnboundedRangeDate.isAfterRange(beforeRangeDate));

		Assert.assertFalse(leftUnboundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(leftUnboundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(leftUnboundedRangeDate.isBeforeRange(afterRangeDate));
		Assert.assertFalse(leftUnboundedRangeDate.isInRange(afterRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchBeforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, afterRangeDate)));
		Assert.assertFalse(leftUnboundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate.isIntersected(new DateRange(
				muchAfterRangeDate, muchAfterRangeDate)));
	}

	@Test
	public void rightUnboundedRangeDateTest() {
		DateRange rightUnboundedRangeDate = new DateRange(rangeStart, null);

		Assert.assertTrue(rightUnboundedRangeDate
				.isBeforeRange(beforeRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate
				.isAfterRange(beforeRangeDate));

		Assert.assertFalse(rightUnboundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(rightUnboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(rightUnboundedRangeDate
				.isBeforeRange(afterRangeDate));
		Assert.assertTrue(rightUnboundedRangeDate.isInRange(afterRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertFalse(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchBeforeRangeDate)));
		Assert.assertFalse(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(rightUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new DateRange(
				muchAfterRangeDate, muchAfterRangeDate)));
	}

	@Test
	public void unboundedRangeDateTest() {
		DateRange unboundedRangeDate = new DateRange(null, null);

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(beforeRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(afterRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(afterRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchBeforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				afterRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new DateRange(
				muchAfterRangeDate, muchAfterRangeDate)));
	}

}
