package de.fu_berlin.imp.apiua.doclog.model;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileData;
import de.fu_berlin.imp.apiua.core.model.data.impl.StringData;
import de.fu_berlin.imp.apiua.core.model.identifier.Token;
import de.fu_berlin.imp.apiua.core.util.FileUtils;
import de.fu_berlin.imp.apiua.doclog.model.Doclog;

public class DoclogTest {

	private static final String root = "/"
			+ DoclogTest.class.getPackage().getName().replace('.', '/') + "/..";
	private static final IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
			FileUtils.getFile(root));
	private static final String dataDirectory = root + "/data";

	private IData getIdDoclogFile() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(DoclogTest.class, dataDirectory
						+ "/0meio6dzt3eo1wj7.doclog"));
	}

	private IData getFingerprintDoclogFile() throws URISyntaxException {
		return new FileData(baseDataContainer, baseDataContainer,
				FileUtils.getFile(DoclogTest.class, dataDirectory
						+ "/!2aa2aaccc0b9b73b230bb4667c5971f8.doclog"));
	}

	@Test
	public void testGetId() throws URISyntaxException {
		IData file = this.getIdDoclogFile();
		Assert.assertEquals(IdentifierFactory.createFrom("0meio6dzt3eo1wj7"),
				Doclog.getIdentifier(file));
	}

	@Test
	public void testGetFingerprint() throws URISyntaxException {
		IData file = this.getFingerprintDoclogFile();
		Assert.assertEquals(IdentifierFactory
				.createFrom("!2aa2aaccc0b9b73b230bb4667c5971f8"), Doclog
				.getIdentifier(file));
	}

	@Test
	public void testGetDateRange() throws URISyntaxException {
		IData idFile = this.getIdDoclogFile();
		TimeZoneDateRange idDateRange = Doclog.getDateRange(idFile);
		Assert.assertEquals(new TimeZoneDate("2011-09-13T12:05:22+02:00"),
				idDateRange.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-14T09:17:49+02:00"),
				idDateRange.getEndDate());

		IData fingerprintFile = this.getFingerprintDoclogFile();
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
				Doclog.getToken(this.getIdDoclogFile()));

		Assert.assertNull(Doclog.getToken(this.getFingerprintDoclogFile()));
	}

	@Test
	public void testSumTyping() {
		String[] lines = new String[] {
				"2012-08-26T14:03:54.247+02:00	TYPING-want to learn how we document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:03:54.444+02:00	TYPING-want to learn how we document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:04:40.365+02:00	TYPING-want to learn how we document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:04:49.851+02:00	TYPING-want to learn how we document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:04:50.079+02:00	TYPING-want to learn how we document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:04:50.558+02:00	TYPING-want to learn how we: document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:04:51.603+02:00	TYPING-want to learn how we:  document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:04:53.207+02:00	TYPING-want to learn how we:  document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:05:05.395+02:00	TYPING-want to learn how we: document or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:05:05.687+02:00	TYPING-want to learn how we: \\ndocument or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:05:05.917+02:00	TYPING-want to learn how we: \\n\\ndocument or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682",
				"2012-08-26T14:05:06.410+02:00	TYPING-want to learn how we: \\n\\ndocument or write tests.	https://trac.seqan.de/wiki/Tutorial	86.56.60.143	-	0	207	1351	682" };
		IData data = new StringData("test", StringUtils.join(lines, "\n"));
		Doclog doclog = new Doclog(data, IdentifierFactory.createFrom("id"),
				new TimeZoneDateRange(new TimeZoneDate(), new TimeZoneDate()),
				null, 3000);
		assertEquals(4, doclog.getDoclogRecords().size());

		assertEquals(new TimeZoneDate("2012-08-26T14:03:54.247+02:00"), doclog
				.getDoclogRecords().get(0).getDate());
		assertEquals("want to learn how we document or write tests.", doclog
				.getDoclogRecords().get(0).getActionParameter());
		assertEquals((long) new TimeZoneDateRange(new TimeZoneDate(
				"2012-08-26T14:03:54.247+02:00"), new TimeZoneDate(
				"2012-08-26T14:04:40.365+02:00")).getDifference() - 1,
				(long) doclog.getDoclogRecords().get(0).getDateRange()
						.getDifference());

		assertEquals(new TimeZoneDate("2012-08-26T14:04:40.365+02:00"), doclog
				.getDoclogRecords().get(1).getDate());
		assertEquals("want to learn how we document or write tests.", doclog
				.getDoclogRecords().get(1).getActionParameter());
		assertEquals((long) new TimeZoneDateRange(new TimeZoneDate(
				"2012-08-26T14:04:40.365+02:00"), new TimeZoneDate(
				"2012-08-26T14:04:49.851+02:00")).getDifference() - 1,
				(long) doclog.getDoclogRecords().get(1).getDateRange()
						.getDifference());

		assertEquals(new TimeZoneDate("2012-08-26T14:04:49.851+02:00"), doclog
				.getDoclogRecords().get(2).getDate());
		assertEquals("want to learn how we:  document or write tests.", doclog
				.getDoclogRecords().get(2).getActionParameter());
		assertEquals((long) new TimeZoneDateRange(new TimeZoneDate(
				"2012-08-26T14:04:49.851+02:00"), new TimeZoneDate(
				"2012-08-26T14:05:05.395+02:00")).getDifference() - 1,
				(long) doclog.getDoclogRecords().get(2).getDateRange()
						.getDifference());

		assertEquals(new TimeZoneDate("2012-08-26T14:05:05.395+02:00"), doclog
				.getDoclogRecords().get(3).getDate());
		assertEquals("want to learn how we: \\n\\ndocument or write tests.",
				doclog.getDoclogRecords().get(3).getActionParameter());
		assertEquals((long) new TimeZoneDateRange(new TimeZoneDate(
				"2012-08-26T14:05:05.395+02:00"), new TimeZoneDate(
				"2012-08-26T14:05:06.410+02:00")).getDifference() - 1,
				(long) doclog.getDoclogRecords().get(3).getDateRange()
						.getDifference());
	}
}
