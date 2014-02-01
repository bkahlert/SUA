package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffDataUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ISourceStore;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.SourceCache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.Trunk;

public class DiffFileTest {

	private static final String root = "/"
			+ DiffFileDirectoryTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	public static IDiff getDiffFile(String diffFileName, IIdentifier id,
			long revision, TimeZoneDateRange dateRange)
			throws URISyntaxException {

		FileBaseDataContainer baseDataContainer = new FileBaseDataContainer(
				FileUtils.getFile(root));

		File diffFile = FileUtils.getFile(root + "/diff/" + diffFileName);
		FileData diffData = new FileData(baseDataContainer, baseDataContainer,
				diffFile);

		ITrunk trunk = new Trunk(new FileDataContainer(baseDataContainer,
				baseDataContainer, FileUtils.getFile(root)));
		ISourceStore sourceCache = new SourceCache(baseDataContainer, "sources");

		return new Diff(diffData, null, trunk, sourceCache,
				new NullProgressMonitor());
	}

	@Test
	public void testDiffFileStatics() throws URISyntaxException {
		FileBaseDataContainer baseContainer = new FileBaseDataContainer(
				FileUtils.getFile(root));
		Assert.assertEquals(
				IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"),
				DiffDataUtils
						.getId(new FileData(
								baseContainer,
								baseContainer,
								new File(
										"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));
		Assert.assertEquals(
				IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"),
				DiffDataUtils
						.getId(new FileData(
								baseContainer,
								baseContainer,
								new File(
										"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));

		Assert.assertEquals(
				"00000048",
				DiffDataUtils
						.getRevision(new FileData(
								baseContainer,
								baseContainer,
								new File(
										"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));
		Assert.assertEquals(
				"00000048",
				DiffDataUtils
						.getRevision(new FileData(
								baseContainer,
								baseContainer,
								new File(
										"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff"))));

		Assert.assertEquals(
				new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				DiffDataUtils
						.getDate(
								new FileData(
										baseContainer,
										baseContainer,
										new File(
												"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")),
								null));
		Assert.assertEquals(
				new TimeZoneDate("2011-09-13T12:11:02+02:00"),
				DiffDataUtils
						.getDate(
								new FileData(
										baseContainer,
										baseContainer,
										new File(
												"some/dir/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff")),
								null));
	}

	@Test
	public void testGetContent() throws URISyntaxException {
		IDiff smallDiffFile = getDiffFile(
				"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff",
				IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"), 48l,
				new TimeZoneDateRange(new TimeZoneDate(
						"2011-09-13T12:11:02+02:00"), null));

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
		this.testDiffFileRecordsCountRun(
				getDiffFile(
						"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff",
						IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"), 48l,
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

		this.testDiffFileRecordsCountRun(
				getDiffFile(
						"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02+0200.diff",
						IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"), 48l,
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

		this.testDiffFileRecordsCountRun(
				getDiffFile(
						"5lpcjqhy0b9yfech/5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43-0530.diff",
						IdentifierFactory.createFrom("5lpcjqhy0b9yfech"), 5l,
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

		this.testDiffFileRecordsCountRun(
				getDiffFile(
						"5lpcjqhy0b9yfech/5lpcjqhy0b9yfech_r00000005_2011-09-13T10-17-43-0530.diff",
						IdentifierFactory.createFrom("5lpcjqhy0b9yfech"), 5l,
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

	@SuppressWarnings("deprecation")
	private void testDiffFileRecordsCountRun(IDiff diff, int[] numContentLines,
			Long[] timeDifferences, String[] sourceFiles) throws IOException {
		DiffRecords diffFileRecords = diff.getDiffFileRecords();
		Assert.assertEquals(numContentLines.length, diffFileRecords.size());
		for (int i = 0; i < diffFileRecords.size(); i++) {
			IDiffRecord diffRecord = diffFileRecords.get(i);

			// patch size
			Assert.assertEquals(numContentLines[i], diffRecord.getPatchLines()
					.size());

			// time difference
			Assert.assertEquals(timeDifferences[i], diffRecord.getDateRange()
					.getDifference());

			// cached source file location
			Assert.assertEquals(
					sourceFiles[i],
					diffRecord.getIdentifier()
							+ "/"
							+ new Long(diffRecord.getDiffFile().getRevision())
									.toString() + "/"
							+ diffRecord.getFilename());
		}
	}
}
