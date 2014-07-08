package de.fu_berlin.imp.apiua.survey.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;
import de.fu_berlin.imp.apiua.core.model.DataReader;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileData;
import de.fu_berlin.imp.apiua.core.util.FileUtils;
import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyManager;
import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyRecord;
import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyRecordList;

public class CSVSurveyRecordManagerTest {

	private static final String root = "/"
			+ CSVSurveyRecordManagerTest.class.getPackage().getName()
					.replace('.', '/') + "/..";
	private static final IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
			FileUtils.getFile(root));
	private static final String dataDirectory = root + "/data";

	private IData getNumbersCsvExport() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(CSVSurveyRecordManagerTest.class,
						dataDirectory + "/workshop12-Data.csv"));
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
		csvReader.close();
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
		csvReader.close();
	}

	@Test
	public void testSurveyRecordManager() throws URISyntaxException {
		IData survey12 = getNumbersCsvExport();

		CSVSurveyManager cSVSurveyManager = new CSVSurveyManager(survey12);

		CSVSurveyRecordList records = cSVSurveyManager.getSurveyRecords();
		assertNull(records);

		cSVSurveyManager.scanRecords(null);
		records = cSVSurveyManager.getSurveyRecords();
		assertNotNull(records);

		for (CSVSurveyRecord record : records) {
			assertNotNull(record.getField("id"));
		}
	}
}
