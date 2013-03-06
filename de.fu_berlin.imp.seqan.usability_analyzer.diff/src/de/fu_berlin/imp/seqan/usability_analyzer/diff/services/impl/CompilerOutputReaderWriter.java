package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Instances of this class are responsible for reading and writing compiler
 * outputs from and to {@link File}s.
 * 
 * @author bkahlert
 * 
 */
public class CompilerOutputReaderWriter {
	/**
	 * Load the compiler output from the given file.
	 * 
	 * @param compilerOutputFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static String fromFile(File compilerOutputFile)
			throws FileNotFoundException, IOException {
		if (compilerOutputFile == null)
			return "";
		return FileUtils.readFileToString(compilerOutputFile, "UTF-8");
	}

	/**
	 * Writes the compiler output to a temporary file.
	 * 
	 * @param html
	 * @return null if the html is null or empty
	 * @throws IOException
	 */
	static File toFile(String html) throws IOException {
		File compilerOutputFile = File.createTempFile("compiler_output",
				".html");
		compilerOutputFile.deleteOnExit();
		if ((html == null || html.trim().equals(""))
				&& compilerOutputFile.exists()) {
			compilerOutputFile.delete();
			compilerOutputFile = null;
		} else {
			FileUtils.writeStringToFile(compilerOutputFile, html, "UTF-8");
		}
		return compilerOutputFile;
	}
}
