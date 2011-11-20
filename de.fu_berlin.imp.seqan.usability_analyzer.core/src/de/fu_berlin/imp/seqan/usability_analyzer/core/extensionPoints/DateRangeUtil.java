package de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class DateRangeUtil {
	public static void notifyDataSourceFilterChanged(TimeZoneDateRange oldDateRange,
			TimeZoneDateRange newDateRange) {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.core.daterange");
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof IDateRangeListener) {
					IDateRangeListener dataSourceFilterListener = (IDateRangeListener) o;
					dataSourceFilterListener.dateRangeChanged(oldDateRange,
							newDateRange);
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
	}
}
