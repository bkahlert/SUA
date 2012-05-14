package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class CodeInstanceViewer extends Composite implements ISelectionProvider {

	private SortableTreeViewer treeViewer;

	public CodeInstanceViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.treeViewer = new SortableTreeViewer(tree);
		this.treeViewer.setAutoExpandLevel(2);
		createColumns();
		this.treeViewer
				.setContentProvider(new CodeInstanceViewerContentProvider());
	}

	private void createColumns() {
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		treeViewer.createColumn("ID", 300).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							return code.getCaption();
						}
						if (ICodeable.class.isInstance(element)) {
							ICodeable codedObject = (ICodeable) element;
							return codeService.getLabelProvider(
									codedObject.getCodeInstanceID()).getText(
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
							return ImageManager.CODE;
						}
						if (ICodeable.class.isInstance(element)) {
							ICodeable codedObject = (ICodeable) element;
							return codeService.getLabelProvider(
									codedObject.getCodeInstanceID()).getImage(
									codedObject);
						}
						return null;
					}
				});
		treeViewer.createColumn("URI", 300).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICodeable.class.isInstance(element)) {
							ICodeable codeable = (ICodeable) element;
							return codeable.getCodeInstanceID().toString();
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

	public Control getControl() {
		if (this.treeViewer != null)
			return this.treeViewer.getTree();
		return null;
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
		return treeViewer;
	}

}
