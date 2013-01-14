package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;

public class FileData implements IData {

	private static Logger LOGGER = Logger.getLogger(FileData.class);

	private IBaseDataContainer baseDataContainer;
	private IDataContainer dataContainer;
	private File file;

	public FileData(IBaseDataContainer baseDataContainer,
			IDataContainer parentDataContainer, File file) {
		this(file);

		Assert.isNotNull(baseDataContainer);
		Assert.isNotNull(parentDataContainer);

		IDataContainer tmp = parentDataContainer;
		while (tmp.getParentDataContainer() != null)
			tmp = tmp.getParentDataContainer();
		Assert.isTrue(tmp == baseDataContainer);

		this.baseDataContainer = baseDataContainer;
		this.dataContainer = parentDataContainer;
	}

	public FileData(File file) {
		this.file = file;
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this.baseDataContainer;
	}

	@Override
	public IDataContainer getParentDataContainer() {
		return this.dataContainer;
	}

	@Override
	public String getName() {
		return this.file.getName();
	}

	@Override
	public String read() {
		try {
			return org.apache.commons.io.FileUtils.readFileToString(this.file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String read(long from, long to) {
		try {
			return new String(
					de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils
							.readBytesFromTo(this.file, from, to));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String readFirstLine() {
		String lineData = "";
		try {
			RandomAccessFile inFile = new RandomAccessFile(this.file, "r");
			lineData = inFile.readLine();
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return lineData;
	}

	@Override
	public String readLastLines(int numLines) {
		try {
			RandomAccessFile fileHandler = new RandomAccessFile(this.file, "r");
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

	@Override
	public Iterator<String> iterator() {
		try {
			return new Iterator<String>() {

				private FileInputStream fstream;
				private BufferedReader br;
				private String line = null;

				{
					this.fstream = new FileInputStream(FileData.this.file);
					this.br = new BufferedReader(new InputStreamReader(
							new DataInputStream(fstream)));
					next();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public String next() {
					String rt = this.line;
					try {
						this.line = br.readLine();
					} catch (IOException e) {
						if (fstream != null)
							try {
								fstream.close();
							} catch (IOException e1) {
							}
						throw new RuntimeException(e);
					}
					if (this.line == null) {
						if (fstream != null)
							try {
								fstream.close();
							} catch (IOException e1) {
							}
					}
					return rt;
				}

				@Override
				public boolean hasNext() {
					return this.line != null;
				}
			};
		} catch (FileNotFoundException e) {
			LOGGER.error("Could not read " + this.file);
			return null;
		}
	}

	@Override
	public long getLength() {
		return this.file.length();
	}

	@Override
	public File getStaticFile() throws IOException {
		List<String> path = new LinkedList<String>();
		path.add(getName());
		IDataContainer container = this.getParentDataContainer();
		while (container != null) {
			path.add(container.getName());
			container = container.getParentDataContainer();
		}
		// remove base
		path.remove(path.size() - 1);

		String scope = path.remove(path.size() - 1);
		String name = StringUtils.join(new ReverseListIterator(path),
				File.separator);
		return this.getBaseDataContainer().getStaticFile(scope, name);
	}

	@Override
	public String toString() {
		return this.file.getAbsolutePath()
				+ " @ "
				+ ((this.baseDataContainer == null) ? "null"
						: this.baseDataContainer.toString());
	}
}
