package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.File;

public class DataSourceManager {
	public DataSourceManager(File file) throws DataSourceInvalidException {
		if (file == null)
			throw new DataSourceInvalidException("No file provided",
					new NullPointerException());
		if (!file.isDirectory() && !file.isFile())
			throw new DataSourceInvalidException(
					"File is neither a directory nor a file");
		if (!file.canRead())
			throw new DataSourceInvalidException("File can't be read");
	}
}
