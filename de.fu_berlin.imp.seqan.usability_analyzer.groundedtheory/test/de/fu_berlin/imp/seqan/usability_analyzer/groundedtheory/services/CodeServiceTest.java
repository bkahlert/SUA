package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Episode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl.CodeServicesHelper;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;

public class CodeServiceTest extends CodeServicesHelper {

	public CodeServiceTest() throws URISyntaxException {
		super();
	}

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();

	@Test(expected = CodeStoreReadException.class)
	public void testNonExistingGetCodes() throws IOException {
		this.getNonExistingCodeService();
	}

	@Test
	public void testGetCodes() throws IOException {
		final URI uri = CodeServiceTest.this.codeInstance1.getId();

		ICodeService emptyCodeService = this.getEmptyCodeService();
		Assert.assertEquals(0, emptyCodeService.getCodes(uri).size());

		ICodeService smallCodeService = this.getSmallCodeService();
		Assert.assertEquals(1, smallCodeService.getCodes(uri).size());
		Assert.assertEquals(9908372l, smallCodeService.getCodes(uri).get(0)
				.getId());
	}

	@Test
	public void testAddCode() throws IOException, URISyntaxException {
		final URI uri1 = CodeServiceTest.this.codeInstance1.getId();
		final URI uri2 = CodeServiceTest.this.codeInstance2.getId();

		ICodeService emptyCodeService = this.getEmptyCodeService();
		Assert.assertEquals(0, emptyCodeService.getCodes(uri1).size());
		Assert.assertEquals(0, emptyCodeService.getCodes(uri2).size());
		emptyCodeService.addCode(this.code1.getCaption(),
				new RGB(1.0, 1.0, 1.0), uri1);
		Assert.assertEquals(1, emptyCodeService.getCodes(uri1).size());
		Assert.assertEquals(this.code1.getCaption(),
				emptyCodeService.getCodes(uri1).get(0).getCaption());
		emptyCodeService.addCode(this.code1, uri2);
		Assert.assertEquals(1, emptyCodeService.getCodes(uri2).size());
		Assert.assertEquals(this.code1, emptyCodeService.getCodes(uri2).get(0));
		Assert.assertFalse(emptyCodeService.getCodes(uri1).get(0)
				.equals(emptyCodeService.getCodes(uri2).get(0)));

		ICodeService smallCodeService = this.getSmallCodeService();
		Assert.assertEquals(1, smallCodeService.getCodes(uri1).size());
		Assert.assertEquals(this.code2, smallCodeService.getCodes(uri1).get(0));

		Assert.assertEquals(1, smallCodeService.getCodes(uri2).size());
		Assert.assertEquals(this.code1, smallCodeService.getCodes(uri2).get(0));
		smallCodeService.addCode(this.code2, uri2);
		Assert.assertEquals(2, smallCodeService.getCodes(uri2).size());
		ICode associatedCode1 = smallCodeService.getCodes(uri2).get(0);
		ICode associatedCode2 = smallCodeService.getCodes(uri2).get(1);
		assertTrue((this.code1.equals(associatedCode1) && this.code2
				.equals(associatedCode2))
				|| (this.code1.equals(associatedCode2) && this.code2
						.equals(associatedCode1)));
	}

	@Test(expected = CodeServiceException.class)
	public void testDuplicateAddCode() throws IOException {
		final URI uri = CodeServiceTest.this.codeInstance1.getId();

		ICodeService emptyCodeService = this.getEmptyCodeService();
		Assert.assertEquals(0, emptyCodeService.getCodes(uri).size());
		emptyCodeService.addCode(this.code2, uri);
		emptyCodeService.addCode(this.code2, uri);
	}

	@Test(expected = CodeServiceException.class)
	public void testEmptyRemoveCode() throws IOException {
		final URI uri = CodeServiceTest.this.codeInstance1.getId();

		ICodeService codeService = this.getEmptyCodeService();
		codeService.removeCodes(Arrays.asList(this.code1), uri);
	}

	@Test(expected = CodeServiceException.class)
	public void testSmallRemoveCodeNonExisting() throws IOException {
		final URI uri = CodeServiceTest.this.codeInstance1.getId();

		ICodeService codeService = this.getSmallCodeService();
		codeService.removeCodes(Arrays.asList(this.code1), uri);
	}

	@Test
	public void testSmallRemoveCodeExisting() throws IOException {
		final URI uri1 = CodeServiceTest.this.codeInstance1.getId();
		final URI uri2 = CodeServiceTest.this.codeInstance2.getId();
		final URI uri3 = CodeServiceTest.this.codeInstance3.getId();

		ICodeService codeService = this.getSmallCodeService();

		Assert.assertEquals(1, codeService.getCodes(uri1).size());
		Assert.assertEquals(this.code2, codeService.getCodes(uri1).get(0));
		Assert.assertEquals(1, codeService.getCodes(uri2).size());
		Assert.assertEquals(this.code1, codeService.getCodes(uri2).get(0));
		Assert.assertEquals(1, codeService.getCodes(uri3).size());
		Assert.assertEquals(this.code2, codeService.getCodes(uri3).get(0));

		codeService.removeCodes(Arrays.asList(this.code2), uri1);

		Assert.assertEquals(0, codeService.getCodes(uri1).size());
		Assert.assertEquals(1, codeService.getCodes(uri2).size());
		Assert.assertEquals(this.code1, codeService.getCodes(uri2).get(0));
		Assert.assertEquals(1, codeService.getCodes(uri3).size());
		Assert.assertEquals(this.code2, codeService.getCodes(uri3).get(0));
	}

	@Test(expected = CodeServiceException.class)
	public void testEmptyCodeNonExisting() throws IOException {
		ICodeService codeService = this.getEmptyCodeService();
		codeService.deleteCode(this.code1);
	}

	@Test(expected = CodeServiceException.class)
	public void testDeleteCodeNonExisting() throws IOException {
		final ICode code = this.context.mock(ICode.class);
		this.context.checking(new Expectations() {
			{
				this.allowing(code).getId();
				this.will(returnValue(-1L));
			}
		});

		ICodeService codeService = this.getSmallCodeService();

		codeService.deleteCode(code);
	}

	@Test
	public void testDeleteCodeExisting() throws IOException {
		final URI uri1 = CodeServiceTest.this.codeInstance1.getId();
		final URI uri2 = CodeServiceTest.this.codeInstance2.getId();
		final URI uri3 = CodeServiceTest.this.codeInstance3.getId();

		ICodeService codeService = this.getSmallCodeService();

		Assert.assertEquals(1, codeService.getCodes(uri1).size());
		Assert.assertEquals(this.code2, codeService.getCodes(uri1).get(0));
		Assert.assertEquals(1, codeService.getCodes(uri2).size());
		Assert.assertEquals(this.code1, codeService.getCodes(uri2).get(0));
		Assert.assertEquals(1, codeService.getCodes(uri3).size());
		Assert.assertEquals(this.code2, codeService.getCodes(uri3).get(0));

		codeService.deleteCode(this.code2, true);

		Assert.assertEquals(0, codeService.getCodes(uri1).size());
		Assert.assertEquals(1, codeService.getCodes(uri2).size());
		Assert.assertEquals(this.code1, codeService.getCodes(uri2).get(0));
		Assert.assertEquals(0, codeService.getCodes(uri3).size());

		Assert.assertNull(codeService.getCode(this.code2.getId()));
	}

	public void testSaveEpisode() throws IOException {
		ICodeService codeService = this.getSmallCodeService();
		assertEquals(0, codeService.getEpisodedIdentifiers().size());
		assertEquals(0,
				codeService.getEpisodes(IdentifierFactory.createFrom("id"))
						.size());

		Episode episode = new Episode(IdentifierFactory.createFrom("id"),
				new TimeZoneDateRange(new TimeZoneDate(
						"2000-01-02T14:00:00.000+02:00"), new TimeZoneDate(
						"2000-01-02T14:30:00.000+02:00")), "TimelineViewer");
		codeService.addEpisodeAndSave(episode);
		assertEquals(1, codeService.getEpisodedIdentifiers().size());
		assertEquals(IdentifierFactory.createFrom("id"), codeService
				.getEpisodedIdentifiers().get(0));
		assertEquals(1,
				codeService.getEpisodes(IdentifierFactory.createFrom("id"))
						.size());
		assertEquals(episode,
				codeService.getEpisodes(IdentifierFactory.createFrom("id"))
						.iterator().next());

		codeService.getEpisodes(IdentifierFactory.createFrom("id")).remove(0);
		assertEquals(0, codeService.getEpisodedIdentifiers().size());
		assertEquals(0,
				codeService.getEpisodes(IdentifierFactory.createFrom("id"))
						.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSaveEpisodeIllegalArgumentException() throws IOException {
		this.getSmallCodeService()
				.getEpisodes(IdentifierFactory.createFrom("id")).add(null);
	}
}
