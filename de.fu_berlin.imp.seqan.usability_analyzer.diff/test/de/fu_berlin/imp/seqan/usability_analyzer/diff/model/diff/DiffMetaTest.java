package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.diff;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecordMeta;

public class DiffMetaTest {
	private static final String fromFileLine = "--- ./misc/seqan_instrumentation/last_revision_copy/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log	2011-09-13 12:35:14.578125000 +0300";
	private static final String toFileLine = "+++ bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log	2011-09-13 12:36:02.125000000 +0230";

	@Test
	public void test() {
		DiffRecordMeta meta = new DiffRecordMeta(fromFileLine,
				toFileLine);
		Assert.assertEquals(
				"misc/seqan_instrumentation/last_revision_copy/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
				meta.getFromFileName());
		Assert.assertEquals("bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
				meta.getToFileName());

		Assert.assertEquals(new TimeZoneDate(
				"2011-09-13T12:35:14.578125000+03:00"), meta.getFromFileDate());
		Assert.assertEquals(new TimeZoneDate(
				"2011-09-13T12:36:02.125000000+02:30"), meta.getToFileDate());

		Assert.assertEquals(new Long(1847547), meta.getDateRange()
				.getDifference());
	}
}
