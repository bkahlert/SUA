package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class DoclogFileTest {

	private static final String root = "/"
			+ DoclogFileTest.class.getPackage().getName().replace('.', '/')
			+ "/..";
	private static final String dataDirectory = root + "/data";

	private File getIdDoclogFile() throws URISyntaxException {
		return FileUtils.getFile(DoclogFileTest.class, dataDirectory
				+ "/0meio6dzt3eo1wj7.doclog");
	}

	private File getFingerprintDoclogFile() throws URISyntaxException {
		return FileUtils.getFile(DoclogFileTest.class, dataDirectory
				+ "/!2aa2aaccc0b9b73b230bb4667c5971f8.doclog");
	}

	@Test
	public void testGetId() throws URISyntaxException {
		File file = getIdDoclogFile();
		Assert.assertEquals(new ID("0meio6dzt3eo1wj7"), DoclogFile.getId(file));
		Assert.assertNull(DoclogFile.getFingerprint(file));
	}

	@Test
	public void testGetFingerprint() throws URISyntaxException {
		File file = getFingerprintDoclogFile();
		Assert.assertNull(DoclogFile.getId(file));
		Assert.assertEquals(
				new Fingerprint("!2aa2aaccc0b9b73b230bb4667c5971f8"),
				DoclogFile.getFingerprint(file));
	}

	@Test
	public void testGetDateRange() throws URISyntaxException {
		File idFile = getIdDoclogFile();
		TimeZoneDateRange idDateRange = DoclogFile.getDateRange(idFile);
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:05:22+02:00"),
				idDateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-14T09:17:49+02:00"),
				idDateRange.getEndDate());

		File fingerprintFile = getFingerprintDoclogFile();
		TimeZoneDateRange fingerprintDateRange = DoclogFile
				.getDateRange(fingerprintFile);
		Assert.assertEquals(new TimeZoneDate("2011-09-14T14:31:26+02:00"),
				fingerprintDateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-17T03:17:24+02:00"),
				fingerprintDateRange.getEndDate());
	}

	@Test
	public void testGetToken() throws URISyntaxException {
		Assert.assertEquals(new Token("7qex"),
				DoclogFile.getToken(getIdDoclogFile()));

		Assert.assertNull(DoclogFile.getToken(getFingerprintDoclogFile()));
	}
}
