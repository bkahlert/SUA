package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class FileUtils {
	/**
	 * Gets the {@link File} that is described by the passed class's location
	 * and the appended filename.
	 * 
	 * @param clazz
	 * @param filename
	 * @return
	 * @throws URISyntaxException
	 */
	public static File getFile(Class<?> clazz, String filename)
			throws URISyntaxException {
		URL url = clazz.getResource(filename);
		URI uri = url.toURI();
		String path = uri.toString().substring(uri.getScheme().length() + 1);
		return new File(path);
	}

	/**
	 * Gets the {@link File} that is described by the passed class's location
	 * relative to the bin directory.
	 * 
	 * @param filename
	 * @return
	 * @throws URISyntaxException
	 */
	public static File getFile(String filename) throws URISyntaxException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, num = FileUtils.class.getPackage().getName()
				.split("\\.").length; i < num; i++)
			sb.append("../");
		return getFile(FileUtils.class, sb.toString() + filename);
	}
}
