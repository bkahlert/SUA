package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;

public class FileDataContainer implements IDataContainer {

	private IBaseDataContainer baseDataContainer;
	private IDataContainer parentDataContainer;
	private File file;

	public FileDataContainer(IBaseDataContainer baseDataContainer,
			IDataContainer parentDataContainer, File file) {
		this(file);

		Assert.isNotNull(baseDataContainer);
		Assert.isNotNull(parentDataContainer);

		IDataContainer tmp = parentDataContainer;
		while (tmp.getParentDataContainer() != null)
			tmp = tmp.getParentDataContainer();
		Assert.isTrue(tmp == baseDataContainer);

		this.baseDataContainer = baseDataContainer;
		this.parentDataContainer = parentDataContainer;
	}

	protected FileDataContainer(File file) {
		Assert.isNotNull(file);
		Assert.isTrue(file.isDirectory());
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
	public IDataContainer getParentDataContainer() {
		return this.parentDataContainer;
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
		return new FileData(getBaseDataContainer(), this, new File(this.file,
				name));
	}

	@Override
	public List<IData> getResources() {
		List<IData> datas = new ArrayList<IData>();
		for (File file : this.file.listFiles()) {
			if (file.isFile())
				datas.add(new FileData(getBaseDataContainer(), this, file));
		}
		return datas;
	}

	@Override
	public IDataContainer getSubContainer(String name) {
		return new FileDataContainer(getBaseDataContainer(), this, new File(
				this.file, name));
	}

	@Override
	public List<IDataContainer> getSubContainers() {
		List<IDataContainer> dataContainers = new ArrayList<IDataContainer>();
		for (File file : this.file.listFiles()) {
			if (file.isDirectory())
				dataContainers.add(new FileDataContainer(
						getBaseDataContainer(), this, file));
		}
		return dataContainers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FileDataContainer))
			return false;
		FileDataContainer other = (FileDataContainer) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

}
