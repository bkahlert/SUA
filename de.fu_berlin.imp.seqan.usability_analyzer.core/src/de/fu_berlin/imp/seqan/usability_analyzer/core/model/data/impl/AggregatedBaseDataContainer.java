package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;

public class AggregatedBaseDataContainer extends AggregatedDataContainer
		implements IBaseDataContainer {

	private IDataSetInfo info;
	private List<? extends IBaseDataContainer> baseContainers;

	public AggregatedBaseDataContainer(
			List<? extends IBaseDataContainer> baseContainers) {
		super(baseContainers);

		this.baseContainers = baseContainers;

		List<IDataSetInfo> infos = new ArrayList<IDataSetInfo>();
		for (IBaseDataContainer baseContainer : baseContainers) {
			infos.add(baseContainer.getInfo());
		}

		final List<String> names = new ArrayList<String>();
		for (IBaseDataContainer container : baseContainers) {
			names.add(container.getInfo().getName());
		}

		final TimeZoneDateRange range = TimeZoneDateRange
				.calculateOuterDateRange(infos);

		this.info = new IDataSetInfo() {

			@Override
			public String getName() {
				return names.size() > 0 ? StringUtils.join(names, ", ")
						: "[EMPTY]";
			}

			@Override
			public TimeZoneDateRange getDateRange() {
				return range;
			}

			@Override
			public Map<String, String> getUnknownProperties() {
				return new HashMap<String, String>();
			}

		};
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this;
	}

	@Override
	public IDataSetInfo getInfo() {
		return this.info;
	}

	@Override
	public File getStaticFile(String scope, String name) throws IOException {
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			File staticFile = baseDataContainer.getStaticFile(scope, name);
			if (staticFile != null && staticFile.exists()) {
				return staticFile;
			}
		}
		return this.baseContainers.get(0).getStaticFile(scope, name);
	}

	@Override
	public void resetStaticFile(String scope, String name) throws IOException {
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			baseDataContainer.resetStaticFile(scope, name);
		}
	}

	@Override
	public File getFile(String scope, String name) throws IOException {
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			File file = baseDataContainer.getFile(scope, name);
			if (file.exists()) {
				return file;
			}
		}
		return this.baseContainers.get(0).getFile(scope, name);
	}

	/**
	 * Returns all files found under the given filename.
	 * <p>
	 * Multiple files can be found because this implementation encapsulates
	 * multiple containers.
	 * <p>
	 * If no file could be found, null is returned.
	 * 
	 * @param scope
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public List<File> getFiles(String scope, String name) throws IOException {
		List<File> files = new ArrayList<File>();
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			File file = baseDataContainer.getFile(scope, name);
			if (file.exists()) {
				files.add(file);
			}
		}
		return files.size() > 0 ? files : null;
	}

	@Override
	public void putFile(String scope, String name, File file)
			throws IOException {
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			baseDataContainer.putFile(scope, name, file);
		}
	}

	@Override
	public void deleteScope(String scope) {
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			baseDataContainer.deleteScope(scope);
		}
	}

	@Override
	public void dispose() {
		for (IBaseDataContainer baseContainer : this.baseContainers) {
			baseContainer.dispose();
		}
	}
}
