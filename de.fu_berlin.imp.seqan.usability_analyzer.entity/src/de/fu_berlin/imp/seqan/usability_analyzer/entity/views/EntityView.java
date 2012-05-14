package de.fu_berlin.imp.seqan.usability_analyzer.entity.views;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.extensionProviders.IDataSourceFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.filters.DataSourceFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.preferences.SUAEntityPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityTableContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer.EntityTableViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows de.fu_berlin.imp.seqan.usability_analyzer.doclog.data obtained from the
 * model. The sample creates a dummy model on the fly, but a real implementation
 * would connect to the model available either in this or another plug-in (e.g.
 * the workspace). The view is connected to the model using a content provider.
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
	public static final Logger LOGGER = Logger.getLogger(EntityView.class);

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
		SelectionUtils.getSelectionService().addPostSelectionListener(
				postSelectionListener);

	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				postSelectionListener);
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

		this.entityTableViewer
				.setContentProvider(new EntityTableContentProvider());
		this.entityTableViewer.setInput(Activator.getDefault()
				.getPersonManager());

		this.status = new Label(parent, SWT.BORDER);
		this.status.setLayoutData(GridDataFactory.fillDefaults().create());

		new ContextMenu(this.entityTableViewer, this.getSite()) {
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

	public EntityTableViewer getEntityTableViewer() {
		return entityTableViewer;
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
		IStatusLineManager manager = getViewSite().getActionBars()
				.getStatusLineManager();
		manager.setMessage(numEntries
				+ ((numEntries != 1) ? " entries" : " entry"));
	}
}