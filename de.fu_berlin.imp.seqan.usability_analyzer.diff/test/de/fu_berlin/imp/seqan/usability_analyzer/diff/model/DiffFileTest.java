package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;

public class DiffFileTest {

	private static final String root = "/"
			+ DiffFileDirectoryTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	private static DiffDataResource getDiffFile(String diffFileName, ID id,
			String revision, TimeZoneDateRange dateRange)
			throws URISyntaxException {

		FileBaseDataContainer baseDataContainer = new FileBaseDataContainer(
				FileUtils.getFile(root));

		File diffFile = FileUtils.getFile(root + "/diff/" + diffFileName);
		FileData diffData = new FileData(baseDataContainer, diffFile);

		SourceOrigin sourceOrigin = new SourceOrigin(new FileDataContainer(
				baseDataContainer, FileUtils.getFile(root)));
		SourceCache sourceCache = new SourceCache(baseDataContainer, "sources");

		return new DiffDataResource(diffData, null, id, revision, dateRange,
				sourceOrigin, sourceCache, new NullProgressMonitor());
	}

	@Test
	public void testDiffFileStatics() throws URISyntaxException {
		Assert.assertEquals(
				new ID("o6lmo5tpxvn3b6fg"),
				DiffDataResource.getId(new FileData(
						new FileBaseDataContainer(FileUtils.getFile(root)),
						new File(
								"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));
		Assert.assertEquals(
				new ID("o6lmo5tpxvn3b6fg"),
				DiffDataResource.getId(new FileData(
						new FileBaseDataContainer(FileUtils.getFile(root)),
						new File(
								"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));

		Assert.assertEquals(
				"00000048",
				DiffDataResource.getRevision(new FileData(
						new FileBaseDataContainer(FileUtils.getFile(root)),
						new File(
								"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));
		Assert.assertEquals(
				"00000048",
				DiffDataResource.getRevision(new FileData(
						new FileBaseDataContainer(FileUtils.getFile(root)),
						new File(
								"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));

		Assert.assertEquals(
				new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				DiffDataResource.getDate(new FileData(
						new FileBaseDataContainer(FileUtils.getFile(root)),
						new File(
								"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));
		Assert.assertEquals(
				new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				DiffDataResource.getDate(new FileData(
						new FileBaseDataContainer(FileUtils.getFile(root)),
						new File(
								"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));
	}

	@Test
	public void testGetContent() throws URISyntaxException {
		DiffDataResource smallDiffFile = getDiffFile(
				"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff",
				new ID("o6lmo5tpxvn3b6fg"), "00000048", new TimeZoneDateRange(
						new TimeZoneDate("2011-09-13T12:11:02+02:00"), null));

		String firstLine = "--- ./misc/seqan_instrumentation/last_revision_copy/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log	2011-09-13 12:10:14.578125000 +0200";
		Assert.assertEquals(firstLine,
				smallDiffFile.getContent(997l, 997l + firstLine.length())
						.get(0));

		String lastLine = "+Time Elapsed 00:00:00.28";
		Assert.assertEquals(
				lastLine,
				smallDiffFile.getContent(1681l - lastLine.length(), 1681l).get(
						0));
	}

	@Test
	public void testDiffFileRecords() throws URISyntaxException,
			IllegalArgumentException, IOException {
		testDiffFileRecordsCountRun(
				getDiffFile(
						"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff",
						new ID("o6lmo5tpxvn3b6fg"), "00000048",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T12:11:02+02:00"), null)),
				new int[] { 14, 14, 90, 14, 15, 14 },
				new Long[] { 47547l, 47656l, 189485l, 47610l, 47921l, 47937l },
				new String[] {
						"o6lmo5tpxvn3b6fg/48/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
						"o6lmo5tpxvn3b6fg/48/bin/extras/Win32/Debug/SeqAnExtras/SeqAnExtras.log",
						"o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/apps/my_app/my_app.dir/Debug/my_app.log",
						"o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/Win32/Debug/SeqAnSandboxMy_sandbox/SeqAnSandboxMy_sandbox.log",
						"o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/seqan_instrumentation_build/seqan_instrumentation_build.log",
						"o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/ZERO_CHECK/ZERO_CHECK.log" });

		testDiffFileRecordsCountRun(
				getDiffFile(
						"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02+0200.diff",
						new ID("o6lmo5tpxvn3b6fg"), "00000048",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T12:11:02+02:00"), null)),
				new int[] { 14, 14, 90, 14, 15, 14 },
				new Long[] { 47547l, 47656l, 189485l, 47610l, 47921l, 47937l },
				new String[] {
						"o6lmo5tpxvn3b6fg/48/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
						"o6lmo5tpxvn3b6fg/48/bin/extras/Win32/Debug/SeqAnExtras/SeqAnExtras.log",
						"o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/apps/my_app/my_app.dir/Debug/my_app.log",
						"o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/Win32/Debug/SeqAnSandboxMy_sandbox/SeqAnSandboxMy_sandbox.log",
						"o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/seqan_instrumentation_build/seqan_instrumentation_build.log",
						"o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/ZERO_CHECK/ZERO_CHECK.log" });

		testDiffFileRecordsCountRun(
				getDiffFile(
						"5lpcjqhy0b9yfech/5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43.diff",
						new ID("5lpcjqhy0b9yfech"), "00000005",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T10:17:43+02:00"), null)),
				new int[] { 9, 8, 14, 24, 8, 24, 66, 131, 14, 9, 9 },
				new Long[] { null, null, null, null, null, null, null, null,
						null, null, null },
				new String[] {
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/first_app.cpp",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/INFO",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/INFO",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.cpp",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.h",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/demos/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/tests/CMakeLists.txt" });

		testDiffFileRecordsCountRun(
				getDiffFile(
						"5lpcjqhy0b9yfech/5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43-0530.diff",
						new ID("5lpcjqhy0b9yfech"), "00000005",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T10:17:43+02:00"), null)),
				new int[] { 9, 8, 14, 24, 8, 24, 66, 131, 14, 9, 9 },
				new Long[] { null, null, null, null, null, null, null, null,
						null, null, null },
				new String[] {
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/first_app.cpp",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/INFO",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/INFO",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.cpp",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.h",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/demos/CMakeLists.txt",
						"5lpcjqhy0b9yfech/5/sandbox/my_sandbox/tests/CMakeLists.txt" });
	}

	private void testDiffFileRecordsCountRun(DiffDataResource diffDataResource,
			int[] numContentLines, Long[] timeDifferences, String[] sourceFiles)
			throws IOException {
		DiffFileRecordList diffFileRecords = diffDataResource
				.getDiffFileRecords();
		Assert.assertEquals(numContentLines.length, diffFileRecords.size());
		for (int i = 0; i < diffFileRecords.size(); i++) {
			DiffRecord diffRecord = diffFileRecords.get(i);

			// patch size
			Assert.assertEquals(numContentLines[i], diffRecord.getPatchLines()
					.size());

			// time difference
			Assert.assertEquals(timeDifferences[i], diffRecord.getDateRange()
					.getDifference());

			// cached source file location
			Assert.assertEquals(
					sourceFiles[i],
					diffRecord.getID()
							+ "/"
							+ new Long(diffRecord.getDiffFile().getRevision())
									.toString() + "/"
							+ diffRecord.getFilename());
		}
	}
}
