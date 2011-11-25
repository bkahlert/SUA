package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import org.junit.Assert;
import org.junit.Test;

public class TimeZoneDateTest {

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
						addMilliseconds(5000l);
					}
				}) == 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:20+09:00")
				.compareTo(new TimeZoneDate("2011-11-18T15:38:28+08:00") {
					{
						addMilliseconds(-8000);
					}
				}) != 0);
		Assert.assertTrue(new TimeZoneDate("2011-11-18T15:38:20+09:00")
				.compareToTimeZoneLess(new TimeZoneDate(
						"2011-11-18T15:38:28+08:00") {
					{
						addMilliseconds(-8000);
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

}
