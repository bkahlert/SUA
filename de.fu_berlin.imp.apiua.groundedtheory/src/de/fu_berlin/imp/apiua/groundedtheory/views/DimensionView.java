package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.PartRenamer;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.ISelectionRetriever;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionComposite;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionValueComposite;
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
			} catch (Exception e) {
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
	private Composite parent;
	private DimensionComposite dimensionComposite;
	private DimensionValueComposite dimensionValueComposite;

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
		this.parent = parent;
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.spacing(5, 5).margins(5, 5).equalWidth(true).create());

		Group dimensionGroup = new Group(parent, SWT.BORDER);
		dimensionGroup.setText("Dimension");
		dimensionGroup.setLayoutData(GridDataFactory.fillDefaults().span(1, 2)
				.grab(true, true).create());
		dimensionGroup.setLayout(new FillLayout());
		this.dimensionComposite = new DimensionComposite(dimensionGroup,
				SWT.NONE);

		Group dimensionValueGroup = new Group(parent, SWT.BORDER);
		dimensionValueGroup.setText("Dimension Values");
		dimensionValueGroup.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		dimensionValueGroup.setLayout(new FillLayout());
		this.dimensionValueComposite = new DimensionValueComposite(
				dimensionValueGroup, SWT.NONE);

		new Label(parent, SWT.BORDER).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, true).create());

		// new ContextMenu(this.episodeViewer.getViewer(), this.getSite()) {
		// @Override
		// protected String getDefaultCommandID() {
		// return null;
		// }
		// };

	}

	private void load(URI uri) throws CodeStoreWriteException,
			CodeServiceException {
		this.partRenamer.apply(uri);
		this.dimensionComposite.load(uri);
		this.dimensionValueComposite.load(uri);

		this.parent.layout();
	}

	@Override
	public void setFocus() {
		this.dimensionComposite.setFocus();
	}

}
