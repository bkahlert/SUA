package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.bkahlert.devel.nebula.utils.FileUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;

// TODO test multiple BaseDataDirectories
public class CompilationServiceTest {
	private IBaseDataContainer createBaseDataContainer() throws IOException {
		File tempDirectory = FileUtils.getTempDirectory();
		File dataSet = new File(tempDirectory, "__dataset.txt");
		dataSet.createNewFile();
		dataSet.deleteOnExit();
		return new FileBaseDataContainer(tempDirectory);
	}

	private ICompilable compilable = new ICompilable() {
		@Override
		public URI getUri() {
			try {
				return new URI("sua://compilable");
			} catch (URISyntaxException e) {
				return null;
			}
		}
	};
	private ICompilable compilable2 = new ICompilable() {
		@Override
		public URI getUri() {
			try {
				return new URI("sua://compilable2");
			} catch (URISyntaxException e) {
				return null;
			}
		}
	};

	@Test
	public void testCompilationStates() throws IOException {
		IBaseDataContainer baseDataContainer = createBaseDataContainer();
		ICompilationService compilationService = new CompilationService(
				baseDataContainer);

		assertNull(compilationService.compiles(compilable));
		assertNull(compilationService.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, true);
		assertTrue(compilationService.compiles(compilable));
		assertNull(compilationService.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, null);
		assertNull(compilationService.compiles(compilable));
		assertNull(compilationService.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, false);
		assertFalse(compilationService.compiles(compilable));
		assertNull(compilationService.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, true);
		compilationService.compiles(new ICompilable[] { compilable2 }, false);
		assertTrue(compilationService.compiles(compilable));
		assertFalse(compilationService.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, null);
		compilationService.compiles(new ICompilable[] { compilable2 }, true);
		assertNull(compilationService.compiles(compilable));
		assertTrue(compilationService.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, false);
		compilationService.compiles(new ICompilable[] { compilable2 }, false);
		assertFalse(compilationService.compiles(compilable));
		assertFalse(compilationService.compiles(compilable2));

		// check synchronization and persistence
		ICompilationService compilationService2 = new CompilationService(
				baseDataContainer);
		assertFalse(compilationService.compiles(compilable));
		assertFalse(compilationService.compiles(compilable2));
		assertFalse(compilationService2.compiles(compilable));
		assertFalse(compilationService2.compiles(compilable2));

		compilationService.compiles(new ICompilable[] { compilable }, null);
		compilationService.compiles(new ICompilable[] { compilable2 }, true);
		assertNull(compilationService.compiles(compilable));
		assertTrue(compilationService.compiles(compilable2));

		// values stay unchanged since the second compilation service does not
		// know of changes in the persistence level
		assertFalse(compilationService2.compiles(compilable));
		assertFalse(compilationService2.compiles(compilable2));
	}

	@Test
	public void testCompilerOutputs() throws IOException {
		IBaseDataContainer baseDataContainer = createBaseDataContainer();
		ICompilationService compilationService = new CompilationService(
				baseDataContainer);

		assertEquals("", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, "test");
		assertEquals("test", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, null);
		assertEquals("", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, " ");
		assertEquals("", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, "test2");
		compilationService.compilerOutput(compilable2, "   ");
		assertEquals("test2", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, null);
		compilationService.compilerOutput(compilable2, "test3");
		assertEquals("", compilationService.compilerOutput(compilable));
		assertEquals("test3", compilationService.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, "");
		compilationService.compilerOutput(compilable2, null);
		assertEquals("", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));

		// check synchronization and persistence
		ICompilationService compilationService2 = new CompilationService(
				baseDataContainer);
		assertEquals("", compilationService.compilerOutput(compilable));
		assertEquals("", compilationService.compilerOutput(compilable2));
		assertEquals("", compilationService2.compilerOutput(compilable));
		assertEquals("", compilationService2.compilerOutput(compilable2));

		compilationService.compilerOutput(compilable, "test4");
		compilationService.compilerOutput(compilable2, "test5");
		assertEquals("test4", compilationService.compilerOutput(compilable));
		assertEquals("test5", compilationService.compilerOutput(compilable2));
		assertEquals("test4", compilationService2.compilerOutput(compilable));
		assertEquals("test5", compilationService2.compilerOutput(compilable2));
	}
}
