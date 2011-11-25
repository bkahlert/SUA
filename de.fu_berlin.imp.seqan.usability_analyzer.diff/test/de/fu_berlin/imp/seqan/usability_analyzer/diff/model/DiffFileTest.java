package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceOrigin;

public class DiffFileTest {

	private static final String root = "/"
			+ DiffFileManagerTest.class.getPackage().getName()
					.replace('.', '/') + "/..";
	private static final String sourcesRoot = "/fake/sources";

	private static DiffFile getDiffFile(String diffFileName, ID id,
			String revision, TimeZoneDateRange dateRange)
			throws URISyntaxException {
		File file = FileUtils.getFile(root + "/data/" + diffFileName);
		return new DiffFile(file, null, id, revision, dateRange,
				new SourceOrigin(new File(root)), new SourceCache(new File(
						sourcesRoot)), new NullProgressMonitor());
	}

	@Test
	public void testDiffFileStatics() {
		Assert.assertEquals(new ID("o6lmo5tpxvn3b6fg"), DiffFile
				.getId(new File(
						"o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")));
		Assert.assertEquals(
				new ID("o6lmo5tpxvn3b6fg"),
				DiffFile.getId(new File(
						"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")));

		Assert.assertEquals("00000048", DiffFile.getRevision(new File(
				"o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")));
		Assert.assertEquals(
				"00000048",
				DiffFile.getRevision(new File(
						"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")));

		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				DiffFile.getDate(new File(
						"o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")));
		Assert.assertEquals(
				new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				DiffFile.getDate(new File(
						"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")));
	}

	@Test
	public void testDiffFileRecords() throws URISyntaxException {
		testDiffFileRecordsCountRun(
				getDiffFile(
						"o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff",
						new ID("o6lmo5tpxvn3b6fg"), "00000048",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T12:11:02+02:00"), null)),
				new int[] { 14, 14, 90, 14, 15, 14 },
				new Long[] { 47547l, 47556l, 189485l, 47610l, 47921l, 47937l },
				new String[] {
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/extras/Win32/Debug/SeqAnExtras/SeqAnExtras.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/apps/my_app/my_app.dir/Debug/my_app.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/Win32/Debug/SeqAnSandboxMy_sandbox/SeqAnSandboxMy_sandbox.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/seqan_instrumentation_build/seqan_instrumentation_build.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/ZERO_CHECK/ZERO_CHECK.log" });

		testDiffFileRecordsCountRun(
				getDiffFile(
						"o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02+0200.diff",
						new ID("o6lmo5tpxvn3b6fg"), "00000048",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T12:11:02+02:00"), null)),
				new int[] { 14, 14, 90, 14, 15, 14 },
				new Long[] { 47547l, 47556l, 189485l, 47610l, 47921l, 47937l },
				new String[] {
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/core/Win32/Debug/SeqAnCore/SeqAnCore.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/extras/Win32/Debug/SeqAnExtras/SeqAnExtras.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/apps/my_app/my_app.dir/Debug/my_app.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/sandbox/my_sandbox/Win32/Debug/SeqAnSandboxMy_sandbox/SeqAnSandboxMy_sandbox.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/seqan_instrumentation_build/seqan_instrumentation_build.log",
						"/fake/sources/o6lmo5tpxvn3b6fg/48/bin/Win32/Debug/ZERO_CHECK/ZERO_CHECK.log" });

		testDiffFileRecordsCountRun(
				getDiffFile(
						"5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43.diff",
						new ID("5lpcjqhy0b9yfech"), "00000005",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T10:17:43+02:00"), null)),
				new int[] { 9, 8, 14, 24, 8, 24, 66, 131, 14, 9, 9 },
				new Long[] { null, null, null, null, null, null, null, null,
						null, null, null },
				new String[] {
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/first_app.cpp",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/INFO",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/INFO",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.cpp",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.h",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/demos/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/tests/CMakeLists.txt" });

		testDiffFileRecordsCountRun(
				getDiffFile(
						"5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43-0530.diff",
						new ID("5lpcjqhy0b9yfech"), "00000005",
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T10:17:43+02:00"), null)),
				new int[] { 9, 8, 14, 24, 8, 24, 66, 131, 14, 9, 9 },
				new Long[] { null, null, null, null, null, null, null, null,
						null, null, null },
				new String[] {
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/first_app.cpp",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/first_app/INFO",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/INFO",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.cpp",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/apps/my_app/my_app.h",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/demos/CMakeLists.txt",
						"/fake/sources/5lpcjqhy0b9yfech/5/sandbox/my_sandbox/tests/CMakeLists.txt" });
	}

	private void testDiffFileRecordsCountRun(DiffFile diffFile,
			int[] numContentLines, Long[] timeDifferences, String[] sourceFiles) {
		DiffFileRecordList diffFileRecords = diffFile.getDiffFileRecords();
		Assert.assertEquals(numContentLines.length, diffFileRecords.size());
		for (int i = 0; i < diffFileRecords.size(); i++) {
			DiffFileRecord diffFileRecord = diffFileRecords.get(i);

			// patch size
			Assert.assertEquals(diffFileRecord.getFilename(),
					numContentLines[i], diffFileRecord.getPatchLines().size());

			// time difference
			Assert.assertEquals(diffFileRecord.getFilename(),
					timeDifferences[i], diffFileRecord.getDateRange()
							.getDifference());

			// cached source file location
			Assert.assertEquals(diffFileRecord.getFilename(), new File(
					sourceFiles[i]), diffFileRecord.getSourceFile());
		}
	}

}
