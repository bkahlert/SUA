package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Episode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeHasChildCodesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteAbandonedCodeInstancesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.CodeStoreHelper;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

public class CodeStoreTest extends CodeStoreHelper {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setThreadingPolicy(new Synchroniser());
		}
	};

	public CodeStoreTest() throws IOException, URISyntaxException {
		super();
	}

	@Test(expected = CodeStoreReadException.class)
	public void testNonExistingLoadCodes() throws IOException,
			SAXParseException {
		getNonExistingCodeStore().getTopLevelCodes();
	}

	@Test(expected = CodeStoreReadException.class)
	public void testNonExistingLoadCodeInstances() throws IOException,
			SAXParseException {
		getNonExistingCodeStore().loadInstances();
	}

	@Test
	public void testEmptyLoadCodes() throws IOException, SAXParseException {
		List<ICode> loadedCodes = getEmptyCodeStore().getTopLevelCodes();
		assertEquals(0, loadedCodes.size());
	}

	@Test
	public void testEmptyLoadCodeInstances() throws IOException,
			SAXParseException {
		Set<ICodeInstance> loadedCodeInstances = getEmptyCodeStore()
				.loadInstances();
		assertEquals(0, loadedCodeInstances.size());
	}

	@Test
	public void testLoadCodes() throws IOException {
		ICodeStore codeStore = getEmptyCodeStore();
		codeStore.addAndSaveCode(code1);
		codeStore.addAndSaveCode(code2);
		codeStore
				.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance1 });
		codeStore.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance2,
				codeInstance3 });
		codeStore.save();
		testCodes(getSmallCodeStore(), codes);
	}

	@Test
	public void testLoadCodeInstances() throws IOException, SAXParseException {
		testCodeInstances(getSmallCodeStore(), codeInstances);
	}

	@Test
	public void testNewFileSave() throws IOException, SAXException,
			ParserConfigurationException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		for (ICode code : codes)
			newCodeStore.addAndSaveCode(code);
		for (ICodeInstance instance : codeInstances)
			newCodeStore
					.addAndSaveCodeInstances(new ICodeInstance[] { instance });
		testCodes(newCodeStore, codes);
		testCodeInstances(newCodeStore, codeInstances);
	}

	@Test
	public void testNewFileSaveCodes() throws IOException, SAXException,
			ParserConfigurationException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		for (ICode code : codes)
			newCodeStore.addAndSaveCode(code);
		newCodeStore.save();
		testCodes(newCodeStore, codes);
	}

	@Test
	public void testNewFileSaveCodeInstances() throws IOException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.addAndSaveCode(code2);
		newCodeStore.addAndSaveCodeInstances(new ICodeInstance[] {
				codeInstance1, codeInstance3 });

		assertEquals(2, newCodeStore.loadInstances().size());
		testCodeInstances(newCodeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });
	}

	@Test(expected = CodeStoreWriteException.class)
	public void testNewFileSaveCodeInstancesWithoutCodes() throws IOException,
			SAXException, ParserConfigurationException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.addAndSaveCodeInstances(new ICodeInstance[] {
				codeInstance1, codeInstance3, codeInstance2 });
		newCodeStore.save();
	}

	@Test
	public void testNewFileAddAndSaveCodeInstances() throws IOException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.addAndSaveCode(code2);
		newCodeStore
				.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance1 });
		newCodeStore
				.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance3 });

		assertEquals(2, newCodeStore.loadInstances().size());
		testCodeInstances(newCodeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		newCodeStore.addAndSaveCode(code2);
		assertEquals(2, newCodeStore.loadInstances().size());
		testCodeInstances(newCodeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });
	}

	@Test
	public void testSmallFileSaveCodes() throws IOException {
		ICodeStore codeStore = getSmallCodeStore();

		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		ICode code3 = new Code(42l, "solution", new TimeZoneDate());

		codeStore.addAndSaveCode(code3);
		assertEquals(3, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, new ICode[] { code1, code2, code3 });
		assertEquals(3, codeStore.loadInstances().size());
		testCodeInstances(codeStore, codeInstances);
	}

	@Test(expected = CodeStoreWriteAbandonedCodeInstancesException.class)
	public void testSmallFileSaveCodesMakingInstancesInvalid()
			throws IOException, CodeHasChildCodesException,
			CodeDoesNotExistException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.removeAndSaveCode(code1);
	}

	@Test
	public void testSmallFileSaveCodeInstances() throws IOException,
			CodeHasChildCodesException, CodeDoesNotExistException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.removeAndSaveCodeInstance(codeInstance2);
		assertEquals(2, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, codes);
		assertEquals(2, codeStore.loadInstances().size());
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.removeAndSaveCode(code1);
		assertEquals(1, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, new ICode[] { code2 });
		assertEquals(2, codeStore.loadInstances().size());
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.removeAndSaveCodeInstance(codeInstance3);
		assertEquals(1, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, new ICode[] { code2 });
		assertEquals(1, codeStore.loadInstances().size());
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1 });

		codeStore
				.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance3 });
		assertEquals(1, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, new ICode[] { code2 });
		assertEquals(2, codeStore.loadInstances().size());
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });
	}

	@Test(expected = CodeStoreWriteAbandonedCodeInstancesException.class)
	public void testSmallFileSaveCodeInstancesMakingInstancesInvalid()
			throws IOException, CodeHasChildCodesException,
			CodeDoesNotExistException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.removeAndSaveCodeInstance(codeInstance2);
		assertEquals(2, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, codes);
		assertEquals(2, codeStore.loadInstances().size());
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.removeAndSaveCode(code1);
		assertEquals(1, codeStore.getTopLevelCodes().size());
		testCodes(codeStore, new ICode[] { code2 });
		assertEquals(2, codeStore.loadInstances().size());
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore
				.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance1 });
		codeStore.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance2,
				codeInstance3 });
	}

	@Test
	public void testCreateCode() throws IOException, CodeStoreFullException {
		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		ICode code = codeStore.createCode("Code #1");
		assertEquals(Long.MIN_VALUE, code.getId());
		assertEquals("Code #1", code.getCaption());
	}

	@Test
	public void testNonExistingCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		assertEquals(new Code(Long.MIN_VALUE, "Code #1", new TimeZoneDate()),
				codeStore.createCode("Code #1"));
		assertEquals(
				new Code(Long.MIN_VALUE + 1, "Code #2", new TimeZoneDate()),
				codeStore.createCode("Code #2"));
		codeStore.addAndSaveCode(new Code(5l, "Code #3", new TimeZoneDate()));
		assertEquals(new Code(6l, "Code #4", new TimeZoneDate()),
				codeStore.createCode("Code #4"));
	}

	@Test
	public void testEmptyCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		assertEquals(new Code(Long.MIN_VALUE, "Code #1", new TimeZoneDate()),
				codeStore.createCode("Code #1"));
		assertEquals(
				new Code(Long.MIN_VALUE + 1, "Code #2", new TimeZoneDate()),
				codeStore.createCode("Code #2"));
		codeStore.addAndSaveCode(new Code(5l, "Code #3", new TimeZoneDate()));
		assertEquals(new Code(6l, "Code #4", new TimeZoneDate()),
				codeStore.createCode("Code #4"));
	}

	@Test
	public void testSmallCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		assertEquals(new Code(234233209l + 1l, "Code #1", new TimeZoneDate()),
				codeStore.createCode("Code #1"));
		assertEquals(new Code(234233209l + 2l, "Code #2", new TimeZoneDate()),
				codeStore.createCode("Code #2"));
		codeStore.addAndSaveCode(new Code(300000000l, "Code #3",
				new TimeZoneDate()));
		assertEquals(new Code(300000001l, "Code #4", new TimeZoneDate()),
				codeStore.createCode("Code #4"));
	}

	@Test(expected = CodeStoreFullException.class)
	public void testOverflowCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.addAndSaveCode(new Code(Long.MAX_VALUE, "Code #1",
				new TimeZoneDate()));
		codeStore.createCode("Code #2");
	}

	@Test(expected = InvalidParameterException.class)
	public void testNonExistingCreateCodeInstance() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(code1.getId()));

				allowing(codeable).getCodeInstanceID();
				will(returnValue("my_id"));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		codeStore.createCodeInstances(new ICode[] { code },
				new ICodeable[] { codeable });
	}

	@Test(expected = InvalidParameterException.class)
	public void testEmptyCreateCodeInstance() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(code1.getId()));

				allowing(codeable).getCodeInstanceID();
				will(returnValue("my_id"));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		codeStore.createCodeInstances(new ICode[] { code },
				new ICodeable[] { codeable });
	}

	@Test
	public void testSmallCreateCodeInstance() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException,
			URISyntaxException {
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(codeable).getCodeInstanceID();
				will(returnValue(new URI("sua://new_id")));
			}
		});

		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		final ICodeInstance codeInstance = codeStore.createCodeInstances(
				new ICode[] { code1 }, new ICodeable[] { codeable })[0];
		codeStore.addAndSaveCodeInstances(new ICodeInstance[] { codeInstance });
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3, new ICodeInstance() {

					@Override
					public ICode getCode() {
						return codeInstance.getCode();
					}

					@Override
					public URI getId() {
						return codeInstance.getId();
					}

					@Override
					public TimeZoneDate getCreation() {
						TimeZoneDate creation = codeInstance.getCreation();
						long millisecondPortion = creation.getTime() % 1000l;
						creation.addMilliseconds(-millisecondPortion);
						return creation;
					}
				} });
	}

	@Test
	public void testLoadInstances() throws IOException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodeInstances(codeStore.loadInstances(), codeInstances);
	}

	@Test(expected = CodeInstanceDoesNotExistException.class)
	public void testSmallDeleteInexistingCodeInstance() throws Exception {
		final ICodeInstance codeInstance = context.mock(ICodeInstance.class);
		context.checking(new Expectations() {
			{
				allowing(codeInstance).getId();
				will(returnValue("my_id"));
			}
		});

		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.deleteCodeInstance(codeInstance);
	}

	@Test
	public void testSmallDeleteExistingCodeInstance() throws Exception {
		ICodeStore codeStore = getSmallCodeStore();

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });

		codeStore.deleteCodeInstance(codeInstance1);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance2,
				codeInstance3 });

		codeStore.deleteCodeInstance(codeInstance2);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance3 });

		codeStore.deleteCodeInstance(codeInstance3);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] {});
	}

	@Test
	public void testEmptyDeleteCodeInstances() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(code1.getId()));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		codeStore.deleteCodeInstances(code);

		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());
	}

	@Test
	public void testSmallDeleteInexistingCodeInstances() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(-1l));
			}
		});

		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.deleteCodeInstances(code);

		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);
	}

	@Test
	public void testSmallDeleteExistingCodeInstances() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });

		codeStore.deleteCodeInstances(code2);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance2 });
	}

	@Test(expected = CodeDoesNotExistException.class)
	public void testEmptyDeleteInexistingCode() throws Exception {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(-1l));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		assertEquals(0, codeStore.getTopLevelCodes().size());
		assertEquals(0, codeStore.loadInstances().size());

		codeStore.removeAndSaveCode(code);
	}

	@Test
	public void testSmallDeleteExistingCode() throws Exception {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });

		codeStore.removeAndSaveCode(code2, true);

		testCodes(codeStore, new ICode[] { code1 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance2 });
	}

	@Test
	public void testSaveMemo() throws Exception {
		ICodeStore codeStore = getSmallCodeStore();
		codeStore.setMemo(codeInstance2, "abc");
		assertEquals("abc", codeStore.getMemo(codeInstance2));
		codeStore.setMemo(code1, "äöü´ß ^°∞ 和平");
		assertEquals("äöü´ß ^°∞ 和平", codeStore.getMemo(code1));
		codeStore.setMemo(codeable2, "line1\nline2\nline3");
		assertEquals("line1\nline2\nline3", codeStore.getMemo(codeable2));

		ICodeStore codeStore2 = loadFromCodeStore(codeStore);
		assertEquals("abc", codeStore2.getMemo(codeInstance2));
		assertEquals("äöü´ß ^°∞ 和平", codeStore2.getMemo(code1));
		assertEquals("line1\nline2\nline3", codeStore2.getMemo(codeable2));

		System.err.println(getTextFromStyledTextWidget("äöü´ß ^°∞ 和平"));

		codeStore.removeAndSaveCode(code1, true);
		assertNull(codeStore.getMemo(code1));
		assertNull(codeStore.getMemo(codeInstance2));
		assertEquals("line1\nline2\nline3", codeStore2.getMemo(codeable2));

		codeStore.setMemo(codeable2, null);
		assertNull(codeStore2.getMemo(codeable2));
	}

	public void testSaveEpisode() throws IOException {
		ICodeStore codeStore = getSmallCodeStore();
		assertEquals(0, codeStore.getEpisodes().size());

		Episode episode = new Episode(new ID("id"), new TimeZoneDateRange(
				new TimeZoneDate("2000-01-02T14:00:00.000+02:00"),
				new TimeZoneDate("2000-01-02T14:30:00.000+02:00")), "Test",
				new RGB(120, 130, 140));
		codeStore.getEpisodes().add(episode);
		assertEquals(1, codeStore.getEpisodes().size());
		assertEquals(episode, codeStore.getEpisodes().iterator().next());

		ICodeStore codeStore2 = loadFromCodeStore(codeStore);
		assertEquals(1, codeStore2.getEpisodes().size());
		assertEquals(episode, codeStore2.getEpisodes().iterator().next());

		codeStore2.getEpisodes().remove(episode);
		assertEquals(0, codeStore2.getEpisodes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSaveEpisodeIllegalArgumentException() throws IOException {
		getSmallCodeStore().getEpisodes().add(null);
	}

	private String getTextFromStyledTextWidget(String text) {
		Display display = new Display();
		final Shell shell = new Shell(display);

		shell.setLayout(new GridLayout());

		final StyledText styledText = new StyledText(shell, SWT.MULTI
				| SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));

		Font font = new Font(shell.getDisplay(), "Courier New", 12, SWT.NORMAL);
		styledText.setFont(font);

		styledText.setText(text);
		final AtomicReference<String> returnedText = new AtomicReference<String>();
		styledText.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				returnedText.set(styledText.getText());
			}
		});

		shell.setSize(300, 120);
		shell.open();

		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				shell.close();
			}
		}, 500);

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		// waiting for other thread to close the shell and make the code
		// continue here

		return returnedText.get();
	}
}
