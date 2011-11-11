package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.diff;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DiffMetaTest {
	private static final String fromFileLine = "--- ./misc/seqan_instrumentation/last_revision_copy/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log	2011-09-13 12:10:14.578125000 +0200";
	private static final String toFileLine = "+++ ./bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log	2011-09-13 12:11:02.125000000 +0200";

	@Test
	public void test() {
		DiffMeta meta = new DiffMeta(fromFileLine, toFileLine);
		Assert.assertEquals(
				"./misc/seqan_instrumentation/last_revision_copy/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
				meta.getFromFileName());
		Assert.assertEquals("./bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
				meta.getToFileName());

		Assert.assertEquals(DateUtil.getDate(2011, 8, 13, 12, 10, 14, 578),
				meta.getFromFileDate());
		Assert.assertEquals(DateUtil.getDate(2011, 8, 13, 12, 11, 02, 125),
				meta.getToFileDate());

		Assert.assertEquals(new Long(47547), meta.getDateRange()
				.getDifference());
	}
}
