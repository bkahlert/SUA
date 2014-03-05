package de.fu_berlin.imp.seqan.usability_analyzer.uri.views;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.services.IUriService;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.viewers.UriContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.viewers.UriViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class UriView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.uri.views.UriView";

	private UriViewer uriViewer;

	public UriView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.uriViewer = new UriViewer(parent, SWT.MULTI,
				new SUACorePreferenceUtil().getDateFormat());
		this.uriViewer.setContentProvider(new UriContentProvider());
		this.uriViewer.setInput(PlatformUI.getWorkbench().getService(
				IUriService.class));

		new ContextMenu(this.uriViewer, this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return "de.fu_berlin.imp.seqan.usability_analyzer.uri.editUri";
			}
		};
	}

	@Override
	public void setFocus() {
		if (this.uriViewer != null && !this.uriViewer.getControl().isDisposed()) {
			this.uriViewer.getControl().setFocus();
			this.uriViewer.refresh();
		}
	}

	/**
	 * Selects the given {@link URI}s in the corresponding viewer.
	 * 
	 * @UI this method must be called from the UI thread
	 * 
	 * @param URIS
	 * @return the successfully selected {@link URI}s
	 */
	public URI[] select(ILocatable[] locatables) {
		this.uriViewer.setSelection(new StructuredSelection(locatables));
		ISelection selection = this.uriViewer.getSelection();
		if (selection instanceof StructuredSelection) {
			return ArrayUtils.getAdaptableObjects(
					((StructuredSelection) selection).toArray(), URI.class)
					.toArray(new URI[0]);
		} else {
			return new URI[0];
		}
	}

}