package de.fu_berlin.imp.seqan.usability_analyzer.core.ui;

import java.util.Arrays;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataDirectoriesService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;

public class DataDirectoriesContributionItem extends ContributionItem {

	private IDataDirectoriesService dataDirectoriesService;

	public DataDirectoriesContributionItem() {
		init();
	}

	public DataDirectoriesContributionItem(String id) {
		super(id);
		init();
	}

	private void init() {
		this.dataDirectoriesService = (IDataDirectoriesService) PlatformUI
				.getWorkbench().getService(IDataDirectoriesService.class);
	}

	@Override
	public void fill(Menu menu, int index) {
		for (final IBaseDataContainer dataResourceContainer : this.dataDirectoriesService
				.getDataDirectories()) {
			MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
			menuItem.setText(dataResourceContainer.toString());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ExecutorUtil.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							dataDirectoriesService
									.setActiveDataDirectories(Arrays
											.asList(dataResourceContainer));
						}
					});
				}
			});
		}
	}
}
