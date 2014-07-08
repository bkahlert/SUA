package de.fu_berlin.imp.apiua.entity.extensionProviders;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import de.fu_berlin.imp.apiua.core.model.DataSource;

public class DataSourceFilterUtil {
	public static void notifyDataSourceFilterChanged(DataSource dataSource,
			boolean isOn) {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.apiua.entity.datasource");
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof IDataSourceFilterListener) {
					IDataSourceFilterListener dataSourceFilterListener = (IDataSourceFilterListener) o;
					dataSourceFilterListener.dataSourceFilterChanged(
							dataSource, isOn);
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
	}
}
