package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.ResortableCodeViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CodeView extends ViewPart {

	private static final Logger LOGGER = Logger.getLogger(CodeView.class);
	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView";

	private ICodeService codeService;
	private IHighlightService highlightService;

	private CodeViewer codeViewer;

	public CodeView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);

		this.highlightService = (IHighlightService) PlatformUI.getWorkbench()
				.getService(IHighlightService.class);
		if (this.highlightService == null) {
			LOGGER.warn("Could not get "
					+ IHighlightService.class.getSimpleName());
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.codeViewer = new ResortableCodeViewer(parent, SWT.NONE);
		this.codeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (CodeView.this.highlightService != null
								&& CodeView.this.getSite().getWorkbenchWindow()
										.getActivePage().getActivePart() == CodeView.this) {
							if (event.getSelection() instanceof IStructuredSelection) {
								List<ICodeInstance> codeInstances = ArrayUtils
										.getAdaptableObjects(
												((ITreeSelection) event
														.getSelection())
														.toArray(),
												ICodeInstance.class);
								List<TimeZoneDateRange> ranges = new ArrayList<TimeZoneDateRange>();
								for (ICodeInstance codeInstance : codeInstances) {
									ICodeable codeable = CodeView.this.codeService
											.getCodedObject(codeInstance
													.getId());
									if (codeable instanceof HasDateRange) {
										ranges.add(((HasDateRange) codeable)
												.getDateRange());
									}
								}
								if (ranges.size() > 0) {
									CodeView.this.highlightService.highlight(
											CodeView.class,
											ranges.toArray(new TimeZoneDateRange[0]),
											true);
								}
							}
							CodeView.this.highlightService.highlight(
									CodeView.this, event.getSelection(), false);
						}
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
		}
	}

}
