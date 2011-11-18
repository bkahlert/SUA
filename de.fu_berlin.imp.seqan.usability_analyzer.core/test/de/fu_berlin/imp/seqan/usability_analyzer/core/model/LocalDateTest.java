package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import org.junit.Assert;
import org.junit.Test;

public class LocalDateTest {

	@Test
	public void boundedRangeDateTest() {
		String pattern = "yyyy-MM-dd HH:mm:ss Z";

		Assert.assertEquals("2011-11-18 15:38:28 +0900", new LocalDate(
				"2011-11-18T15:38:28+09:00").format(pattern));
		Assert.assertEquals("1984-05-15 14:30:00 +0900", new LocalDate(
				"1984-05-15T14:30:00+09:00").format(pattern));
	}

}
