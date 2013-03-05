package de.fu_berlin.imp.seqan.usability_analyzer.core.ui;

import java.util.Arrays;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;

public class DataDirectoriesContributionItem extends ContributionItem {

	private IDataService dataService;

	public DataDirectoriesContributionItem() {
		init();
	}

	public DataDirectoriesContributionItem(String id) {
		super(id);
		init();
	}

	private void init() {
		this.dataService = (IDataService) PlatformUI
				.getWorkbench().getService(IDataService.class);
	}

	@Override
	public void fill(Menu menu, int index) {
		for (final IBaseDataContainer dataResourceContainer : this.dataService
				.getDataDirectories()) {
			MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
			menuItem.setText(dataResourceContainer.toString());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ExecutorUtil.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							dataService
									.loadDataDirectories(Arrays
											.asList(dataResourceContainer));
						}
					});
				}
			});
		}
	}
}
