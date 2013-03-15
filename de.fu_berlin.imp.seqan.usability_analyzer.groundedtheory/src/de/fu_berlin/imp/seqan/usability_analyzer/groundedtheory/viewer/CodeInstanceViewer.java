package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;

public class CodeInstanceViewer extends Composite implements ISelectionProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CodeInstanceViewer.class);

	private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private SortableTreeViewer treeViewer;

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
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		this.treeViewer.createColumn("ID", 180).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							return code.getCaption();
						}
						if (ICodeable.class.isInstance(element)) {
							ICodeable codedObject = (ICodeable) element;
							return CodeInstanceViewer.this.labelProviderService
									.getLabelProvider(codedObject).getText(
											codedObject);
						}
						if (NoCodesNode.class.isInstance(element)) {
							return "no codes";
						}
						return "ERROR";
					}

					@Override
					public Image getImage(Object element) {
						if (ICode.class.isInstance(element)) {
							return codeService.isMemo((ICode) element) ? ImageManager.CODE_MEMO
									: ImageManager.CODE;
						}
						if (ICodeable.class.isInstance(element)) {
							ICodeable codedObject = (ICodeable) element;
							return CodeInstanceViewer.this.labelProviderService
									.getLabelProvider(codedObject).getImage(
											codedObject);
						}
						return null;
					}
				});
		this.treeViewer.createColumn("", 16).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						return "";
					}
				});
		this.treeViewer.createColumn("URI", 300).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICodeable.class.isInstance(element)) {
							ICodeable codeable = (ICodeable) element;
							return codeable.getUri().toString();
						}
						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							return new Long(code.getId()).toString();
						}
						if (NoCodesNode.class.isInstance(element)) {
							return "";
						}
						return "ERROR";
					}
				});
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

	public void setInput(List<ICodeable> codeables) {
		this.treeViewer.setInput(codeables);
	}

	public StructuredViewer getViewer() {
		return this.treeViewer;
	}

	/**
	 * Returns the {@link ICodeable} that is the root of the currently selected
	 * item (e.g. a {@link ICode}).
	 * 
	 * @return
	 */
	public ICodeable getCodeable() {
		List<ICodeable> codeables = new LinkedList<ICodeable>();
		TreeItem[] treeItems = this.treeViewer.getTree().getSelection();
		for (TreeItem treeItem : treeItems) {
			ICodeable codeable = this.getCodeable(treeItem);
			if (!codeables.contains(codeable)) {
				codeables.add(codeable);
			}
		}
		return codeables.size() == 1 ? codeables.get(0) : null;
	}

	/**
	 * Returns the {@link ICodeable} that is the root of the given
	 * {@link TreeItem}.
	 * <p>
	 * If the {@link TreeItem} is itself the representative for a
	 * {@link ICodeable} it is also returned.
	 * 
	 * @param treeItem
	 * @return
	 */
	public ICodeable getCodeable(TreeItem treeItem) {
		if (treeItem.getData() instanceof ICodeable) {
			return (ICodeable) treeItem.getData();
		}
		if (treeItem.getParentItem() != null) {
			return this.getCodeable(treeItem.getParentItem());
		}
		return null;
	}

}
