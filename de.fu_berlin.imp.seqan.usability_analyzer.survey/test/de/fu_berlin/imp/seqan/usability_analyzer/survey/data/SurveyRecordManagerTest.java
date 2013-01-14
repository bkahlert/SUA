package de.fu_berlin.imp.seqan.usability_analyzer.survey.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataReader;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyManager;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecordList;

public class SurveyRecordManagerTest {

	private static final String root = "/"
			+ SurveyRecordManagerTest.class.getPackage().getName()
					.replace('.', '/') + "/..";
	private static final IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
			FileUtils.getFile(root));
	private static final String dataDirectory = root + "/data";

	private IData getNumbersCsvExport() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(SurveyRecordManagerTest.class, dataDirectory
						+ "/workshop12-Data.csv"));
	}

	@Test
	public void testMatrixForm() throws URISyntaxException, IOException {
		IData survey12 = getNumbersCsvExport();
		assertNotNull(survey12);

		CSVReader csvReader = new CSVReader(new DataReader(survey12), ';', '"');
		String[] nextLine;
		Integer numValues = null;
		int numLines = 0;
		while ((nextLine = csvReader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			if (numValues == null) {
				numValues = nextLine.length;
			} else {
				assertEquals((int) numValues, nextLine.length);
			}
			numLines++;
		}
		assertEquals(65, numLines);
	}

	@Test
	public void testCorrectReadOfSomeLines() throws URISyntaxException,
			IOException {
		IData survey12 = getNumbersCsvExport();
		assertNotNull(survey12);

		CSVReader csvReader = new CSVReader(new DataReader(survey12), ';', '"');
		String[] nextLine;
		int line = 0;
		while ((nextLine = csvReader.readNext()) != null) {
			if (line == 57) {
				assertEquals("phr0d30hyzmq0xri", nextLine[0]);
				assertEquals(
						"R3: Option (if not standard) to output the sequence in all alignments in case of multiple hits",
						nextLine[116]);
				assertEquals("Linux 2.6.38", nextLine[142]);
			}
			line++;
		}
	}

	@Test
	public void testSurveyRecordManager() throws URISyntaxException {
		IData survey12 = getNumbersCsvExport();

		SurveyManager surveyManager = new SurveyManager(
				survey12);

		SurveyRecordList records = surveyManager.getSurveyRecords();
		assertNull(records);

		surveyManager.scanRecords(null);
		records = surveyManager.getSurveyRecords();
		assertNotNull(records);

		for (SurveyRecord record : records) {
			assertNotNull(record.getField("id"));
		}
	}
}
