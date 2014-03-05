package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

	@SuppressWarnings("serial")
	@Test
	public void getDateTest() {
		Assert.assertEquals(new Date() {
			{
				this.setTime(0l);
			}
		}, DateUtil.getDate(1970, 0, 1, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				this.setTime(283996800000l);
			}
		}, DateUtil.getDate(1979, 0, 1, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				this.setTime(294364800000l);
			}
		}, DateUtil.getDate(1979, 4, 1, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				this.setTime(295574400000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				this.setTime(295624800000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 0, 0));

		Assert.assertEquals(new Date() {
			{
				this.setTime(295626600000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 30, 0));

		Assert.assertEquals(new Date() {
			{
				this.setTime(295626617000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 30, 17));

		Assert.assertEquals(new Date() {
			{
				this.setTime(295626617367l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 30, 17, 367));

		Assert.assertEquals(new Date() {
			{
				this.setTime(453479417694l);
			}
		}, DateUtil.getDate(1984, 4, 15, 14, 30, 17, 694));
	}

	@Test
	public void testIsUnixTimeStart() {
		Assert.assertEquals(false, DateUtil.isUnixTimeStart(DateUtil.getDate(
				1979, 4, 15, 14, 30, 17, 367)));
		Assert.assertEquals(false, DateUtil.isUnixTimeStart(DateUtil.getDate(
				1984, 4, 15, 14, 30, 17, 694)));
		Assert.assertEquals(false,
				DateUtil.isUnixTimeStart(DateUtil.getDate(0, 0, 0, 0, 0, 0, 0)));
		Assert.assertEquals(false, DateUtil.isUnixTimeStart(DateUtil.getDate(
				1970, 0, 0, 0, 0, 0, 0)));
		Assert.assertEquals(true, DateUtil.isUnixTimeStart(DateUtil.getDate(
				1970, 0, 1, 0, 0, 0, 0)));
		Assert.assertEquals(false, DateUtil.isUnixTimeStart(DateUtil.getDate(
				1970, 0, 1, 0, 0, 0, 1)));
		Assert.assertEquals(true, DateUtil.isUnixTimeStart(null));
	}

	@Test
	public void testFromISO8601() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss Z");

		Calendar calendar = DateUtil.fromISO8601("2011-11-18T15:38:28+09:00");

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+9"));
		Assert.assertEquals("2011-11-18 15:38:28 +0900",
				dateFormat.format(calendar.getTime()));

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5"));
		Assert.assertEquals("2011-11-18 11:38:28 +0500",
				dateFormat.format(calendar.getTime()));

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5"));
		Assert.assertEquals("2011-11-18 01:38:28 -0500",
				dateFormat.format(calendar.getTime()));
	}

	@Test
	public void testToISO8601() {
		Assert.assertEquals("2011-11-18T15:38:28.000+09:00", DateUtil
				.toISO8601(DateUtil.fromISO8601("2011-11-18T15:38:28+09:00")));

		// over-accuracy, missing : in timezone, - instead of :
		Assert.assertEquals("2013-09-18T17:45:54.889+02:00", DateUtil
				.toISO8601(DateUtil
						.fromISO8601("2013-09-18T17-45-54.88891500+02:00")));
	}
}
