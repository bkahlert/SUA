package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeViewer extends Composite implements ISelectionProvider {

	private static Logger LOGGER = Logger.getLogger(CodeViewer.class);
	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private SortableTreeViewer treeViewer;

	public CodeViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.treeViewer = new SortableTreeViewer(tree);
		this.treeViewer.setAutoExpandLevel(2);
		this.treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				List<URI> codeInstanceIDs = getURIs(event.getSelection());
				if (codeService != null) {
					codeService.showCodedObjectsInWorkspace(codeInstanceIDs);
				} else {
					LOGGER.error("Could not retrieve "
							+ ICodeService.class.getSimpleName());
				}
			}
		});
		createColumns();
		this.treeViewer.setContentProvider(new CodeViewerContentProvider());
		this.treeViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
	}

	private void createColumns() {
		// TODO: Cache labelProviders on URI base
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		CodeViewerUtils.createCodeColumn(treeViewer, codeService);

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
							return codeInstance.getId().toString();

						}
						if (NoCodesNode.class.isInstance(element)) {
							return "";
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
							return preferenceUtil.getDateFormat().format(
									codeInstance.getCreation().getDate());
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

	/**
	 * Returns all {@link URI}s that can be retrieved from an {@link ISelection}
	 * .
	 * <p>
	 * E.g. if you selection contains a {@link ICode} and a
	 * {@link ICodeInstance} the resulting list contains all occurrences
	 * instances of the code and the code instance itself.
	 * 
	 * @param selection
	 * @return
	 */
	public static List<URI> getURIs(ISelection selection) {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		List<ICodeInstance> codeInstances = SelectionUtils.getAdaptableObjects(
				selection, ICodeInstance.class);
		for (ICode code : SelectionUtils.getAdaptableObjects(selection,
				ICode.class))
			codeInstances.addAll(codeService.getInstances(code));
		List<URI> uris = new ArrayList<URI>();
		for (ICodeInstance codeInstance : codeInstances)
			uris.add(codeInstance.getId());
		return uris;
	}

	public ColumnViewer getViewer() {
		return treeViewer;
	}

}
