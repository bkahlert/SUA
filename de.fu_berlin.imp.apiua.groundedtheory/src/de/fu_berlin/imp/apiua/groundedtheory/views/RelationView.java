package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.selection.ArrayUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IHighlightService;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer.Filterable;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer.QuickSelectionMode;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer.ShowInstances;
import de.ralfebert.rcputils.menus.ContextMenu;

public class RelationView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.RelationView";

	private static final Logger LOGGER = Logger.getLogger(RelationView.class);

	private final IHighlightService highlightService = (IHighlightService) PlatformUI
			.getWorkbench().getService(IHighlightService.class);
	private Job highlighter = null;
	private RelationViewer relationViewer;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.relationViewer = new RelationViewer(parent, SWT.NONE,
				ShowInstances.ON, RelationView.class.getName(), Filterable.ON,
				QuickSelectionMode.OFF);
		this.relationViewer
				.addSelectionChangedListener(event -> {
					if (RelationView.this.highlightService == null
							|| RelationView.this.getSite().getWorkbenchWindow()
									.getActivePage().getActivePart() != RelationView.this) {
						return;
					}

					if (RelationView.this.highlighter != null) {
						RelationView.this.highlighter.cancel();
					}
					if (!(event.getSelection() instanceof IStructuredSelection)) {
						return;
					}

					RelationView.this.highlighter = new NamedJob(
							RelationView.class, "Highlight") {
						@Override
						protected IStatus runNamed(IProgressMonitor monitor) {
							List<URI> uris = ArrayUtils.getAdaptableObjects(
									((ITreeSelection) event.getSelection())
											.toArray(), URI.class);

							SubMonitor subMonitor = SubMonitor.convert(monitor,
									uris.size() * 2);
							if (subMonitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							List<ICodeInstance> codeInstances;
							try {
								codeInstances = LocatorService.INSTANCE
										.resolve(
												uris,
												ICodeInstance.class,
												subMonitor.newChild(uris.size()))
										.get();
							} catch (Exception e) {
								LOGGER.error(
										"Error resolving "
												+ StringUtils.join(uris, ", "),
										e);
								return Status.CANCEL_STATUS;
							}
							if (subMonitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							List<CalendarRange> ranges = new ArrayList<CalendarRange>();
							for (ICodeInstance codeInstance : codeInstances) {
								ILocatable locatable;
								try {
									locatable = LocatorService.INSTANCE
											.resolve(codeInstance.getId(),
													subMonitor.newChild(1))
											.get();
								} catch (Exception e) {
									LOGGER.error("Error resolving "
											+ codeInstance.getId(), e);
									continue;
								}
								if (locatable instanceof HasDateRange) {
									ranges.add(((HasDateRange) locatable)
											.getDateRange().getCalendarRange());
								}
								if (subMonitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
							}
							if (ranges.size() > 0) {
								RelationView.this.highlightService.highlight(
										RelationView.class,
										ranges.toArray(new CalendarRange[ranges
												.size()]), false);
							}
							return Status.OK_STATUS;
						}
					};
					RelationView.this.highlighter.schedule();
				});

		new ContextMenu(this.relationViewer.getViewer(), this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
	}

	public RelationViewer getRelationViewer() {
		return this.relationViewer;
	}

	@Override
	public void setFocus() {
		if (this.relationViewer != null && !this.relationViewer.isDisposed()) {
			this.relationViewer.setFocus();
			this.relationViewer.refresh();
		}
	}

}
