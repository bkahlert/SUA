package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;

public class DiffFileDirectoryTest {

	private static final String root = "/"
			+ DiffFileDirectoryTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	private static ID id = new ID("amudto8y1mzxaebv");

	private static DiffContainer getDiffFileManager()
			throws URISyntaxException {
		return new DiffContainer(new FileBaseDataContainer(
				FileUtils.getFile(root)));
	}

	private DiffRecordHistory getDiffFileRecordHistory()
			throws URISyntaxException {
		return getDiffFileManager().createDiffFiles(id,
				new NullProgressMonitor()).getHistory(
				"sandbox/mordor/apps/exastellar/exastellar.cpp");
	}

	@Before
	public void clearCache() throws URISyntaxException, IOException {
		ISourceStore sourcesDirectory = new SourceCache(
				new FileBaseDataContainer(FileUtils.getFile(root)));
		sourcesDirectory.clear();
	}

	@Test
	public void testGetDateRange() throws Exception {
		DiffContainer diffFileManager = getDiffFileManager();
		TimeZoneDateRange dateRange;

		dateRange = diffFileManager.getDateRange(new ID("5lpcjqhy0b9yfech"));
		Assert.assertEquals(new TimeZoneDate("2011-09-13T10:17:43+02:00"),
				dateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T10:17:43-05:30"),
				dateRange.getEndDate());

		dateRange = diffFileManager.getDateRange(new ID("o6lmo5tpxvn3b6fg"));
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				dateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				dateRange.getEndDate());

		dateRange = diffFileManager.getDateRange(new ID("amudto8y1mzxaebv"));
		Assert.assertEquals(new TimeZoneDate("2011-09-13T09:41:46+02:00"),
				dateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T11:55:46+02:00"),
				dateRange.getEndDate());
	}

	@Test
	public void testGetDiffFiles() throws Exception {
		DiffContainer diffFileManager = getDiffFileManager();

		DiffList diffFiles = diffFileManager.createDiffFiles(id,
				new NullProgressMonitor());
		Assert.assertEquals(6, diffFiles.size());
	}

	@Test
	public void testGetRevision() throws Exception {
		DiffContainer diffFileManager = getDiffFileManager();

		DiffList diffFiles = diffFileManager.createDiffFiles(id,
				new NullProgressMonitor());

		for (int i = 0; i < diffFiles.size(); i++) {
			IDiff diff = diffFiles.get(i);
			Assert.assertEquals(i, diff.getRevision());
		}
	}

	@Test
	public void testPredecessorSuccessor() throws Exception {
		DiffRecordHistory diffRecordHistory = getDiffFileRecordHistory();
		Assert.assertEquals(3, diffRecordHistory.size());

		DiffRecord r0 = diffRecordHistory.get(0);
		DiffRecord r1 = diffRecordHistory.get(1);
		DiffRecord r2 = diffRecordHistory.get(2);

		// Assert.assertEquals(r1, r0.getSuccessor());
		// Assert.assertEquals(r2, r1.getSuccessor());
		// Assert.assertNull(r2.getSuccessor());

		Assert.assertNull(r0.getPredecessor());
		Assert.assertEquals(r0, r1.getPredecessor());
		Assert.assertEquals(r1, r2.getPredecessor());
	}

	@Test
	public void testSources() throws Exception {
		DiffRecordHistory diffRecordHistory = getDiffFileRecordHistory();
		Assert.assertEquals(3, diffRecordHistory.size());

		for (int i = 0, j = diffRecordHistory.size(); i < j; i++) {
			DiffRecord diffRecord = diffRecordHistory.get(i);
			File expectedFile = FileUtils.getFile(root
					+ "/trunk/sandbox/mordor/apps/exastellar/exastellar.r" + i
					+ ".cpp");
			String expectedSource = org.apache.commons.io.FileUtils
					.readFileToString(expectedFile);
			String actualSource = diffRecord.getSource();
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
