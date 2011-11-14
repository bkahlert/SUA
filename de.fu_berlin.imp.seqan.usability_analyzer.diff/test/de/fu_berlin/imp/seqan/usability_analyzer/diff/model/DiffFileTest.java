package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import util.FileUtils;

public class DiffFileTest {

	private static DiffFile getDiffFile(String diffFileName)
			throws URISyntaxException {
		File file = FileUtils.getFile("data/" + diffFileName);
		return new DiffFile(null, file.getAbsolutePath());
	}

	@Test
	public void testDiffFileRecordsCount() throws URISyntaxException {
		DiffFile diffFile = getDiffFile("o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff");
		int[] numContentLines = new int[] { 12, 12, 88, 12, 13, 12 };

		DiffFileRecordList diffFileRecords = diffFile.getDiffFileRecords();
		Assert.assertEquals(6, diffFileRecords.size());
		for (int i = 0; i < diffFileRecords.size(); i++) {
			DiffFileRecord diffFileRecord = diffFileRecords.get(i);
			Assert.assertEquals(numContentLines[i], diffFileRecord.getPatchContent()
					.split("\n").length);
		}
	}

}
