package de.fu_berlin.imp.seqan.usability_analyzer.entity.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.extensionProviders.IDataSourceFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.filters.DataSourceFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.preferences.SUAEntityPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityTableViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.UsabilityLogContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows de.fu_berlin.imp.seqan.usability_analyzer.doclog.data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class EntityView extends ViewPart implements IDataSourceFilterListener,
		IDateRangeListener {
	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView";

	public static class Factory implements IExecutableExtensionFactory {
		@Override
		public Object create() throws CoreException {
			IViewReference[] allviews = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getViewReferences();
			for (IViewReference viewReference : allviews) {
				if (viewReference.getId().equals(ID))
					return viewReference.getView(true);
			}
			return null;
		}
	}

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private EntityTableViewer entityTableViewer;
	private Label status;

	private ISelectionListener postSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		}
	};

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (preferenceUtil.logfilePathChanged(event))
				entityTableViewer.setInput(preferenceUtil.getLogfilePath());
		}
	};

	private Map<DataSource, DataSourceFilter> dataSourceFilters;
	private DateRangeFilter dateRangeFilter = null;

	public EntityView() {
		this.dataSourceFilters = new HashMap<DataSource, DataSourceFilter>();
		this.dataSourceFilters.put(DataSource.DIFFS, new DataSourceFilter(
				DataSource.DIFFS));
		this.dataSourceFilters.put(DataSource.DOCLOG, new DataSourceFilter(
				DataSource.DOCLOG));
		this.dataSourceFilters.put(DataSource.SURVEYRECORD,
				new DataSourceFilter(DataSource.SURVEYRECORD));
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		preferenceUtil.addPropertyChangeListener(propertyChangeListener);
		SelectionUtils.getSelectionService().addPostSelectionListener(
				postSelectionListener);

	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				postSelectionListener);
		preferenceUtil.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		this.entityTableViewer = new EntityTableViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		final Table table = entityTableViewer.getTable();
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();
		this.entityTableViewer.sort(0);

		this.entityTableViewer
				.setContentProvider(new UsabilityLogContentProvider());
		this.entityTableViewer.setInput(Activator.getDefault()
				.getPersonManager());

		this.status = new Label(parent, SWT.BORDER);
		this.status.setLayoutData(GridDataFactory.fillDefaults().create());

		this.hookContextMenu();
		this.getSite().setSelectionProvider(entityTableViewer);

		applyFilters();
	}

	private void applyFilters() {
		for (DataSource dataSource : new SUAEntityPreferenceUtil()
				.getFilterdDataSources()) {
			dataSourceFilterChanged(dataSource, true);
		}
		this.dateRangeChanged(null, preferenceUtil.getDateRange());
	}

	private void createColumns() {
		this.entityTableViewer.createColumn("ID", 150).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						ID id = person.getId();
						return (id != null) ? id.toString() : "";
					}
				});

		this.entityTableViewer.createColumn("Fingerprints", 300)
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						List<Fingerprint> secondaryFingerprints = person
								.getFingerprints();
						return (secondaryFingerprints != null) ? StringUtils
								.join(secondaryFingerprints, ", ") : "";
					}
				});

		this.entityTableViewer.createColumn("Token", 45).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						Token token = person.getToken();
						return (token != null) ? token.toString() : "";

					}
				});

		this.entityTableViewer.createColumn("OS", 100).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						StatsFile statsFile = person.getStatsFile();
						return (statsFile != null) ? statsFile
								.getPlatformLong() : "";

					}
				});

		this.entityTableViewer.createColumn("Generator", 100).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						CMakeCacheFile cMakeCacheFile = person
								.getCMakeCacheFile();
						return (cMakeCacheFile != null) ? cMakeCacheFile
								.getGenerator() : "";

					}
				});

		this.entityTableViewer.createColumn("Earliest Entry", 180)
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						TimeZoneDate earliestDate = person
								.getEarliestEntryDate();
						return (earliestDate != null) ? earliestDate
								.format(preferenceUtil.getDateFormat()) : "";

					}
				});

		this.entityTableViewer.createColumn("Latest Entry", 180)
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						TimeZoneDate lastestDate = person.getLatestEntryDate();
						return (lastestDate != null) ? lastestDate
								.format(preferenceUtil.getDateFormat()) : "";
					}
				});
	}

	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				menuMgr.add(new GroupMarker(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuMgr.createContextMenu(entityTableViewer.getControl());
		entityTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, entityTableViewer);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		entityTableViewer.getControl().setFocus();
	}

	@Override
	public void dataSourceFilterChanged(DataSource dataSource, boolean isOn) {
		DataSourceFilter dataSourceFilter = this.dataSourceFilters
				.get(dataSource);

		if (isOn) {
			this.entityTableViewer.addFilter(dataSourceFilter);
		} else {
			this.entityTableViewer.removeFilter(dataSourceFilter);
		}

		updateStatus();
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			TimeZoneDateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.entityTableViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.entityTableViewer.addFilter(this.dateRangeFilter);

		updateStatus();
	}

	private void updateStatus() {
		int numEntries = entityTableViewer.getTable().getItems().length;
		this.status.setText(numEntries
				+ ((numEntries != 1) ? " entries" : " entry"));
	}
}