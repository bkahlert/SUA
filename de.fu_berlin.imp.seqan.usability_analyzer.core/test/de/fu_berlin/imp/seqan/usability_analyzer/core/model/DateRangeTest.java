package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DateRangeTest {

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

	@Test
	public void boundedRangeDateTest() {
		DateRange boundedRangeDate = new DateRange(rangeStart, rangeEnd);

		Assert.assertTrue(boundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(boundedRangeDate.isInRange(inRangeDate));
		Assert.assertTrue(boundedRangeDate.isAfterRange(afterRangeDate));
	}

	@Test
	public void leftUnboundedRangeDateTest() {
		DateRange leftUnboundedRangeDate = new DateRange(null, rangeEnd);

		Assert.assertFalse(leftUnboundedRangeDate
				.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate.isInRange(inRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate.isAfterRange(afterRangeDate));
	}

	@Test
	public void rightUnboundedRangeDateTest() {
		DateRange rightUnboundedRangeDate = new DateRange(rangeStart, null);

		Assert.assertTrue(rightUnboundedRangeDate
				.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(rightUnboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate.isAfterRange(afterRangeDate));
	}

	@Test
	public void unboundedRangeDateTest() {
		DateRange unboundedRangeDate = new DateRange(null, null);

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(afterRangeDate));
	}

}
