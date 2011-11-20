package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import org.junit.Assert;
import org.junit.Test;

public class DateRangeTest {

	// -2 years
	private LocalDate muchBeforeRangeDate = new LocalDate(
			"1982-05-15T14:30:00+01:00");

	// -1 year, -1 month
	private LocalDate beforeRangeDate = new LocalDate(
			"1983-04-15T14:30:00+01:00");

	// -1 year
	private LocalDate rangeStart = new LocalDate("1983-05-15T14:30:00+01:00");

	// fixed
	private LocalDate inRangeDate = new LocalDate("1984-05-15T14:30:00+01:00");

	// +1 year
	private LocalDate rangeEnd = new LocalDate("1985-05-15T14:30:00+01:00");

	// +1 year, +1 month
	private LocalDate afterRangeDate = new LocalDate(
			"1985-06-15T14:30:00+01:00");

	// -2 years
	private LocalDate muchAfterRangeDate = new LocalDate(
			"1986-05-15T14:30:00+01:00");

	@Test
	public void boundedRangeDateTest() {
		LocalDateRange boundedRangeDate = new LocalDateRange(rangeStart,
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

		Assert.assertFalse(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, muchBeforeRangeDate)));
		Assert.assertFalse(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new LocalDateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new LocalDateRange(
				afterRangeDate, afterRangeDate)));
		Assert.assertFalse(boundedRangeDate.isIntersected(new LocalDateRange(
				afterRangeDate, muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new LocalDateRange(
				muchAfterRangeDate, muchAfterRangeDate)));
	}

	@Test
	public void leftUnboundedRangeDateTest() {
		LocalDateRange leftUnboundedRangeDate = new LocalDateRange(null,
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
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						muchBeforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate, rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate, inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeEnd, rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeEnd, afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeEnd, muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(afterRangeDate,
						afterRangeDate)));
		Assert.assertFalse(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(afterRangeDate,
						muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchAfterRangeDate,
						muchAfterRangeDate)));
	}

	@Test
	public void rightUnboundedRangeDateTest() {
		LocalDateRange rightUnboundedRangeDate = new LocalDateRange(rangeStart,
				null);

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
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						muchBeforeRangeDate)));
		Assert.assertFalse(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchBeforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertFalse(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate,
						beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate, rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(beforeRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeStart,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate, inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(inRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeEnd, rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeEnd, afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(rangeEnd, muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(afterRangeDate,
						afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(afterRangeDate,
						muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate
				.isIntersected(new LocalDateRange(muchAfterRangeDate,
						muchAfterRangeDate)));
	}

	@Test
	public void unboundedRangeDateTest() {
		LocalDateRange unboundedRangeDate = new LocalDateRange(null, null);

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(beforeRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(beforeRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(beforeRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(inRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(inRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(inRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(afterRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(afterRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(afterRangeDate));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, muchBeforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchBeforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				beforeRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeStart, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				inRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeEnd, rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeEnd, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				rangeEnd, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				afterRangeDate, afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				afterRangeDate, muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new LocalDateRange(
				muchAfterRangeDate, muchAfterRangeDate)));
	}

}
