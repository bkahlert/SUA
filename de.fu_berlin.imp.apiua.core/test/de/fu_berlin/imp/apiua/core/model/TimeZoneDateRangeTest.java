package de.fu_berlin.imp.apiua.core.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;

public class TimeZoneDateRangeTest {

	// -2 years
	private TimeZoneDate muchBeforeRangeDate = new TimeZoneDate(
			"1982-05-15T14:30:00+01:00");

	// -1 year, -1 month
	private TimeZoneDate beforeRangeDate = new TimeZoneDate(
			"1983-04-15T14:30:00+01:00");

	// -1 year
	private TimeZoneDate rangeStart = new TimeZoneDate(
			"1983-05-15T14:30:00+01:00");

	// fixed
	private TimeZoneDate inRangeDate = new TimeZoneDate(
			"1984-05-15T14:30:00+01:00");

	// +1 year
	private TimeZoneDate rangeEnd = new TimeZoneDate(
			"1985-05-15T14:30:00+01:00");

	// +1 year, +1 month
	private TimeZoneDate afterRangeDate = new TimeZoneDate(
			"1985-06-15T14:30:00+01:00");

	// -2 years
	private TimeZoneDate muchAfterRangeDate = new TimeZoneDate(
			"1986-05-15T14:30:00+01:00");

	@Test
	public void testValidRange() {
		TimeZoneDateRange dateRange = new TimeZoneDateRange(rangeStart,
				rangeEnd);
		Assert.assertEquals(rangeStart, dateRange.getStartDate());
		Assert.assertEquals(rangeEnd, dateRange.getEndDate());

		dateRange = new TimeZoneDateRange(rangeStart, rangeStart);
		Assert.assertEquals(rangeStart, dateRange.getStartDate());
		Assert.assertEquals(rangeStart, dateRange.getEndDate());

		dateRange = new TimeZoneDateRange(rangeStart, null);
		Assert.assertEquals(rangeStart, dateRange.getStartDate());
		Assert.assertNull(dateRange.getEndDate());

		dateRange = new TimeZoneDateRange(null, rangeEnd);
		Assert.assertNull(dateRange.getStartDate());
		Assert.assertEquals(rangeEnd, dateRange.getEndDate());

		dateRange = new TimeZoneDateRange(null, null);
		Assert.assertNull(dateRange.getStartDate());
		Assert.assertNull(dateRange.getEndDate());
	}

	@Test(expected = InvalidParameterException.class)
	public void testInvalidRange() {
		new TimeZoneDateRange(new TimeZoneDate("2011-11-18T15:38:28+09:00"),
				new TimeZoneDate("2011-11-18T14:38:28+09:00"));
	}

	@Test(expected = InvalidParameterException.class)
	public void testInvalidRange2() {
		new TimeZoneDateRange(new TimeZoneDate("2011-11-18T14:38:28+09:00"),
				new TimeZoneDate("2011-11-18T14:38:28+10:00"));
	}

	@Test
	public void boundedRangeDateTest() {
		TimeZoneDateRange boundedRangeDate = new TimeZoneDateRange(rangeStart,
				rangeEnd);

		Assert.assertTrue(boundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertFalse(boundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(boundedRangeDate.isAfterRange(beforeRangeDate));

		Assert.assertFalse(boundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(boundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(boundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(boundedRangeDate.isBeforeRange(afterRangeDate));
		Assert.assertFalse(boundedRangeDate.isInRange(afterRangeDate));
		Assert.assertTrue(boundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertFalse(boundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchBeforeRangeDate)));
		Assert.assertFalse(boundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new TimeZoneDateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						afterRangeDate)));
		Assert.assertFalse(boundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchAfterRangeDate,
						muchAfterRangeDate)));
	}

	@Test
	public void leftUnboundedRangeDateTest() {
		TimeZoneDateRange leftUnboundedRangeDate = new TimeZoneDateRange(null,
				rangeEnd);

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

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchBeforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate,
						afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd,
						muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						afterRangeDate)));
		Assert.assertFalse(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchAfterRangeDate,
						muchAfterRangeDate)));
	}

	@Test
	public void rightUnboundedRangeDateTest() {
		TimeZoneDateRange rightUnboundedRangeDate = new TimeZoneDateRange(
				rangeStart, null);

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

		Assert.assertFalse(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchBeforeRangeDate)));
		Assert.assertFalse(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertFalse(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchAfterRangeDate,
						muchAfterRangeDate)));
	}

	@Test
	public void unboundedRangeDateTest() {
		TimeZoneDateRange unboundedRangeDate = new TimeZoneDateRange(null, null);

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(beforeRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(afterRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(afterRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchBeforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						rangeStart)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						rangeEnd)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchBeforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						rangeStart)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(beforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, rangeStart)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeStart,
						muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate,
						afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(inRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(rangeEnd,
						muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(afterRangeDate,
						muchAfterRangeDate)));
		Assert.assertTrue(unboundedRangeDate
				.isIntersected(new TimeZoneDateRange(muchAfterRangeDate,
						muchAfterRangeDate)));
	}

	@Test
	public void testCalculateOuterDateRange() {
		TimeZoneDateRange outerRange;

		outerRange = TimeZoneDateRange
				.calculateOuterDateRange(new TimeZoneDateRange[0]);
		Assert.assertNull(outerRange.getStartDate());
		Assert.assertNull(outerRange.getEndDate());

		// one moment
		outerRange = TimeZoneDateRange
				.calculateOuterDateRange(new TimeZoneDateRange(
						muchBeforeRangeDate, muchBeforeRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getEndDate());

		// one range
		outerRange = TimeZoneDateRange
				.calculateOuterDateRange(new TimeZoneDateRange(
						muchBeforeRangeDate, muchAfterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		// two ranges
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, muchAfterRangeDate),
				new TimeZoneDateRange(beforeRangeDate, afterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(beforeRangeDate, muchAfterRangeDate),
				new TimeZoneDateRange(muchBeforeRangeDate, afterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, afterRangeDate),
				new TimeZoneDateRange(beforeRangeDate, muchAfterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(beforeRangeDate, afterRangeDate),
				new TimeZoneDateRange(muchBeforeRangeDate, muchAfterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		// null
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, muchAfterRangeDate),
				null, new TimeZoneDateRange(beforeRangeDate, afterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		// left partially unbounded
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(null, muchAfterRangeDate), null,
				new TimeZoneDateRange(beforeRangeDate, afterRangeDate));
		Assert.assertEquals(beforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, muchAfterRangeDate),
				null, new TimeZoneDateRange(null, afterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		// left unbounded
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(null, muchAfterRangeDate),
				new TimeZoneDateRange(null, afterRangeDate));
		Assert.assertNull(outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		// right partially unbounded
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, null),
				new TimeZoneDateRange(beforeRangeDate, afterRangeDate));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(afterRangeDate, outerRange.getEndDate());

		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, muchAfterRangeDate),
				new TimeZoneDateRange(beforeRangeDate, null));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(muchAfterRangeDate, outerRange.getEndDate());

		// right unbounded
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(muchBeforeRangeDate, null),
				new TimeZoneDateRange(beforeRangeDate, null));
		Assert.assertEquals(muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertNull(outerRange.getEndDate());

		// both unbounded
		outerRange = TimeZoneDateRange.calculateOuterDateRange(
				new TimeZoneDateRange(null, null), new TimeZoneDateRange(null,
						null));
		Assert.assertNull(outerRange.getStartDate());
		Assert.assertNull(outerRange.getEndDate());
	}

	@Test
	public void testIsIntersected() {
		TimeZoneDateRange r1 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-13T14:32:03+02:00"), new TimeZoneDate(
				"2011-09-13T15:27:23+02:00"));
		TimeZoneDateRange r2 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-13T14:32:03+02:00"), new TimeZoneDate(
				"2011-09-13T15:27:24+02:00"));
		TimeZoneDateRange r3 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-13T15:27:23+02:00"), new TimeZoneDate(
				"2011-09-13T15:27:37+02:00"));
		TimeZoneDateRange r4 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-14T15:27:23+02:00"), new TimeZoneDate(
				"2011-09-14T15:27:37+02:00"));

		assertTrue(r1.isIntersected(r2));
		assertTrue(r2.isIntersected(r1));

		assertTrue(r1.isIntersected(r3));
		assertTrue(r3.isIntersected(r1));

		assertFalse(r1.isIntersected(r4));
		assertFalse(r4.isIntersected(r1));

		assertTrue(r2.isIntersected(r3));
		assertTrue(r3.isIntersected(r2));

		assertFalse(r2.isIntersected(r4));
		assertFalse(r4.isIntersected(r2));

		assertFalse(r3.isIntersected(r4));
		assertFalse(r4.isIntersected(r3));
	}

	@Test
	public void testIsIntersected2() {
		TimeZoneDateRange r1 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-13T14:32:03+02:00"), new TimeZoneDate(
				"2011-09-13T15:27:23+02:00"));
		TimeZoneDateRange r2 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-13T14:32:03+02:00"), new TimeZoneDate(
				"2011-09-13T15:27:24+02:00"));
		TimeZoneDateRange r3 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-13T15:27:23+02:00"), new TimeZoneDate(
				"2011-09-13T15:27:37+02:00"));
		TimeZoneDateRange r4 = new TimeZoneDateRange(new TimeZoneDate(
				"2011-09-14T15:27:23+02:00"), new TimeZoneDate(
				"2011-09-14T15:27:37+02:00"));

		assertTrue(r1.isIntersected2(r2));
		assertTrue(r2.isIntersected2(r1));

		assertFalse(r1.isIntersected2(r3));
		assertFalse(r3.isIntersected2(r1));

		assertFalse(r1.isIntersected2(r4));
		assertFalse(r4.isIntersected2(r1));

		assertTrue(r2.isIntersected2(r3));
		assertTrue(r3.isIntersected2(r2));

		assertFalse(r2.isIntersected2(r4));
		assertFalse(r4.isIntersected2(r2));

		assertFalse(r3.isIntersected2(r4));
		assertFalse(r4.isIntersected2(r3));
	}

}
