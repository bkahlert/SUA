package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.PartRenamer;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.ISelectionRetriever;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionComposite;
import de.fu_berlin.imp.apiua.groundedtheory.ui.UriPartRenamerConverter;

public class DimensionView extends ViewPart {

	private static final Logger LOGGER = Logger.getLogger(DimensionView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.DimensionView";

	private final ISelectionRetriever<URI> uriRetriever = SelectionRetrieverFactory
			.getSelectionRetriever(URI.class);

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == DimensionView.this) {
				return;
			}
			List<URI> uris = DimensionView.this.uriRetriever.getSelection();
			try {
				if (uris.size() > 0) {
					DimensionView.this.load(uris.get(0));
				} else {
					DimensionView.this.load(null);
				}
			} catch (CodeStoreWriteException e) {
				LOGGER.error(e);
			}
		}
	};

	public static enum DimensionType {
		None, Nominal;

		@Override
		public String toString() {
			return super.toString();
		};
	}

	private final PartRenamer<URI> partRenamer;
	private DimensionComposite dimensionComposite;

	public DimensionView() {
		this.partRenamer = new PartRenamer<URI>(this,
				new UriPartRenamerConverter());
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

		this.dimensionComposite = new DimensionComposite(parent, SWT.NONE);

		// new ContextMenu(this.episodeViewer.getViewer(), this.getSite()) {
		// @Override
		// protected String getDefaultCommandID() {
		// return null;
		// }
		// };

	}

	private void load(URI uri) throws CodeStoreWriteException {
		this.partRenamer.apply(uri);
		this.dimensionComposite.load(uri);
	}

	@Override
	public void setFocus() {
		this.dimensionComposite.setFocus();
	}

}
