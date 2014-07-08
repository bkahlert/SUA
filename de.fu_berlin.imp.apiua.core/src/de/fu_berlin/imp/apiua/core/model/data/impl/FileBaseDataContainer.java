package de.fu_berlin.imp.apiua.core.model.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.core.model.DataSetInfo;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IDataSetInfo;

public class FileBaseDataContainer extends FileDataContainer implements
		IBaseDataContainer {

	public static final Logger LOGGER = Logger
			.getLogger(FileBaseDataContainer.class);

	private List<File> returnedFiles;
	private IDataSetInfo info;

	protected FileBaseDataContainer(File file, boolean expectDataSetInfo) {
		super(file);
		this.returnedFiles = new ArrayList<File>();
		this.info = expectDataSetInfo ? new DataSetInfo(
				this.getResource("__dataset.txt")) : null;
	}

	public FileBaseDataContainer(File file) {
		this(file, true);
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this;
	}

	@Override
	public IDataContainer getParentDataContainer() {
		return null;
	}

	@Override
	public IDataSetInfo getInfo() {
		return this.info;
	}

	@Override
	public IDataContainer getSubContainer(String name) {
		File directory = new File(this.getFile(), name);
		if (!directory.isDirectory()) {
			return null;
		}
		return new FileDataContainer(this, this, directory);
	}

	protected File getTempDirectory() {
		File tmp = new File(this.getFile(), "tmp");
		if (!tmp.exists()) {
			tmp.mkdirs();
		}
		return tmp;
	}

	protected File getScope(String scope) {
		if (scope == null || scope.isEmpty()) {
			return this.getFile();
		}
		File scopeDir = new File(this.getFile(), scope);
		scopeDir.mkdirs();
		return scopeDir;
	}

	protected File getLocation(String scope, String name) {
		File scopeDir = this.getScope(scope);
		return new File(scopeDir, name);
	}

	@Override
	public File getStaticFile(String scope, String name) throws IOException {
		File file = this.getLocation(scope, name);
		if (!file.exists()) {
			return null;
		}

		File staticFile = new File(new File(new File(this.getTempDirectory(),
				"static-files"), scope), name);
		if (!staticFile.exists()
				|| staticFile.lastModified() != file.lastModified()) {
			FileUtils.copyFile(file, staticFile);
		}

		return staticFile;
	}

	@Override
	public void resetStaticFile(String scope, String name) throws IOException {
		File staticFile = this.getStaticFile(scope, name);
		if (staticFile != null && staticFile.exists()) {
			staticFile.delete();
		}
	}

	@Override
	public File getFile(String scope, String name) throws IOException {
		File file = this.getLocation(scope, name);

		File tmpFile = File.createTempFile("sua-tmp-",
				FilenameUtils.getName(name));
		tmpFile.delete();
		if (file.exists()) {
			FileUtils.copyFile(file, tmpFile);
		}
		this.returnedFiles.add(tmpFile);
		return tmpFile;
	}

	@Override
	public void putFile(String scope, String name, File file)
			throws IOException {
		if (file == null) {
			this.getLocation(scope, name).delete();
		} else {
			if (file.exists()) {
				FileUtils.copyFile(file, this.getLocation(scope, name));
			}
		}
		this.resetStaticFile(scope, name);
	}

	@Override
	public void deleteScope(String scope) {
		try {
			FileUtils.deleteDirectory(this.getScope(scope));
		} catch (IOException e) {
			LOGGER.error("Error deleting scope \"" + scope + "\"");
		}
	}

	@Override
	public String toString() {
		return this.getFile().getAbsolutePath();
	}

	@Override
	public void dispose() {
		for (File returnedFile : this.returnedFiles) {
			if (returnedFile != null && returnedFile.exists()) {
				returnedFile.delete();
			}
		}
	}

}
