package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bkahlert.nebula.utils.FileUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
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

	private final ICompilable compilable = new ICompilable() {
		private static final long serialVersionUID = 1L;

		@Override
		public URI getUri() {
			return new URI("sua://compilable");
		}
	};
	private final ICompilable compilable2 = new ICompilable() {
		private static final long serialVersionUID = 1L;

		@Override
		public URI getUri() {
			return new URI("sua://compilable2");
		}
	};

	@Test
	public void testCompilationStates() throws IOException {
		IBaseDataContainer baseDataContainer = this.createBaseDataContainer();
		ICompilationService compilationService = new CompilationService(
				baseDataContainer);

		assertNull(compilationService.compiles(this.compilable));
		assertNull(compilationService.compiles(this.compilable2));

		compilationService
				.compiles(new ICompilable[] { this.compilable }, true);
		assertTrue(compilationService.compiles(this.compilable));
		assertNull(compilationService.compiles(this.compilable2));

		compilationService
				.compiles(new ICompilable[] { this.compilable }, null);
		assertNull(compilationService.compiles(this.compilable));
		assertNull(compilationService.compiles(this.compilable2));

		compilationService.compiles(new ICompilable[] { this.compilable },
				false);
		assertFalse(compilationService.compiles(this.compilable));
		assertNull(compilationService.compiles(this.compilable2));

		compilationService
				.compiles(new ICompilable[] { this.compilable }, true);
		compilationService.compiles(new ICompilable[] { this.compilable2 },
				false);
		assertTrue(compilationService.compiles(this.compilable));
		assertFalse(compilationService.compiles(this.compilable2));

		compilationService
				.compiles(new ICompilable[] { this.compilable }, null);
		compilationService.compiles(new ICompilable[] { this.compilable2 },
				true);
		assertNull(compilationService.compiles(this.compilable));
		assertTrue(compilationService.compiles(this.compilable2));

		compilationService.compiles(new ICompilable[] { this.compilable },
				false);
		compilationService.compiles(new ICompilable[] { this.compilable2 },
				false);
		assertFalse(compilationService.compiles(this.compilable));
		assertFalse(compilationService.compiles(this.compilable2));

		// check synchronization and persistence
		ICompilationService compilationService2 = new CompilationService(
				baseDataContainer);
		assertFalse(compilationService.compiles(this.compilable));
		assertFalse(compilationService.compiles(this.compilable2));
		assertFalse(compilationService2.compiles(this.compilable));
		assertFalse(compilationService2.compiles(this.compilable2));

		compilationService
				.compiles(new ICompilable[] { this.compilable }, null);
		compilationService.compiles(new ICompilable[] { this.compilable2 },
				true);
		assertNull(compilationService.compiles(this.compilable));
		assertTrue(compilationService.compiles(this.compilable2));

		// values stay unchanged since the second compilation service does not
		// know of changes in the persistence level
		assertFalse(compilationService2.compiles(this.compilable));
		assertFalse(compilationService2.compiles(this.compilable2));
	}

	@Test
	public void testCompilerOutputs() throws IOException {
		IBaseDataContainer baseDataContainer = this.createBaseDataContainer();
		ICompilationService compilationService = new CompilationService(
				baseDataContainer);

		assertEquals("", compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, "test");
		assertEquals("test", compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, null);
		assertEquals("", compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, " ");
		assertEquals("", compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, "test2");
		compilationService.compilerOutput(this.compilable2, "   ");
		assertEquals("test2",
				compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, null);
		compilationService.compilerOutput(this.compilable2, "test3");
		assertEquals("", compilationService.compilerOutput(this.compilable));
		assertEquals("test3",
				compilationService.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, "");
		compilationService.compilerOutput(this.compilable2, null);
		assertEquals("", compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));

		// check synchronization and persistence
		ICompilationService compilationService2 = new CompilationService(
				baseDataContainer);
		assertEquals("", compilationService.compilerOutput(this.compilable));
		assertEquals("", compilationService.compilerOutput(this.compilable2));
		assertEquals("", compilationService2.compilerOutput(this.compilable));
		assertEquals("", compilationService2.compilerOutput(this.compilable2));

		compilationService.compilerOutput(this.compilable, "test4");
		compilationService.compilerOutput(this.compilable2, "test5");
		assertEquals("test4",
				compilationService.compilerOutput(this.compilable));
		assertEquals("test5",
				compilationService.compilerOutput(this.compilable2));
		assertEquals("test4",
				compilationService2.compilerOutput(this.compilable));
		assertEquals("test5",
				compilationService2.compilerOutput(this.compilable2));
	}

	@Test
	public void testRunOutputs() throws IOException {
		IBaseDataContainer baseDataContainer = this.createBaseDataContainer();
		ICompilationService compilationService = new CompilationService(
				baseDataContainer);

		assertEquals("", compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, "test");
		assertEquals("test",
				compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, null);
		assertEquals("", compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, " ");
		assertEquals("", compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, "test2");
		compilationService.executionOutput(this.compilable2, "   ");
		assertEquals("test2",
				compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, null);
		compilationService.executionOutput(this.compilable2, "test3");
		assertEquals("", compilationService.executionOutput(this.compilable));
		assertEquals("test3",
				compilationService.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, "");
		compilationService.executionOutput(this.compilable2, null);
		assertEquals("", compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));

		// check synchronization and persistence
		ICompilationService compilationService2 = new CompilationService(
				baseDataContainer);
		assertEquals("", compilationService.executionOutput(this.compilable));
		assertEquals("", compilationService.executionOutput(this.compilable2));
		assertEquals("", compilationService2.executionOutput(this.compilable));
		assertEquals("", compilationService2.executionOutput(this.compilable2));

		compilationService.executionOutput(this.compilable, "test4");
		compilationService.executionOutput(this.compilable2, "test5");
		assertEquals("test4",
				compilationService.executionOutput(this.compilable));
		assertEquals("test5",
				compilationService.executionOutput(this.compilable2));
		assertEquals("test4",
				compilationService2.executionOutput(this.compilable));
		assertEquals("test5",
				compilationService2.executionOutput(this.compilable2));
	}
}
