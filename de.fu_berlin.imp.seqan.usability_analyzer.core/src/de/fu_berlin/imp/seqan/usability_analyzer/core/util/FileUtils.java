package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;

import org.junit.Assert;

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
			fileHandler.close();
			return lastLine;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] readBytesFromTo(File file, long from, long to) {
		long fileLength = file.length();
		Assert.assertTrue(from <= fileLength);
		Assert.assertTrue(to <= fileLength);
		Assert.assertTrue(from <= to);
		byte[] bytes = new byte[(int) (to - from)];
		try {
			RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
			fileHandler.seek(from);
			fileHandler.readFully(bytes);
			fileHandler.close();
			return bytes;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte getNewlineLengthAt(File file, long position) {
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(position);
			byte[] separator = new byte[2];
			raf.read(separator, 0, 2);
			raf.close();
			if (separator[0] == 0x0A) { // LF / \n
				return 1;
			} else if (separator[0] == 0x0D) { // CR / \r
				if (separator[1] == 0x0A)
					return 2; // CR+LF
				else
					return 1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			throw new InvalidParameterException(file.getName()
					+ " cannot be read");
		}
	}
}
