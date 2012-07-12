package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.File;

public class DataSourceManager {

	private File file;

	public DataSourceManager(File file) throws DataSourceInvalidException {
		if (file == null)
			throw new DataSourceInvalidException("No file provided",
					new NullPointerException());
		if (!file.isDirectory() && !file.isFile())
			throw new DataSourceInvalidException(
					"File is neither a directory nor a file: " + file);
		if (!file.canRead())
			throw new DataSourceInvalidException("File can't be read: " + file);

		this.file = file;
	}

	public File getFile() {
		return file;
	}
}
