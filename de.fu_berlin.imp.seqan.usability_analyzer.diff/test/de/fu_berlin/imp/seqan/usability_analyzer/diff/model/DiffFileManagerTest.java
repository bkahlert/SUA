package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffUtils;

public class DiffFileManagerTest {

	private static final String logDirectory = "data";
	private static final String trunkDirectory = "trunk";

	private static ID id = new ID("amudto8y1mzxaebv");

	private static DiffFileManager getDiffFileManager()
			throws DataSourceInvalidException, URISyntaxException {
		return new DiffFileManager(FileUtils.getFile(logDirectory),
				FileUtils.getFile(trunkDirectory));
	}

	private DiffFileRecordHistory getDiffFileRecordHistory()
			throws DataSourceInvalidException, URISyntaxException {
		return getDiffFileManager().getDiffFiles(id).getHistory(
				"sandbox/mordor/apps/exastellar/exastellar.cpp");
	}

	@Before
	public void clearCache() throws URISyntaxException, IOException {
		File sourcesDirectory = new DiffUtils(FileUtils.getFile(logDirectory))
				.getSourceRoot();
		org.apache.commons.io.FileUtils.cleanDirectory(sourcesDirectory);
	}

	@Test
	public void testGeneral() throws Exception {
		DiffFileManager diffFileManager = getDiffFileManager();

		DiffFileList diffFiles = diffFileManager.getDiffFiles(id);
		Assert.assertEquals(6, diffFiles.size());

		for (int i = 0; i < diffFiles.size(); i++) {
			DiffFile diffFile = diffFiles.get(i);
			Assert.assertEquals(i, Integer.parseInt(diffFile.getRevision()));
		}
	}

	@Test
	public void testPredecessorSuccessor() throws Exception {
		DiffFileRecordHistory diffFileRecordHistory = getDiffFileRecordHistory();
		Assert.assertEquals(3, diffFileRecordHistory.size());

		DiffFileRecord r0 = diffFileRecordHistory.get(0);
		DiffFileRecord r1 = diffFileRecordHistory.get(1);
		DiffFileRecord r2 = diffFileRecordHistory.get(2);

		Assert.assertEquals(r1, r0.getSuccessor());
		Assert.assertEquals(r2, r1.getSuccessor());
		Assert.assertNull(r2.getSuccessor());

		Assert.assertNull(r0.getPredecessor());
		Assert.assertEquals(r0, r1.getPredecessor());
		Assert.assertEquals(r1, r2.getPredecessor());
	}

	@Test
	public void testSources() throws Exception {
		DiffFileRecordHistory diffFileRecordHistory = getDiffFileRecordHistory();
		Assert.assertEquals(3, diffFileRecordHistory.size());

		for (int i = 0, j = diffFileRecordHistory.size(); i < j; i++) {
			DiffFileRecord diffFileRecord = diffFileRecordHistory.get(i);
			File expectedFile = FileUtils.getFile(trunkDirectory
					+ "/sandbox/mordor/apps/exastellar/exastellar.r" + i
					+ ".cpp");
			String expectedSource = org.apache.commons.io.FileUtils
					.readFileToString(expectedFile);
			String actualSource = diffFileRecord.getSource();
			String[] a = expectedSource.split("\n");
			String[] b = actualSource.split("\n");
			Assert.assertEquals(a.length, b.length);
			for (int k = 0; k < a.length; k++) {
				Assert.assertEquals(a[i], b[i]);
			}
			Assert.assertEquals(expectedSource, actualSource);
		}
	}
}
