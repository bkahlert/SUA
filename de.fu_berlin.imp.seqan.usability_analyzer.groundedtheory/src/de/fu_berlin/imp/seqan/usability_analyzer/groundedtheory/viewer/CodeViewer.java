package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeViewer extends Composite implements ISelectionProvider {

	private SortableTreeViewer treeViewer;

	public CodeViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.treeViewer = new SortableTreeViewer(tree);
		this.treeViewer.setAutoExpandLevel(2);
		createColumns();
		this.treeViewer.setContentProvider(new CodeViewerContentProvider());
		this.treeViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
	}

	private void createColumns() {
		treeViewer.createColumn("Code", 150).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							return code.getCaption();
						}
						if (ICodeInstance.class.isInstance(element)) {
							ICodeInstance codeInstance = (ICodeInstance) element;
							return codeInstance.getId().toString();
						}
						return "ERROR";
					}
				});
		treeViewer.createColumn("ID", 150).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							return new Long(code.getId()).toString();
						}
						if (ICodeInstance.class.isInstance(element)) {
							ICodeInstance codeInstance = (ICodeInstance) element;
							return codeInstance.toString();
						}
						return "ERROR";
					}
				});
		treeViewer.createColumn("Date Created", 170).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (ICode.class.isInstance(element)) {
							return "";
						}
						if (ICodeInstance.class.isInstance(element)) {
							ICodeInstance codeInstance = (ICodeInstance) element;
							return codeInstance.getCreation().toISO8601();
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

}
