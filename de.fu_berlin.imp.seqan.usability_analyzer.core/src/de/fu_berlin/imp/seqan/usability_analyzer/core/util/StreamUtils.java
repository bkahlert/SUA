package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class StreamUtils {
	public static String convertInputStreamToString(InputStream is,
			String encoding) {
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, encoding);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}
}
