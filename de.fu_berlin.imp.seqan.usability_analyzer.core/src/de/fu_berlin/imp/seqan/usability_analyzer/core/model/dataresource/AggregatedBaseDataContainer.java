package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class AggregatedBaseDataContainer extends AggregatedDataContainer
		implements IBaseDataContainer {

	private IDataSetInfo info;
	private List<? extends IBaseDataContainer> baseContainers;

	public AggregatedBaseDataContainer(
			List<? extends IBaseDataContainer> containers) {
		super(null, containers);

		this.baseContainers = containers;

		List<IDataSetInfo> infos = new ArrayList<IDataSetInfo>();
		for (IBaseDataContainer container : containers) {
			infos.add(container.getInfo());
		}

		final List<String> names = new ArrayList<String>();
		for (IBaseDataContainer container : containers) {
			names.add(container.getInfo().getName());
		}

		final TimeZoneDateRange range = TimeZoneDateRange
				.calculateOuterDateRange(infos);

		this.info = new IDataSetInfo() {

			@Override
			public String getName() {
				return StringUtils.join(names, ", ");
			}

			@Override
			public TimeZoneDateRange getDateRange() {
				return range;
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
	public File getFile(String scope, String filename) throws IOException {
		return this.baseContainers.get(0).getFile(scope, filename);
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
}
