package de.fu_berlin.imp.seqan.usability_analyzer.person.views.controls;

import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.person.extensionProviders.DataSourceFilterUtil;

abstract public class DataOnlyPersonsButton extends
		WorkbenchWindowControlContribution {

	public DataOnlyPersonsButton() {

	}

	public DataOnlyPersonsButton(String id) {
		super(id);

	}

	public void setDataSourceFilter(DataSource dataSource, boolean isOn) {
		DataSourceFilterUtil.notifyDataSourceFilterChanged(dataSource, isOn);
	}

}
