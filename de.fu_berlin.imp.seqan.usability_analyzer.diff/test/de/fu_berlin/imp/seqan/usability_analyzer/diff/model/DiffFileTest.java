package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

public class DiffFileTest {
	private static final String diffFileName = "o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff";
	private static final int[] numContentLines = new int[] { 12, 12, 88, 12,
			13, 12 };

	private static DiffFile getDiffFile() throws URISyntaxException {
		URI uri = DiffFileTest.class.getResource(diffFileName).toURI();
		String path = uri.toString().substring(uri.getScheme().length() + 1);
		return new DiffFile(path);
	}

	@Test
	public void test() throws URISyntaxException {
		DiffFile diffFile = getDiffFile();

		DiffFileRecordList diffFileRecords = diffFile.getDiffFileRecords();
		Assert.assertEquals(6, diffFileRecords.size());
		for (int i = 0; i < diffFileRecords.size(); i++) {
			DiffFileRecord diffFileRecord = diffFileRecords.get(i);
			Assert.assertEquals(numContentLines[i], diffFileRecord.getContent()
					.split("\n").length);
		}
	}
}
