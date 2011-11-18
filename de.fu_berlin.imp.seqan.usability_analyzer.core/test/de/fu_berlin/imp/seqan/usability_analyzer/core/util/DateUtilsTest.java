package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.security.InvalidParameterException;
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
				setTime(0l);
			}
		}, DateUtil.getDate(1970, 0, 1, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				setTime(283996800000l);
			}
		}, DateUtil.getDate(1979, 0, 1, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				setTime(294364800000l);
			}
		}, DateUtil.getDate(1979, 4, 1, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				setTime(295574400000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 0, 0, 0));

		Assert.assertEquals(new Date() {
			{
				setTime(295624800000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 0, 0));

		Assert.assertEquals(new Date() {
			{
				setTime(295626600000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 30, 0));

		Assert.assertEquals(new Date() {
			{
				setTime(295626617000l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 30, 17));

		Assert.assertEquals(new Date() {
			{
				setTime(295626617367l);
			}
		}, DateUtil.getDate(1979, 4, 15, 14, 30, 17, 367));

		Assert.assertEquals(new Date() {
			{
				setTime(453479417694l);
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
	public void nanoDateStringToMilliDateString() {
		Object[] rt;

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-09-13 12:10:14.578125000 +0200");
		Assert.assertEquals("2011-09-13 12:10:14.578 +0200", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-09-13 12:10:14.578125000 +1100");
		Assert.assertEquals("2011-09-13 12:10:14.578 +1100", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 12:10:14.578825000 +1100");
		Assert.assertEquals("2011-01-13 12:10:14.579 +1100", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 12:10:14.570000000 +1100");
		Assert.assertEquals("2011-01-13 12:10:14.570 +1100", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 12:10:14.500000000 +1100");
		Assert.assertEquals("2011-01-13 12:10:14.500 +1100", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 12:10:14.000000000 +1100");
		Assert.assertEquals("2011-01-13 12:10:14.000 +1100", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 12:10:14.000500000 +1100");
		Assert.assertEquals("2011-01-13 12:10:14.001 +1100", rt[0]);
		Assert.assertEquals(false, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 12:10:14.999999999 +1100");
		Assert.assertEquals("2011-01-13 12:10:14.000 +1100", rt[0]);
		Assert.assertEquals(true, rt[1]);

		rt = DateUtil
				.nanoDateStringToMilliDateString("2011-01-13 23:59:59.999999999 +1100");
		Assert.assertEquals("2011-01-13 23:59:59.000 +1100", rt[0]);
		Assert.assertEquals(true, rt[1]);
	}

	@Test(expected = InvalidParameterException.class)
	public void nanoDateStringToMilliDateStringInvalid() {
		DateUtil.nanoDateStringToMilliDateString("2011-01-13 23:59:59.12345678912345 +1100");
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
		Calendar calendar = DateUtil.fromISO8601("2011-11-18T15:38:28+09:00");
		Assert.assertEquals("2011-11-18T15:38:28+09:00",
				DateUtil.toISO8601(calendar));
	}
}
