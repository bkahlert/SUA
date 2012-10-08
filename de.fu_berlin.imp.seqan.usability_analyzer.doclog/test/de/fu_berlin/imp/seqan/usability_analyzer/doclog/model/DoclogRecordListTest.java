package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimeZone;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.TempBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class DoclogRecordListTest {

	private String doclogFileContent = ""
			+ "2011-09-13T12-43-28	ready	http://www.seqan.de/	141.14.249.178	-	0	0	1137	520\n"
			+ "2011-09-13T12-43-36	unload	http://www.seqan.de/	141.14.249.178	-	0	0	1137	520\n"
			+ "2011-09-13T12-43-36	ready	http://www.seqan.de/dddoc/html/index.html	141.14.249.178	-	0	0	1137	520\n"
			+ "2011-09-13T12-43-36	ready	http://www.seqan.de/dddoc/html/INDEX_Page.html	141.14.249.178	-	0	0	194	1148\n"
			+ "2011-09-13T12-43-43	scroll	http://www.seqan.de/dddoc/html/index.html	141.14.249.178	-	0	439	1137	520\n"
			+ "2011-09-13T12-43-44	link-http://trac.mi.fu-berlin.de/seqan/wiki	http://www.seqan.de/dddoc/html/index.html	141.14.249.178	-	0	439	1137	520\n"
			+ "2011-09-13T12-43-45	unload	http://www.seqan.de/dddoc/html/index.html	141.14.249.178	-	0	439	1137	520\n"
			+ "2011-09-13T12-43-45	unload	http://www.seqan.de/dddoc/html/INDEX_Page.html	141.14.249.178	-	0	0	194	1148\n"
			+ "2011-09-10T10-20-59	ready	http://www.seqan.de/	85.179.79.188	-	0	0	1263	607\n"
			+ "2011-09-10T11-31-05	ready	http://www.seqan.de/	85.179.79.188	-	0	0	1263	578\n"
			+ "2011-09-10T11-31-11	unload	http://www.seqan.de/	85.179.79.188	-	0	0	1263	578\n"
			+ "2011-09-10T11-31-29	ready	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	0	1263	578\n"
			+ "2011-09-10T11-31-44	scroll	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	703	1263	578\n"
			+ "2011-09-10T11-32-04	scroll	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	5030	1263	578\n"
			+ "2011-09-10T11-32-14	scroll	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	5086	1263	578\n"
			+ "2011-09-10T12-45-47	ready	http://trac.mi.fu-berlin.de/seqan/attachment/wiki/Tutorial/GettingStarted/my_sandbox.zip	85.179.79.188	-	0	0	1280	607\n"
			+ "2011-09-10T12-46-06	unload	http://trac.mi.fu-berlin.de/seqan/attachment/wiki/Tutorial/GettingStarted/my_sandbox.zip	85.179.79.188	-	0	0	1280	607\n"
			+ "2011-09-10T12-46-07	ready	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	7604	1263	607\n"
			+ "2011-09-10T12-46-07	unload	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	7604	1263	607\n"
			+ "2020-10-20T15-33-05+0800	ready	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/GettingStarted	85.179.79.188	-	0	7604	1263	607\n";

	private int doclogNumDoclogRecords;

	public DoclogRecordListTest() {
		this.doclogNumDoclogRecords = doclogFileContent.split("\n").length;
	}

	public Doclog getDoclogFile() throws IOException {
		File file = File.createTempFile("test", "_doclog.txt");
		file.deleteOnExit();
		BufferedWriter out = null;
		try {
			FileWriter fstream = new FileWriter(file);
			out = new BufferedWriter(fstream);
			out.write(doclogFileContent);
		} finally {
			if (out != null)
				out.close();
		}
		IBaseDataContainer baseDataContainer = new TempBaseDataContainer();
		FileData dataResource = new FileData(baseDataContainer,
				baseDataContainer, file);
		return new Doclog(dataResource, new ID("fakeID"),
				Doclog.getDateRange(dataResource),
				Doclog.getToken(dataResource), null);
	}

	@Test
	public void sizeTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		Assert.assertEquals(doclogNumDoclogRecords, doclog.getDoclogRecords()
				.size());
	}

	@Test
	public void earliestLatestDateTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		DoclogRecord oldestRecord = doclog.getDoclogRecords().get(0);
		DoclogRecord youngestRecord = doclog.getDoclogRecords().get(
				doclogNumDoclogRecords - 1);
		Assert.assertEquals(
				new TimeZoneDate(DateUtil.getDate(2011, 8, 10, 8, 20, 59),
						TimeZone.getDefault()), oldestRecord.getDate());
		Assert.assertEquals(new TimeZoneDate("2020-10-20T15:33:05+08:00"),
				youngestRecord.getDate());
	}

	/*
	 * Predecessor
	 */
	@Test
	public void predecessorTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		DoclogRecordList doclogRecords = doclog.getDoclogRecords();

		Assert.assertNull(doclogRecords.getPredecessor(doclogRecords.get(0)));

		predecessorTestCheck(doclogRecords, 0, 1, "http://www.seqan.de/");

		predecessorTestCheck(doclogRecords, 1, 2, "http://www.seqan.de/");

		predecessorTestCheck(
				doclogRecords,
				7,
				8,
				"http://trac.mi.fu-berlin.de/seqan/attachment/wiki/Tutorial/GettingStarted/my_sandbox.zip");

		predecessorTestCheck(doclogRecords, 16, 17,
				"http://www.seqan.de/dddoc/html/index.html");
	}

	private static void predecessorTestCheck(DoclogRecordList doclogRecords,
			int expectedPredecessorIdx, int doclogRecordIdx, String url) {

		DoclogRecord expectedPredecessor = doclogRecords
				.get(expectedPredecessorIdx);

		DoclogRecord doclogRecord = doclogRecords.get(doclogRecordIdx);
		DoclogRecord predecessor = doclogRecords.getPredecessor(doclogRecord);

		Assert.assertEquals("DoclogRecords\n" + expectedPredecessor + " and\n"
				+ predecessor + "\nare not equal", expectedPredecessor,
				predecessor);
		Assert.assertEquals("URL of DoclogRecord " + expectedPredecessor
				+ " differs", url, expectedPredecessor.getUrl());
		Assert.assertEquals("URL of DoclogRecord " + predecessor + " differs",
				url, predecessor.getUrl());

		long expectedTimeDifference = doclogRecord.getDate().getTime()
				- expectedPredecessor.getDate().getTime();
		long actualTimeDifference = doclogRecord.getDate().getTime()
				- predecessor.getDate().getTime();
		Assert.assertEquals("DoclogRecords\n" + doclogRecord + " and\n"
				+ predecessor + "\nhave wrong time difference",
				expectedTimeDifference, actualTimeDifference);
	}

	@Test
	public void predecessorIsAlwaysInPastTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		DoclogRecordList doclogRecords = doclog.getDoclogRecords();

		boolean skip = true;
		for (DoclogRecord doclogRecord : doclogRecords) {
			DoclogRecord predecessor = doclogRecords
					.getPredecessor(doclogRecord);
			if (skip) {
				Assert.assertNull(predecessor);
				skip = false;
			} else {
				Assert.assertThat(predecessor.getDate(),
						Matchers.lessThanOrEqualTo(doclogRecord.getDate()));
			}
		}
	}

	/*
	 * Successor
	 */
	@Test
	public void successorTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		DoclogRecordList doclogRecords = doclog.getDoclogRecords();

		Assert.assertNull(doclogRecords.getSuccessor(doclogRecords
				.get(doclogRecords.size() - 1)));

		successorTestCheck(doclogRecords, 1, 0, "http://www.seqan.de/");

		successorTestCheck(doclogRecords, 2, 1, "http://www.seqan.de/");

		successorTestCheck(
				doclogRecords,
				8,
				7,
				"http://trac.mi.fu-berlin.de/seqan/attachment/wiki/Tutorial/GettingStarted/my_sandbox.zip");

		successorTestCheck(doclogRecords, 17, 16,
				"http://www.seqan.de/dddoc/html/index.html");
	}

	private static void successorTestCheck(DoclogRecordList doclogRecords,
			int expectedSuccessorIdx, int doclogRecordIdx, String url) {

		DoclogRecord expectedSuccessor = doclogRecords
				.get(expectedSuccessorIdx);

		DoclogRecord doclogRecord = doclogRecords.get(doclogRecordIdx);
		DoclogRecord successor = doclogRecords.getSuccessor(doclogRecord);

		Assert.assertEquals("DoclogRecords\n" + expectedSuccessor + " and\n"
				+ successor + "\nare not equal", expectedSuccessor, successor);
		Assert.assertEquals("URL of DoclogRecord " + expectedSuccessor
				+ " differs", url, expectedSuccessor.getUrl());
		Assert.assertEquals("URL of DoclogRecord " + successor + " differs",
				url, successor.getUrl());

		long expectedTimeDifference = expectedSuccessor.getDate().getTime()
				- doclogRecord.getDate().getTime() - 1;
		long actualTimeDifference = successor.getDate().getTime()
				- doclogRecord.getDate().getTime() - 1;
		Assert.assertEquals("DoclogRecords\n" + doclogRecord + " and\n"
				+ successor + "\nhave wrong time difference",
				expectedTimeDifference, actualTimeDifference);
		Assert.assertEquals("DoclogRecords\n" + doclogRecord + " and\n"
				+ successor + "\nhave wrong time difference", new Long(
				expectedTimeDifference), doclogRecord.getDateRange()
				.getDifference());
	}

	@Test
	public void successorIsAlwaysInFutureTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		DoclogRecordList doclogRecords = doclog.getDoclogRecords();

		for (int i = 0; i < doclogRecords.size(); i++) {
			DoclogRecord doclogRecord = doclogRecords.get(i);
			DoclogRecord successor = doclogRecords.getSuccessor(doclogRecord);
			if (i == doclogRecords.size() - 1) {
				Assert.assertNull(successor);
			} else {
				Assert.assertThat(successor.getDate(),
						Matchers.greaterThanOrEqualTo(doclogRecord.getDate()));
			}
		}
	}

	/*
	 * Predecessor & Successor
	 */
	@Test
	public void predecessorSuccessorTest() throws Exception {
		Doclog doclog = this.getDoclogFile();
		DoclogRecordList doclogRecords = doclog.getDoclogRecords();

		for (int i = 0; i < doclogRecords.size(); i++) {
			DoclogRecord doclogRecord = doclogRecords.get(i);
			DoclogRecord predecessor = doclogRecords
					.getPredecessor(doclogRecord);
			DoclogRecord successor = doclogRecords.getSuccessor(doclogRecord);

			if (i == 0) {
				Assert.assertNull(predecessor);
				Assert.assertNotNull(successor);
			} else if (i == doclogRecords.size() - 1) {
				Assert.assertNotNull(predecessor);
				Assert.assertNull(successor);
			} else {
				Assert.assertEquals(doclogRecord,
						doclogRecords.getPredecessor(successor));
				Assert.assertEquals(doclogRecord,
						doclogRecords.getSuccessor(predecessor));
			}
		}
	}
}
