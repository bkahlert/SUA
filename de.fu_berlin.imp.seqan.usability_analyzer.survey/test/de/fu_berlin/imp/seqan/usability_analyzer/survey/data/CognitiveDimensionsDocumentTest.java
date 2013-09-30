package de.fu_berlin.imp.seqan.usability_analyzer.survey.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.bkahlert.devel.nebula.utils.CalendarUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocument;

public class CognitiveDimensionsDocumentTest {

	private static final String root = "/"
			+ CognitiveDimensionsDocumentTest.class.getPackage().getName()
					.replace('.', '/') + "/..";
	private static final IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
			FileUtils.getFile(root));
	private static final String dataDirectory = root + "/data";

	private IData getTestFile() throws URISyntaxException {
		return new FileData(baseDataContainer,
				baseDataContainer.getSubContainer("data"), FileUtils.getFile(
						CognitiveDimensionsDocumentTest.class, dataDirectory
								+ "/2013-09-18T17-45-54.88891500+0200.xml"));
	}

	@Test
	public void testWithTranslation() throws URISyntaxException, IOException {
		IData testFile = this.getTestFile();
		assertNotNull(testFile);

		CDDocument cdDoc = new CDDocument(
				testFile, "en");

		assertEquals(new URI("sua://survey/cd/2013-09-18T17:45:54.888+02:00"),
				cdDoc.getUri());

		assertEquals(
				CalendarUtils.fromISO8601("2013-09-18T17:45:54.888+02:00"),
				cdDoc.getCompleted());

		assertEquals(27, cdDoc.getSize());

		// access
		assertEquals("studies", cdDoc.getQuestionKey(0));
		assertEquals("Bioinformatik", cdDoc.getQuestionAnswer(0));
		assertEquals("What are you studying / did you study?",
				cdDoc.getQuestionTitle(0));

		// umlauts
		assertEquals("mainPurpose", cdDoc.getQuestionKey(6));
		assertEquals(
				"Bisher für noch nichts. Hoffe bald für Development im Berreich genome assembly",
				cdDoc.getQuestionAnswer(6));
		assertEquals(null, cdDoc.getQuestionTitle(6)); // no
														// translation
	}

}
