package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSetInfo;

public class FileBaseDataContainer extends FileDataContainer implements
		IBaseDataContainer {

	public static final Logger LOGGER = Logger
			.getLogger(FileBaseDataContainer.class);

	private IDataSetInfo info;

	public FileBaseDataContainer(File file) {
		super(null, file);
		this.info = new DataSetInfo(new File(file, DataSetInfo.FILENAME));
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this;
	}

	@Override
	public IDataSetInfo getInfo() {
		return this.info;
	}

	protected File getScope(String scope) {
		File scopeDir = new File(this.getFile(), scope);
		scopeDir.mkdirs();
		return scopeDir;
	}

	protected File getLocation(String scope, String name) {
		File scopeDir = getScope(scope);
		return new File(scopeDir, name);
	}

	@Override
	public File getFile(String scope, String name) throws IOException {
		File scopeDir = new File(this.getFile(), scope);
		scopeDir.mkdirs();
		File file = new File(scopeDir, name);

		File tmpFile = File.createTempFile("sua", ".tmp");
		tmpFile.delete();
		if (file.exists())
			FileUtils.copyFile(file, tmpFile);
		return tmpFile;
	}

	@Override
	public void putFile(String scope, String name, File file)
			throws IOException {
		if (file == null) {
			getLocation(scope, name).delete();
		} else {
			if (file.exists())
				FileUtils.copyFile(file, getLocation(scope, name));
		}
	}

	@Override
	public void deleteScope(String scope) {
		try {
			FileUtils.deleteDirectory(getScope(scope));
		} catch (IOException e) {
			LOGGER.error("Error deleting scope \"" + scope + "\"");
		}
	}

}
