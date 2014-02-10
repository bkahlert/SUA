package de.fu_berlin.imp.seqan.usability_analyzer.uri.viewers;

import java.net.URI;
import java.text.DateFormat;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;
import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class UriViewer extends SortableTreeViewer {

	private static final Logger LOGGER = Logger.getLogger(UriViewer.class);

	private LocalResourceManager resources;
	private TreeViewerColumn column;

	public UriViewer(final Composite parent, int style, DateFormat dateFormat) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				UriViewer.this.resources.dispose();
			}
		});

		final Tree tree = (Tree) this.getControl();
		tree.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				UriViewer.this.column.getColumn().setWidth(
						tree.getClientArea().width);
			}
		});

		this.initColumns(dateFormat);

		final ISelectionRetriever<ILocatable> locatableRetriever = SelectionRetrieverFactory
				.getSelectionRetriever(ILocatable.class);

		int operations = DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		this.addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					@Override
					public void dragStart(DragSourceEvent event) {
						if (locatableRetriever.getSelection().size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									UriViewer.this.getSelection());
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					@Override
					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
						LocalSelectionTransfer.getTransfer()
								.setSelectionSetTime(0);
					}
				});

		this.sort(0);
	}

	private void initColumns(final DateFormat dateFormat) {
		this.column = this.createColumn("URI", 200);
		this.column.setLabelProvider(new StyledCellLabelProvider() {
			private UriLabelProvider uriLabelProvider = new UriLabelProvider();

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				if (element instanceof URI) {
					URI uri = (URI) element;
					try {
						StyledString text = this.uriLabelProvider
								.getStyledText(uri);
						cell.setText(text.getString());
						cell.setStyleRanges(text.getStyleRanges());
						cell.setImage(this.uriLabelProvider.getImage(uri));
					} catch (Exception e) {
						LOGGER.error("Error styling " + uri, e);
					}
				}
			}
		});
	}
}