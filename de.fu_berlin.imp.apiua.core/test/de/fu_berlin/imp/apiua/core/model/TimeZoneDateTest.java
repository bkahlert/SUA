package de.fu_berlin.imp.apiua.core.model;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;

public class TimeZoneDateTest {

	@Test
	public void defaultConstructor() {
		Assert.assertTrue(Math.abs(new Date().getTime()
				- new TimeZoneDate().getTime()) < 10);
	}

	@Test
	public void testFormat() {
		String pattern = "yyyy-MM-dd HH:mm:ss Z";

		Assert.assertEquals("2011-11-18 15:38:28 +0900", new TimeZoneDate(
				"2011-11-18T15:38:28+09:00").format(pattern));
		Assert.assertEquals("1984-05-15 14:30:00 +0900", new TimeZoneDate(
				"1984-05-15T14:30:00+09:00").format(pattern));
	}

	@Test
	public void testGetTime() {
		Assert.assertEquals(1321598308000l, new TimeZoneDate(
				"2011-11-18T15:38:28+09:00").getTime());
		Assert.assertEquals(1321643308000l, new TimeZoneDate(
				"2011-11-18T15:38:28-03:30").getTime());
	}

	@Test
	public void testGetTimeZone() {
		Assert.assertEquals(TimeZone.getTimeZone("GMT+09:00"),
				new TimeZoneDate("2011-11-18T15:38:28+09:00").getTimeZone());
		Assert.assertEquals(TimeZone.getTimeZone("GMT-05:00"),
				new TimeZoneDate("2011-11-18T15:38:28-05:00").getTimeZone());
		Assert.assertEquals(TimeZone.getTimeZone("GMT+00:00"),
				new TimeZoneDate("2011-11-18T15:38:28").getTimeZone());
	}

	@Test
	public void testCompareTo() {
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+09:00")) == 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+08:00")) < 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-17T15:38:28+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+09:00")) < 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-19T15:38:28+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+09:00")) > 0);
	}

	@Test
	public void testCompareToTimeZoneLess() {
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.compareToTimeZoneLess(new TimeZoneDate(
						"2011-11-18T15:38:28+09:00")) == 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.compareToTimeZoneLess(new TimeZoneDate(
						"2011-11-18T15:38:28+08:00")) == 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-17T15:38:28+09:00")
				.compareToTimeZoneLess(new TimeZoneDate(
						"2011-11-18T15:38:28+09:00")) < 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-19T15:38:28+09:00")
				.compareToTimeZoneLess(new TimeZoneDate(
						"2011-11-18T15:38:28+09:00")) > 0);
	}

	@Test
	public void testEquals() {
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.equals(new TimeZoneDate("2011-11-18T15:38:28+09:00")));
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.equals(new TimeZoneDate("2011-11-18T14:38:28+08:00")));
		Assert.assertFalse(new TimeZoneDate("2011-11-19T15:38:28+09:00")
				.equals(new TimeZoneDate("2011-11-18T15:38:28+09:00")));
	}

	@Test
	public void testAfter() {
		Assert.assertFalse(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.after(new TimeZoneDate("2011-11-18T15:38:28+09:00")));
		Assert.assertFalse(new TimeZoneDate("2011-11-18T15:38:28+09:00")
				.after(new TimeZoneDate("2011-11-18T15:38:28+08:00")));
		Assert.assertTrue(new TimeZoneDate("2011-11-19T15:38:28+09:00")
				.after(new TimeZoneDate("2011-11-18T15:38:28+09:00")));
	}

	@Test
	public void testAddMilliseconds() {
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:33+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+09:00") {
					{
						this.addMilliseconds(5000l);
					}
				}) == 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:20+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+08:00") {
					{
						this.addMilliseconds(-8000);
					}
				}) != 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:20+09:00")
				.compareToTimeZoneLess(new TimeZoneDate(
						"2011-11-18T15:38:28+08:00") {
					{
						this.addMilliseconds(-8000);
					}
				}) == 0);
	}

	@Test
	public void testClone() {
		TimeZoneDate original = new TimeZoneDate("2011-11-18T15:38:28+09:00");
		TimeZoneDate clone = original.clone();
		clone.addMilliseconds(1000);
		Assert.assertFalse(original.getTime() == clone.getTime());
		Assert.assertTrue(original.getTime() - clone.getTime() == -1000);
	}

	@Test
	public void testFormatDuration() {
		assertEquals("1s", new TimeZoneDateRange(new TimeZoneDate(
				"2011-11-18T15:38:28+09:00"), new TimeZoneDate(
				"2011-11-18T15:38:29+09:00")).formatDuration());
		assertEquals("1m 00s", new TimeZoneDateRange(new TimeZoneDate(
				"2011-11-18T15:38:28+09:00"), new TimeZoneDate(
				"2011-11-18T15:39:28+09:00")).formatDuration());
		assertEquals("1h 00m 00s", new TimeZoneDateRange(new TimeZoneDate(
				"2011-11-18T15:38:28+09:00"), new TimeZoneDate(
				"2011-11-18T16:38:28+09:00")).formatDuration());

		assertEquals("25h 00m 00s", new TimeZoneDateRange(new TimeZoneDate(
				"2011-11-18T15:38:28+09:00"), new TimeZoneDate(
				"2011-11-19T16:38:28+09:00")).formatDuration());

		assertEquals("25h 10m 04s", new TimeZoneDateRange(new TimeZoneDate(
				"2011-11-18T15:38:28+09:00"), new TimeZoneDate(
				"2011-11-19T16:48:32+09:00")).formatDuration());
	}

}
