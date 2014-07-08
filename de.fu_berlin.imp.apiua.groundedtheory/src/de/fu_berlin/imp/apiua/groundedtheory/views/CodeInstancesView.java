package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.ISelectionRetriever;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeInstanceViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CodeInstancesView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.CodeInstancesView";

	private static final Logger LOGGER = Logger
			.getLogger(CodeInstancesView.class);

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private CodeInstanceViewer codeInstanceViewer;

	private final ISelectionRetriever<URI> uriRetriever = SelectionRetrieverFactory
			.getSelectionRetriever(URI.class);

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == CodeInstancesView.this) {
				return;
			}
			List<URI> uris = CodeInstancesView.this.uriRetriever.getSelection();
			if (uris.size() > 0) {
				CodeInstancesView.this.refreshPartName(uris);

				if (CodeInstancesView.this.codeInstanceViewer != null
						&& !CodeInstancesView.this.codeInstanceViewer
								.isDisposed()) {
					CodeInstancesView.this.codeInstanceViewer.setInput(uris);
				}
			}
		}
	};

	public CodeInstancesView() {
		SelectionUtils.getSelectionService().addSelectionListener(
				this.selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removeSelectionListener(
				this.selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.codeInstanceViewer = new CodeInstanceViewer(parent, SWT.NONE);
		this.getSite().setSelectionProvider(this.codeInstanceViewer);
		new ContextMenu(this.codeInstanceViewer.getViewer(), this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	private String initPartName = null;

	private void refreshPartName(List<URI> uris) {
		if (this.initPartName == null) {
			this.initPartName = this.getPartName();
		}

		List<String> labels = new ArrayList<String>(uris.size());
		for (URI uri : uris) {
			ILabelProvider lp = this.labelProviderService.getLabelProvider(uri);
			try {
				labels.add(lp.getText(uri));
			} catch (Exception e) {
				LOGGER.error("Error getting label for " + uri);
				labels.add("ERROR");
			}
		}

		String label = StringUtils.join(labels, ", ");
		this.setPartName(this.initPartName + ": " + label);
	}

	@Override
	public void setFocus() {
		this.codeInstanceViewer.setFocus();
	}

	public URI getLocatable() {
		if (this.codeInstanceViewer == null) {
			return null;
		}
		return this.codeInstanceViewer.getUri();
	}

}
