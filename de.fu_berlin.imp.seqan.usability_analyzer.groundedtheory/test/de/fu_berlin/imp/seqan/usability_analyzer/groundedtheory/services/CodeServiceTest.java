package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.swt.graphics.RGB;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Episode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
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
		getNonExistingCodeService();
	}

	@Test
	public void testGetCodes() throws IOException {
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(codeable).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));
			}
		});

		ICodeService emptyCodeService = getEmptyCodeService();
		Assert.assertEquals(0, emptyCodeService.getCodes(codeable).size());

		ICodeService smallCodeService = getSmallCodeService();
		Assert.assertEquals(1, smallCodeService.getCodes(codeable).size());
		Assert.assertEquals(9908372l, smallCodeService.getCodes(codeable)
				.get(0).getId());
	}

	@Test
	public void testAddCode() throws IOException, URISyntaxException {
		final ICodeable codeable1 = context.mock(ICodeable.class,
				"ICodeable #1");
		final ICodeable codeable2 = context.mock(ICodeable.class,
				"ICodeable #2");
		context.checking(new Expectations() {
			{
				allowing(codeable1).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));

				allowing(codeable2).getCodeInstanceID();
				will(returnValue(new URI("invalid://id")));
			}
		});

		ICodeService emptyCodeService = getEmptyCodeService();
		Assert.assertEquals(0, emptyCodeService.getCodes(codeable1).size());
		Assert.assertEquals(0, emptyCodeService.getCodes(codeable2).size());
		emptyCodeService.addCode(code1.getCaption(), codeable1);
		Assert.assertEquals(1, emptyCodeService.getCodes(codeable1).size());
		Assert.assertEquals(code1.getCaption(),
				emptyCodeService.getCodes(codeable1).get(0).getCaption());
		emptyCodeService.addCode(code1, codeable2);
		Assert.assertEquals(1, emptyCodeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, emptyCodeService.getCodes(codeable2).get(0));
		Assert.assertFalse(emptyCodeService.getCodes(codeable1).get(0)
				.equals(emptyCodeService.getCodes(codeable2).get(0)));

		ICodeService smallCodeService = getSmallCodeService();
		Assert.assertEquals(1, smallCodeService.getCodes(codeable1).size());
		Assert.assertEquals(code2.getId(), smallCodeService.getCodes(codeable1)
				.get(0).getId());
		Assert.assertEquals(0, smallCodeService.getCodes(codeable2).size());
		smallCodeService.addCode(code1, codeable2);
		Assert.assertEquals(1, smallCodeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, smallCodeService.getCodes(codeable2).get(0));
		smallCodeService.addCode(code2, codeable2);
		Assert.assertEquals(2, smallCodeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, smallCodeService.getCodes(codeable2).get(0));
		Assert.assertEquals(code2, smallCodeService.getCodes(codeable2).get(1));
	}

	@Test(expected = CodeServiceException.class)
	public void testDuplicateAddCode() throws IOException {
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(codeable).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));
			}
		});

		ICodeService emptyCodeService = getEmptyCodeService();
		Assert.assertEquals(0, emptyCodeService.getCodes(codeable).size());
		emptyCodeService.addCode(code2, codeable);
		emptyCodeService.addCode(code2, codeable);
	}

	@Test(expected = CodeServiceException.class)
	public void testEmptyRemoveCode() throws IOException {
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(codeable).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));
			}
		});

		ICodeService codeService = getEmptyCodeService();
		codeService.removeCodes(Arrays.asList(code1), codeable);
	}

	@Test(expected = CodeServiceException.class)
	public void testSmallRemoveCodeNonExisting() throws IOException {
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(codeable).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));
			}
		});

		ICodeService codeService = getSmallCodeService();
		codeService.removeCodes(Arrays.asList(code1), codeable);
	}

	@Test
	public void testSmallRemoveCodeExisting() throws IOException {
		final ICodeable codeable1 = context.mock(ICodeable.class,
				"ICodeable #1");
		final ICodeable codeable2 = context.mock(ICodeable.class,
				"ICodeable #2");
		final ICodeable codeable3 = context.mock(ICodeable.class,
				"ICodeable #3");
		context.checking(new Expectations() {
			{
				allowing(codeable1).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));

				allowing(codeable2).getCodeInstanceID();
				will(returnValue(codeInstance2.getId()));

				allowing(codeable3).getCodeInstanceID();
				will(returnValue(codeInstance3.getId()));
			}
		});

		ICodeService codeService = getSmallCodeService();

		Assert.assertEquals(1, codeService.getCodes(codeable1).size());
		Assert.assertEquals(code2, codeService.getCodes(codeable1).get(0));
		Assert.assertEquals(1, codeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, codeService.getCodes(codeable2).get(0));
		Assert.assertEquals(1, codeService.getCodes(codeable3).size());
		Assert.assertEquals(code2, codeService.getCodes(codeable3).get(0));

		codeService.removeCodes(Arrays.asList(code2), codeable1);

		Assert.assertEquals(0, codeService.getCodes(codeable1).size());
		Assert.assertEquals(1, codeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, codeService.getCodes(codeable2).get(0));
		Assert.assertEquals(1, codeService.getCodes(codeable3).size());
		Assert.assertEquals(code2, codeService.getCodes(codeable3).get(0));
	}

	@Test(expected = CodeServiceException.class)
	public void testEmptyCodeNonExisting() throws IOException {
		ICodeService codeService = getEmptyCodeService();
		codeService.deleteCode(code1);
	}

	@Test(expected = CodeServiceException.class)
	public void testDeleteCodeNonExisting() throws IOException {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(-1L));
			}
		});

		ICodeService codeService = getSmallCodeService();

		codeService.deleteCode(code);
	}

	@Test
	public void testDeleteCodeExisting() throws IOException {
		final ICodeable codeable1 = context.mock(ICodeable.class,
				"ICodeable #1");
		final ICodeable codeable2 = context.mock(ICodeable.class,
				"ICodeable #2");
		final ICodeable codeable3 = context.mock(ICodeable.class,
				"ICodeable #3");
		context.checking(new Expectations() {
			{
				allowing(codeable1).getCodeInstanceID();
				will(returnValue(codeInstance1.getId()));

				allowing(codeable2).getCodeInstanceID();
				will(returnValue(codeInstance2.getId()));

				allowing(codeable3).getCodeInstanceID();
				will(returnValue(codeInstance3.getId()));
			}
		});

		ICodeService codeService = getSmallCodeService();

		Assert.assertEquals(1, codeService.getCodes(codeable1).size());
		Assert.assertEquals(code2, codeService.getCodes(codeable1).get(0));
		Assert.assertEquals(1, codeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, codeService.getCodes(codeable2).get(0));
		Assert.assertEquals(1, codeService.getCodes(codeable3).size());
		Assert.assertEquals(code2, codeService.getCodes(codeable3).get(0));

		codeService.deleteCode(code2, true);

		Assert.assertEquals(0, codeService.getCodes(codeable1).size());
		Assert.assertEquals(1, codeService.getCodes(codeable2).size());
		Assert.assertEquals(code1, codeService.getCodes(codeable2).get(0));
		Assert.assertEquals(0, codeService.getCodes(codeable3).size());

		Assert.assertNull(codeService.getCode(code2.getId()));
	}

	public void testSaveEpisode() throws IOException {
		ICodeService codeService = getSmallCodeService();
		assertEquals(0, codeService.getEpisodedKeys().size());
		assertEquals(0, codeService.getEpisodes(new ID("id")).size());

		Episode episode = new Episode(new ID("id"), new TimeZoneDateRange(
				new TimeZoneDate("2000-01-02T14:00:00.000+02:00"),
				new TimeZoneDate("2000-01-02T14:30:00.000+02:00")), "Test",
				new RGB(120, 130, 140));
		codeService.addEpisodeAndSave(episode);
		assertEquals(1, codeService.getEpisodedKeys().size());
		assertEquals(new ID("id"), (ID) codeService.getEpisodedKeys().get(0));
		assertEquals(1, codeService.getEpisodes(new ID("id")).size());
		assertEquals(episode, codeService.getEpisodes(new ID("id")).iterator()
				.next());

		codeService.getEpisodes(new ID("id")).remove(0);
		assertEquals(0, codeService.getEpisodedKeys().size());
		assertEquals(0, codeService.getEpisodes(new ID("id")).size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSaveEpisodeIllegalArgumentException() throws IOException {
		getSmallCodeService().getEpisodes(new ID("id")).add(null);
	}
}
