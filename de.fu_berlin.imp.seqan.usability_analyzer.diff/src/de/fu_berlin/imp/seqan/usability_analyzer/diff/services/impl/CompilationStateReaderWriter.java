package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;

/**
 * Instances of this class are responsible for reading and writing compilation
 * states from and to {@link File}s.
 * 
 * @author bkahlert
 * 
 */
public class CompilationStateReaderWriter {
	/**
	 * Load the compilation states from the given file.
	 * 
	 * @param compilationStateFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static Map<URI, Boolean> fromFile(File compilationStateFile)
			throws FileNotFoundException, IOException {
		Map<URI, Boolean> compilationStates = new HashMap<URI, Boolean>();
		if (compilationStateFile == null) {
			return compilationStates;
		}

		Properties properties = new Properties();
		properties.load(new FileReader(compilationStateFile));
		for (Entry<Object, Object> entry : properties.entrySet()) {
			try {
				URI uri = new URI(entry.getKey().toString());
				String value = entry.getValue().toString();
				if (value.equalsIgnoreCase("null")) {
					compilationStates.put(uri, null);
				} else if (value.equalsIgnoreCase("true")) {
					compilationStates.put(uri, true);
				} else if (value.equalsIgnoreCase("false")) {
					compilationStates.put(uri, false);
				} else {
					CompilationService.LOGGER.warn(compilationStateFile + ":"
							+ uri + " contains invalid compilation state "
							+ value);
				}
			} catch (Exception e) {
				CompilationService.LOGGER.warn(compilationStateFile
						+ " processing error", e);
			}
		}
		return compilationStates;
	}

	/**
	 * Writes the compilation states to a temporary file.
	 * 
	 * @param compilationStates
	 * @return
	 * @throws IOException
	 */
	static File toFile(Map<URI, Boolean> compilationStates) throws IOException {
		Assert.isNotNull(compilationStates);
		Properties properties = new Properties();
		for (Entry<URI, Boolean> entry : compilationStates.entrySet()) {
			String key = entry.getKey().toString();
			String value = "null";
			if (Boolean.TRUE.equals(entry.getValue())) {
				value = "true";
			} else if (Boolean.FALSE.equals(entry.getValue())) {
				value = "false";
			}
			properties.put(key, value);
		}

		File compilationStateFile = File.createTempFile("compilation_states",
				".properties");
		compilationStateFile.deleteOnExit();
		OutputStream out = new FileOutputStream(compilationStateFile);
		properties.store(out, "This file contains the compilations states of "
				+ ICompilable.class.getSimpleName() + "s.");
		return compilationStateFile;
	}
}
