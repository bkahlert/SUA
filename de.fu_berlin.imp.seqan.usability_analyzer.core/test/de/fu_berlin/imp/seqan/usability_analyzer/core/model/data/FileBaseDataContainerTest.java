package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;

public class FileBaseDataContainerTest {

	public static File createTempDirectory() throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: "
					+ temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: "
					+ temp.getAbsolutePath());
		}

		return (temp);
	}

	@Test
	public void test() throws URISyntaxException, IOException {
		File dir = createTempDirectory();

		new File(dir, "__dataset.txt").createNewFile();

		IBaseDataContainer baseDataContainer = new FileBaseDataContainer(dir);
		File file = baseDataContainer.getFile("test", "my_name");
		assertFalse(file.exists());

		FileUtils.write(file, "Hello World!", "UTF-8");
		File file2 = baseDataContainer.getFile("test", "my_name");
		assertFalse(file2.exists());
		assertNotSame(file, file2);

		baseDataContainer.putFile("test", "my_name", file);
		File file3 = baseDataContainer.getFile("test", "my_name");
		assertEquals("Hello World!", FileUtils.readFileToString(file3, "UTF-8"));

		baseDataContainer.putFile("test", "my_name", null);
		File file4 = baseDataContainer.getFile("test", "my_name");
		assertFalse(file4.exists());
	}
}
