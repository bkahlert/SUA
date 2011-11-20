package de.fu_berlin.imp.seqan.usability_analyzer.person.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.person.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.person.extensionProviders.IDataSourceFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.person.filters.DataSourceFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.person.model.Person;
import de.fu_berlin.imp.seqan.usability_analyzer.person.preferences.SUAEntityPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.person.viewer.PersonTableViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.person.viewer.UsabilityLogContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
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

public class PersonView extends ViewPart implements IDataSourceFilterListener,
		IDateRangeListener {
	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.person.views.PersonView";

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
	private PersonTableViewer personTableViewer;
	private Label status;

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (preferenceUtil.logfilePathChanged(event))
				personTableViewer.setInput(preferenceUtil.getLogfilePath());
		}
	};

	private Map<DataSource, DataSourceFilter> dataSourceFilters;
	private DateRangeFilter dateRangeFilter = null;

	public PersonView() {
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
	}

	@Override
	public void dispose() {
		preferenceUtil.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		this.personTableViewer = new PersonTableViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		final Table table = personTableViewer.getTable();
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();
		this.personTableViewer.sort(0);

		this.personTableViewer
				.setContentProvider(new UsabilityLogContentProvider());
		this.personTableViewer.setInput(Activator.getDefault()
				.getPersonManager());

		this.status = new Label(parent, SWT.BORDER);
		this.status.setLayoutData(GridDataFactory.fillDefaults().create());

		this.hookContextMenu();
		this.getSite().setSelectionProvider(personTableViewer);

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
		this.personTableViewer.createColumn("ID", 150).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						ID id = person.getId();
						return (id != null) ? id.toString() : "";
					}
				});

		this.personTableViewer.createColumn("Fingerprints", 300)
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						List<Fingerprint> secondaryFingerprints = person
								.getFingerprints();
						return (secondaryFingerprints != null) ? StringUtils
								.join(secondaryFingerprints, ", ") : "";
					}
				});

		this.personTableViewer.createColumn("Token", 45).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						Token token = person.getToken();
						return (token != null) ? token.toString() : "";

					}
				});

		this.personTableViewer.createColumn("OS", 100).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						StatsFile statsFile = person.getStatsFile();
						return (statsFile != null) ? statsFile
								.getPlatformLong() : "";

					}
				});

		this.personTableViewer.createColumn("Generator", 100).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						CMakeCacheFile cMakeCacheFile = person
								.getCMakeCacheFile();
						return (cMakeCacheFile != null) ? cMakeCacheFile
								.getGenerator() : "";

					}
				});

		this.personTableViewer.createColumn("Earliest Entry", 180)
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						LocalDate earliestDate = person.getEarliestEntryDate();
						return (earliestDate != null) ? earliestDate
								.format(preferenceUtil.getDateFormat()) : "";

					}
				});

		this.personTableViewer.createColumn("Latest Entry", 180)
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Person person = (Person) element;
						LocalDate lastestDate = person.getLatestEntryDate();
						return (lastestDate != null) ? lastestDate
								.format(preferenceUtil.getDateFormat()) : "";
					}
				});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

			}
		});
		Menu menu = menuMgr.createContextMenu(personTableViewer.getControl());
		personTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, personTableViewer);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		personTableViewer.getControl().setFocus();
	}

	@Override
	public void dataSourceFilterChanged(DataSource dataSource, boolean isOn) {
		DataSourceFilter dataSourceFilter = this.dataSourceFilters
				.get(dataSource);

		if (isOn) {
			this.personTableViewer.addFilter(dataSourceFilter);
		} else {
			this.personTableViewer.removeFilter(dataSourceFilter);
		}

		updateStatus();
	}

	@Override
	public void dateRangeChanged(LocalDateRange oldDateRange,
			LocalDateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.personTableViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.personTableViewer.addFilter(this.dateRangeFilter);

		updateStatus();
	}

	private void updateStatus() {
		int numEntries = personTableViewer.getTable().getItems().length;
		this.status.setText(numEntries
				+ ((numEntries != 1) ? " entries" : " entry"));
	}
}