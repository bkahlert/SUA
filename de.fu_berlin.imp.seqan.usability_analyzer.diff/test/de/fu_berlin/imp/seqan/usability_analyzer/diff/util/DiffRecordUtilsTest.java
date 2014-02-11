package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffTest;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.util.DiffRecordUtils.DiffRecordDescriptor;

public class DiffRecordUtilsTest {

	private static final String root = "/"
			+ DiffRecordUtilsTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	/**
	 * Returns an around 300MB diff file that includes passages hard to parse
	 * (e.g. lines starting with "+++").
	 * 
	 * @return
	 */
	private static File getDiffFile() {
		return FileUtils
				.getFile(root
						+ "/diff/axokgsh9f9xk33dk/axokgsh9f9xk33dk_r00000001_2012-04-11T11-12-55+0200.diff");
	}

	private static IData getDiff() {
		IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
				FileUtils.getFile(root));
		IData data = new FileData(baseDataContainer, baseDataContainer,
				getDiffFile());
		return data;
	}

	@Test
	public void loadTest() {
		IData data = getDiff();
		List<DiffRecordDescriptor> descriptors = DiffRecordUtils
				.readDescriptors(data, new NullProgressMonitor());
		for (DiffRecordDescriptor descriptor : descriptors) {
			// 2012-10-04 +++ ver was detected as the new meta line
			// fixed, we don't want this to occur a second time
			assertFalse(descriptor.metaNewLine.contains("+++ ver"));
			assertTrue(descriptor.contentStart < descriptor.contentEnd);
		}

		// check num descriptors
		int numRecords = 0;
		for (String line : data) {
			if (line.startsWith("diff -u -r -N -x")) {
				++numRecords;
			}
		}
		assertEquals(numRecords, descriptors.size());
	}

	@Test
	public void testGetRecordsFromSegments() throws IllegalArgumentException,
			URISyntaxException {
		IDiff diff = DiffTest
				.getDiffFile(
						"o6lmo5tpxvn3b6fg/o6lmo5tpxvn3b6fg_r00000048_2011-09-13T12-11-02.diff",
						IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"), 48l,
						new TimeZoneDateRange(new TimeZoneDate(
								"2011-09-13T12:11:02+02:00"), null));
		IDiffRecord diffRecord = diff.getDiffFileRecords().get(2);
		IDiffRecordSegment diffRecordSegment = new DiffRecordSegment(
				diffRecord, 25, 134);
		System.err.println(diffRecordSegment.getUri());

		// sanity check
		assertEquals(diffRecord, diffRecordSegment.getDiffFileRecord());
		assertEquals(diffRecord.getUri(), diffRecordSegment.getDiffFileRecord()
				.getUri());

		// check functionality
		assertEquals(Arrays.asList(diffRecord.getUri()),
				DiffRecordUtils
						.getRecordsFromSegments(new URI[] { diffRecordSegment
								.getUri() }));

		// check empty list
		assertEquals(new ArrayList<URI>(),
				DiffRecordUtils.getRecordsFromSegments(new URI[] {}));

		// check just DiffRecord instead of segment
		assertEquals(Arrays.asList(diffRecord.getUri()),
				DiffRecordUtils.getRecordsFromSegments(new URI[] { diffRecord
						.getUri() }));

		// check non segment URI
		assertEquals(new ArrayList<URI>(),
				DiffRecordUtils.getRecordsFromSegments(new URI[] { new URI(
						"sua://abc") }));
	}
}
