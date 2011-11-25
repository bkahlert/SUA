package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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

	public static String readFirstLine(File file) {
		String lineData = "";
		try {
			RandomAccessFile inFile = new RandomAccessFile(file, "r");
			lineData = inFile.readLine();
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return lineData;
	}

	public static String readLastLines(File file, int numLines) {
		try {
			RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
			long fileLength = file.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					line++;
					if (line >= numLines) {
						if (filePointer == fileLength) {
							continue;
						} else {
							break;
						}
					}
				}
				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
