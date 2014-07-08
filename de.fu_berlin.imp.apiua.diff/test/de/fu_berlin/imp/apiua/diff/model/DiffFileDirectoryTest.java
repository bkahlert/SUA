package de.fu_berlin.imp.apiua.diff.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.util.FileUtils;
import de.fu_berlin.imp.apiua.diff.model.DiffContainer;
import de.fu_berlin.imp.apiua.diff.model.IDiff;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;
import de.fu_berlin.imp.apiua.diff.model.IDiffs;
import de.fu_berlin.imp.apiua.diff.model.impl.DiffRecordHistory;
import de.fu_berlin.imp.apiua.diff.util.ISourceStore;
import de.fu_berlin.imp.apiua.diff.util.SourceCache;

public class DiffFileDirectoryTest {

	private static final String root = "/"
			+ DiffFileDirectoryTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	private static IIdentifier identifier = IdentifierFactory
			.createFrom("amudto8y1mzxaebv");

	private static DiffContainer getDiffFileManager() throws URISyntaxException {
		return new DiffContainer(new FileBaseDataContainer(
				FileUtils.getFile(root)));
	}

	private DiffRecordHistory getDiffFileRecordHistory()
			throws URISyntaxException {
		return getDiffFileManager().createDiffFiles(identifier,
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

		dateRange = diffFileManager.getDateRange(IdentifierFactory
				.createFrom("5lpcjqhy0b9yfech"));
		Assert.assertEquals(new TimeZoneDate("2011-09-13T10:17:43.0-05:30"),
				dateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T10:17:43.0-05:30"),
				dateRange.getEndDate());

		dateRange = diffFileManager.getDateRange(IdentifierFactory
				.createFrom("o6lmo5tpxvn3b6fg"));
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:11:02.0+02:00"),
				dateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:11:02.0+02:00"),
				dateRange.getEndDate());

		dateRange = diffFileManager.getDateRange(IdentifierFactory
				.createFrom("amudto8y1mzxaebv"));
		Assert.assertEquals(new TimeZoneDate("2011-09-13T09:41:46.0+02:00"),
				dateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T11:55:46.0+02:00"),
				dateRange.getEndDate());
	}

	@Test
	public void testGetDiffFiles() throws Exception {
		DiffContainer diffFileManager = getDiffFileManager();

		IDiffs diffFiles = diffFileManager.createDiffFiles(identifier,
				new NullProgressMonitor());
		Assert.assertEquals(6, diffFiles.length());
	}

	@Test
	public void testGetRevision() throws Exception {
		DiffContainer diffFileManager = getDiffFileManager();

		IDiffs diffFiles = diffFileManager.createDiffFiles(identifier,
				new NullProgressMonitor());

		for (int i = 0; i < diffFiles.length(); i++) {
			IDiff diff = diffFiles.get(i);
			Assert.assertEquals(StringUtils.leftPad(i + "", 8, "0"),
					diff.getRevision());
		}
	}

	@Test
	public void testPredecessorSuccessor() throws Exception {
		DiffRecordHistory diffRecordHistory = this.getDiffFileRecordHistory();
		Assert.assertEquals(3, diffRecordHistory.size());

		IDiffRecord r0 = diffRecordHistory.get(0);
		IDiffRecord r1 = diffRecordHistory.get(1);
		IDiffRecord r2 = diffRecordHistory.get(2);

		// Assert.assertEquals(r1, r0.getSuccessor());
		// Assert.assertEquals(r2, r1.getSuccessor());
		// Assert.assertNull(r2.getSuccessor());

		Assert.assertNull(r0.getPredecessor());
		Assert.assertEquals(r0, r1.getPredecessor());
		Assert.assertEquals(r1, r2.getPredecessor());
	}

	@Test
	public void testSources() throws Exception {
		DiffRecordHistory diffRecordHistory = this.getDiffFileRecordHistory();
		Assert.assertEquals(3, diffRecordHistory.size());

		for (int i = 0, j = diffRecordHistory.size(); i < j; i++) {
			IDiffRecord diffRecord = diffRecordHistory.get(i);
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
