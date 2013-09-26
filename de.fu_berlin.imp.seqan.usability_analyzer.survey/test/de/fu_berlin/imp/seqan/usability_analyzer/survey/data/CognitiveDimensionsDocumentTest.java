package de.fu_berlin.imp.seqan.usability_analyzer.survey.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml.CognitiveDimensionsDocument;

public class CognitiveDimensionsDocumentTest {

	private static final String root = "/"
			+ CognitiveDimensionsDocumentTest.class.getPackage().getName()
					.replace('.', '/') + "/..";
	private static final IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
			FileUtils.getFile(root));
	private static final String dataDirectory = root + "/data";

	private IData getNumbersCsvExport() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(CognitiveDimensionsDocumentTest.class,
						dataDirectory
								+ "/2013-09-18T17-45-54.88891500+0200.xml"));
	}

	@Test
	public void testMatrixForm() throws URISyntaxException, IOException {
		IData testFile = this.getNumbersCsvExport();
		assertNotNull(testFile);

		CognitiveDimensionsDocument cdDoc = new CognitiveDimensionsDocument(
				testFile);
		assertEquals(27, cdDoc.getSize());

		assertEquals("studies", cdDoc.getQuestion(0).getKey());
	}

}
