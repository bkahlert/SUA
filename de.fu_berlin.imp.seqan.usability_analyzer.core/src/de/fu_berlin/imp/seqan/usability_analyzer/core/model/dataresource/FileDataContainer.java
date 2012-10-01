package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class FileDataContainer implements IDataContainer {

	private IBaseDataContainer baseDataContainer;
	private File file;

	public FileDataContainer(IBaseDataContainer baseDataContainer, File file) {
		Assert.isNotNull(file);
		Assert.isTrue(file.isDirectory());
		this.baseDataContainer = baseDataContainer;
		this.file = file;
	}

	@Override
	public String toString() {
		return this.file.getAbsolutePath()
				+ " @ "
				+ ((this.baseDataContainer == null) ? "null"
						: this.baseDataContainer.toString());
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this.baseDataContainer;
	}

	@Override
	public String getName() {
		return this.file.getName();
	}

	public File getFile() {
		return file;
	}

	@Override
	public IData getResource(String name) {
		return new FileData(this.baseDataContainer, new File(this.file, name));
	}

	@Override
	public List<IData> getResources() {
		List<IData> datas = new ArrayList<IData>();
		for (File file : this.file.listFiles()) {
			if (file.isFile())
				datas.add(new FileData(this.baseDataContainer, file));
		}
		return datas;
	}

	@Override
	public IDataContainer getSubContainer(String name) {
		return new FileDataContainer(this.baseDataContainer, new File(
				this.file, name));
	}

	@Override
	public List<IDataContainer> getSubContainers() {
		List<IDataContainer> dataContainers = new ArrayList<IDataContainer>();
		for (File file : this.file.listFiles()) {
			if (file.isDirectory())
				dataContainers.add(new FileDataContainer(
						this.baseDataContainer, file));
		}
		return dataContainers;
	}

}
