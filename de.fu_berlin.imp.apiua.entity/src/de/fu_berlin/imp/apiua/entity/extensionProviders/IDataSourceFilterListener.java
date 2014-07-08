package de.fu_berlin.imp.apiua.entity.extensionProviders;

import de.fu_berlin.imp.apiua.core.model.DataSource;

public interface IDataSourceFilterListener {
	public void dataSourceFilterChanged(DataSource dataSource, boolean isOn);
}
