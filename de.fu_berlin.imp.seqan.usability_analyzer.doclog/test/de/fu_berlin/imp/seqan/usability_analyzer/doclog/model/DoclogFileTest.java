package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class DoclogFileTest {

	private static final String root = "/"
			+ DoclogFileTest.class.getPackage().getName().replace('.', '/')
			+ "/..";
	private static final IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
			FileUtils.getFile(root));
	private static final String dataDirectory = root + "/data";

	private IData getIdDoclogFile() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(DoclogFileTest.class, dataDirectory
						+ "/0meio6dzt3eo1wj7.doclog"));
	}

	private IData getFingerprintDoclogFile() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(DoclogFileTest.class, dataDirectory
						+ "/!2aa2aaccc0b9b73b230bb4667c5971f8.doclog"));
	}

	@Test
	public void testGetId() throws URISyntaxException {
		IData file = getIdDoclogFile();
		Assert.assertEquals(new ID("0meio6dzt3eo1wj7"), Doclog.getID(file));
		Assert.assertNull(Doclog.getFingerprint(file));
	}

	@Test
	public void testGetFingerprint() throws URISyntaxException {
		IData file = getFingerprintDoclogFile();
		Assert.assertNull(Doclog.getID(file));
		Assert.assertEquals(
				new Fingerprint("!2aa2aaccc0b9b73b230bb4667c5971f8"),
				Doclog.getFingerprint(file));
	}

	@Test
	public void testGetDateRange() throws URISyntaxException {
		IData idFile = getIdDoclogFile();
		TimeZoneDateRange idDateRange = Doclog.getDateRange(idFile);
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:05:22+02:00"),
				idDateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-14T09:17:49+02:00"),
				idDateRange.getEndDate());

		IData fingerprintFile = getFingerprintDoclogFile();
		TimeZoneDateRange fingerprintDateRange = Doclog
				.getDateRange(fingerprintFile);
		Assert.assertEquals(new TimeZoneDate("2011-09-14T14:31:26+02:00"),
				fingerprintDateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-17T03:17:24+02:00"),
				fingerprintDateRange.getEndDate());
	}

	@Test
	public void testGetToken() throws URISyntaxException {
		Assert.assertEquals(new Token("7qex"),
				Doclog.getToken(getIdDoclogFile()));

		Assert.assertNull(Doclog.getToken(getFingerprintDoclogFile()));
	}
}
