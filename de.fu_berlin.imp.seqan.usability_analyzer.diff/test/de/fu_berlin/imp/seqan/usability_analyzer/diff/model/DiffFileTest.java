package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class DiffFileTest {

	private static final String root = "/"
			+ DiffFileManagerTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	private static DiffFile getDiffFile(String diffFileName)
			throws URISyntaxException {
		File file = FileUtils.getFile(root + "/data/" + diffFileName);
		return new DiffFile(null, file.getAbsolutePath());
	}

	@Test
	public void testDiffFileRecords() throws URISyntaxException {
		testDiffFileRecordsCountRun(
				getDiffFile("o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"),
				new int[] { 12, 12, 88, 12, 13, 12 }, new Long[] { 47547l,
						47556l, 189485l, 47610l, 47921l, 47937l });

		testDiffFileRecordsCountRun(
				getDiffFile("o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02+0200.diff"),
				new int[] { 12, 12, 88, 12, 13, 12 }, new Long[] { 47547l,
						47556l, 189485l, 47610l, 47921l, 47937l });

		testDiffFileRecordsCountRun(
				getDiffFile("5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43.diff"),
				new int[] { 7, 6, 12, 22, 6, 22, 64, 129, 12, 7, 7 },
				new Long[] { null, null, null, null, null, null, null, null,
						null, null, null });

		testDiffFileRecordsCountRun(
				getDiffFile("5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43-0530.diff"),
				new int[] { 7, 6, 12, 22, 6, 22, 64, 129, 12, 7, 7 },
				new Long[] { null, null, null, null, null, null, null, null,
						null, null, null });
	}

	private void testDiffFileRecordsCountRun(DiffFile diffFile,
			int[] numContentLines, Long[] timeDifferences) {
		DiffFileRecordList diffFileRecords = diffFile.getDiffFileRecords();
		Assert.assertEquals(numContentLines.length, diffFileRecords.size());
		for (int i = 0; i < diffFileRecords.size(); i++) {
			DiffFileRecord diffFileRecord = diffFileRecords.get(i);

			Assert.assertEquals(diffFileRecord.getFilename(),
					numContentLines[i],
					diffFileRecord.getPatchContent().split("\n").length);
			Assert.assertEquals(diffFileRecord.getFilename(),
					timeDifferences[i], diffFileRecord.getDateRange()
							.getDifference());
		}
	}

}
