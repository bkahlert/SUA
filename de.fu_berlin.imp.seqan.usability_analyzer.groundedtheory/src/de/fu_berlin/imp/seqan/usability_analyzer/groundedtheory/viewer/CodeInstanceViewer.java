package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.ViewerURI.State;

public class CodeInstanceViewer extends Composite implements ISelectionProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CodeInstanceViewer.class);

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private final SortableTreeViewer treeViewer;

	public CodeInstanceViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		Utils.addCodeColorRenderSupport(tree, 1);

		this.treeViewer = new SortableTreeViewer(tree);
		this.treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		this.createColumns();
		this.treeViewer
				.setContentProvider(new CodeInstanceViewerContentProvider());
	}

	private void createColumns() {
		this.treeViewer.createColumn("ID", new RelativeWidth(1.0, 150))
				.setLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (uri == ViewerURI.NO_CODES_URI) {
									return new StyledString("no codes",
											Stylers.MINOR_STYLER);
								}
								StyledString text = new StyledString(
										CodeInstanceViewer.this.labelProviderService
												.getLabelProvider(uri).getText(
														uri),
										Stylers.DEFAULT_STYLER);
								if (uri instanceof ViewerURI
										&& ((ViewerURI) uri).getState() == State.PARENT) {
									text.append("  parent",
											Stylers.MINOR_STYLER);
								}
								return text;
							}

							@Override
							public Image getImage(URI uri) throws Exception {
								if (uri == ViewerURI.NO_CODES_URI) {
									return null;
								}
								return CodeInstanceViewer.this.labelProviderService
										.getLabelProvider(uri).getImage(uri);
							}

							@Override
							public String getToolTipText(URI uri)
									throws Exception {
								if (uri == ViewerURI.NO_CODES_URI) {
									return null;
								}
								return uri.toString();
							}
						});
		this.treeViewer.createColumn("", new AbsoluteWidth(16))
				.setLabelProvider(
						new ILabelProviderService.ColumnLabelProvider() {
							@Override
							public String getText(Object element) {
								return "";
							}
						});

		Utils.createNumPhaenomenonsColumn(
				this.treeViewer,
				(ICodeService) PlatformUI.getWorkbench().getService(
						ICodeService.class));
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.treeViewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.treeViewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.treeViewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.treeViewer.setSelection(selection);
	}

	public void setInput(List<URI> uris) {
		this.treeViewer.setInput(uris.toArray(new URI[uris.size()]));
	}

	public StructuredViewer getViewer() {
		return this.treeViewer;
	}

	/**
	 * Returns the {@link ILocatable} that is the root of the currently selected
	 * item (e.g. a {@link ICode}).
	 * 
	 * @return
	 */
	public URI getUri() {
		List<URI> uris = new LinkedList<URI>();
		TreeItem[] treeItems = this.treeViewer.getTree().getSelection();
		for (TreeItem treeItem : treeItems) {
			URI locatable = this.getURI(treeItem);
			if (!uris.contains(locatable)) {
				uris.add(locatable);
			}
		}
		return uris.size() == 1 ? uris.get(0) : null;
	}

	/**
	 * Returns the {@link ILocatable} that is the root of the given
	 * {@link TreeItem}.
	 * <p>
	 * If the {@link TreeItem} is itself the representative for a
	 * {@link ILocatable} it is also returned.
	 * 
	 * @param treeItem
	 * @return
	 */
	public URI getURI(TreeItem treeItem) {
		if (treeItem.getData() instanceof URI) {
			return (URI) treeItem.getData();
		}
		if (treeItem.getParentItem() != null) {
			return this.getURI(treeItem.getParentItem());
		}
		return null;
	}

}
