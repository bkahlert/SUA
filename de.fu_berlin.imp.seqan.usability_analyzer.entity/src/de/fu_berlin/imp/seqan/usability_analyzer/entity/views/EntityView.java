package de.fu_berlin.imp.seqan.usability_analyzer.entity.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.DataServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.extensionProviders.IDataSourceFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.filters.DataSourceFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.preferences.SUAEntityPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class EntityView extends ViewPart implements IDataSourceFilterListener,
		IDateRangeListener {
	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView";
	public static final Logger LOGGER = Logger.getLogger(EntityView.class);

	public static class Factory implements IExecutableExtensionFactory {
		@Override
		public Object create() throws CoreException {
			IViewReference[] allViews;
			try {
				allViews = ExecutorUtil
						.syncExec(new Callable<IViewReference[]>() {
							@Override
							public IViewReference[] call() throws Exception {
								return PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage().getViewReferences();
							}
						});
			} catch (Exception e) {
				LOGGER.error("Error enumerating present views");
				return null;
			}

			for (IViewReference viewReference : allViews) {
				if (viewReference.getId().equals(ID))
					return viewReference.getView(true);
			}
			return null;
		}
	}

	private IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> dataContainers) {
			LOGGER.info("Refreshing " + EntityViewer.class.getSimpleName());
			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					entityViewer.setInput(Activator.getDefault()
							.getLoadedData());
				}
			});
		}
	};

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private EntityViewer entityViewer;
	private Label status;

	private Map<DataSource, DataSourceFilter> dataSourceFilters;
	private DateRangeFilter dateRangeFilter = null;
	private IDataService dataService = (IDataService) PlatformUI.getWorkbench()
			.getService(IDataService.class);

	public EntityView() {
		this.dataSourceFilters = new HashMap<DataSource, DataSourceFilter>();
		this.dataSourceFilters.put(DataSource.DIFFS, new DataSourceFilter(
				DataSource.DIFFS));
		this.dataSourceFilters.put(DataSource.DOCLOG, new DataSourceFilter(
				DataSource.DOCLOG));
		this.dataSourceFilters.put(DataSource.SURVEYRECORD,
				new DataSourceFilter(DataSource.SURVEYRECORD));

		dataService.addDataServiceListener(dataServiceListener);
	}

	@Override
	public void dispose() {
		dataService.removeDataServiceListener(dataServiceListener);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		// IEclipseContext context = parentContext.createChild();
		// context.set(Table.class, new Table(parent, SWT.MULTI | SWT.H_SCROLL
		// | SWT.V_SCROLL | SWT.BORDER));

		// this.entityViewer = ContextInjectionFactory.make(EntityViewer.class,
		// context);
		// this.entityViewer.setContentProvider(ContextInjectionFactory.make(
		// EntityContentProvider.class, context));
		this.entityViewer = new EntityViewer(new Table(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));
		this.entityViewer.setContentProvider(new EntityContentProvider());

		this.status = new Label(parent, SWT.BORDER);
		this.status.setLayoutData(GridDataFactory.fillDefaults().create());

		new ContextMenu(this.entityViewer, this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return "de.fu_berlin.imp.seqan.usability_analyzer.core.commands.startWorkSession";
			}
		};

		applyFilters();
	}

	private void applyFilters() {
		for (DataSource dataSource : new SUAEntityPreferenceUtil()
				.getFilterdDataSources()) {
			dataSourceFilterChanged(dataSource, true);
		}
		this.dateRangeChanged(null, preferenceUtil.getDateRange());
	}

	public EntityViewer getEntityTableViewer() {
		return entityViewer;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		entityViewer.getControl().setFocus();
	}

	@Override
	public void dataSourceFilterChanged(DataSource dataSource, boolean isOn) {
		DataSourceFilter dataSourceFilter = this.dataSourceFilters
				.get(dataSource);

		if (isOn) {
			this.entityViewer.addFilter(dataSourceFilter);
		} else {
			this.entityViewer.removeFilter(dataSourceFilter);
		}

		updateStatus();
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			final TimeZoneDateRange newDateRange) {
		if (this.dateRangeFilter != null) {
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					entityViewer.removeFilter(dateRangeFilter);
					dateRangeFilter = new DateRangeFilter(newDateRange);
					entityViewer.addFilter(dateRangeFilter);
					updateStatus();
				}
			});
		}
	}

	private void updateStatus() {
		int numEntries = entityViewer.getTable().getItems().length;
		this.status.setText(numEntries
				+ ((numEntries != 1) ? " entries" : " entry"));
		IStatusLineManager manager = getViewSite().getActionBars()
				.getStatusLineManager();
		manager.setMessage(numEntries
				+ ((numEntries != 1) ? " entries" : " entry"));
	}
}