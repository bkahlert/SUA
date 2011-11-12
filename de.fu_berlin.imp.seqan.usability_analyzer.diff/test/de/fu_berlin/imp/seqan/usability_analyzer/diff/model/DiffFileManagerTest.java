package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import junit.framework.Assert;

import org.junit.Test;

import util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class DiffFileManagerTest {
	@Test
	public void testDiffFileManager() throws Exception {
		DiffFileManager diffFileManager = new DiffFileManager(
				FileUtils.getFile("data"), FileUtils.getFile("trunk"));

		ID id = new ID("amudto8y1mzxaebv");
		DiffFileList diffFiles = diffFileManager.getDiffFiles(id);
		Assert.assertEquals(6, diffFiles.size());

		for (int i = 0; i < diffFiles.size(); i++) {
			DiffFile diffFile = diffFiles.get(i);
			Assert.assertEquals(i, Integer.parseInt(diffFile.getRevision()));
		}

		DiffFileRecordHistory diffFileRecordHistory = diffFiles
				.getHistory("sandbox/mordor/apps/exastellar/exastellar.cpp");
		Assert.assertEquals(3, diffFileRecordHistory.size());

		for (DiffFileRecord diffFileRecord : diffFileRecordHistory) {
			System.err.println(diffFileRecord.getSource());
		}

		// for (int i = 0; i < diffFileRecords.size(); i++) {
		// DiffFileRecord diffFileRecord = diffFileRecords.get(i);
		// Assert.assertEquals(numContentLines[i], diffFileRecord.getContent()
		// .split("\n").length);
		// }
	}
}
