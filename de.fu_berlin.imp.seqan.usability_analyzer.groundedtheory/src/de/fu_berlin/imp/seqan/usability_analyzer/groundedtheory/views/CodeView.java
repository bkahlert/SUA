package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.NamedJob;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.nebula.datetime.CalendarRange;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.ResortableCodeViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CodeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView";

	private static final Logger LOGGER = Logger.getLogger(CodeView.class);
	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final IHighlightService highlightService = (IHighlightService) PlatformUI
			.getWorkbench().getService(IHighlightService.class);
	private Job highlighter = null;

	private CodeViewer codeViewer;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.codeViewer = new ResortableCodeViewer(parent, SWT.NONE);
		this.codeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(
							final SelectionChangedEvent event) {
						if (CodeView.this.highlightService == null
								|| CodeView.this.getSite().getWorkbenchWindow()
										.getActivePage().getActivePart() != CodeView.this) {
							return;
						}

						if (CodeView.this.highlighter != null) {
							CodeView.this.highlighter.cancel();
						}
						if (!(event.getSelection() instanceof IStructuredSelection)) {
							return;
						}

						CodeView.this.highlighter = new NamedJob(
								CodeView.class, "Highlight") {
							@Override
							protected IStatus runNamed(IProgressMonitor monitor) {
								List<URI> uris = ArrayUtils
										.getAdaptableObjects(
												((ITreeSelection) event
														.getSelection())
														.toArray(), URI.class);

								SubMonitor subMonitor = SubMonitor.convert(
										monitor, uris.size() * 2);
								if (subMonitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
								List<ICodeInstance> codeInstances;
								try {
									codeInstances = CodeView.this.locatorService
											.resolve(
													uris,
													ICodeInstance.class,
													subMonitor.newChild(uris
															.size())).get();
								} catch (Exception e) {
									LOGGER.error("Error resolving "
											+ StringUtils.join(uris, ", "), e);
									return Status.CANCEL_STATUS;
								}
								if (subMonitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
								List<CalendarRange> ranges = new ArrayList<CalendarRange>();
								for (ICodeInstance codeInstance : codeInstances) {
									ILocatable locatable;
									try {
										locatable = CodeView.this.locatorService
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
												.getDateRange()
												.getCalendarRange());
									}
									if (subMonitor.isCanceled()) {
										return Status.CANCEL_STATUS;
									}
								}
								if (ranges.size() > 0) {
									CodeView.this.highlightService.highlight(
											CodeView.class,
											ranges.toArray(new CalendarRange[ranges
													.size()]), false);
								}
								return Status.OK_STATUS;
							}
						};
						CodeView.this.highlighter.schedule();
					}
				});

		new ContextMenu(this.codeViewer.getViewer(), this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	public CodeViewer getCodeViewer() {
		return this.codeViewer;
	}

	@Override
	public void setFocus() {
		if (this.codeViewer != null && !this.codeViewer.isDisposed()) {
			this.codeViewer.setFocus();
			this.codeViewer.refresh();
		}
	}

}
