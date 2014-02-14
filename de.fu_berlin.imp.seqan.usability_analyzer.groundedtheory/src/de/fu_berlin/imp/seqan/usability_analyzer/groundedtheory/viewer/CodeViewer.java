package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.Stylers;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;

public class CodeViewer extends Composite implements ISelectionProvider {

	private static Logger LOGGER = Logger.getLogger(CodeViewer.class);
	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final SortableTreeViewer treeViewer;

	public CodeViewer(Composite parent, int style) {
		this(parent, style, true);
	}

	public CodeViewer(Composite parent, int style, boolean showInstances) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		Utils.addCodeColorRenderSupport(tree, 1);

		this.treeViewer = new SortableTreeViewer(tree);
		this.treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final ISelection selection = event.getSelection();
				ILocatorService locatorService = (ILocatorService) PlatformUI
						.getWorkbench().getService(ILocatorService.class);
				URI[] codeInstanceIDs = getURIs(selection);
				if (locatorService != null) {
					locatorService
							.showInWorkspace(codeInstanceIDs, false, null);
				} else {
					LOGGER.error("Could not retrieve "
							+ ILocatorService.class.getSimpleName());
				}
			}
		});
		this.createColumns();
		this.treeViewer.setContentProvider(new CodeViewerContentProvider(
				showInstances));
		this.treeViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
	}

	private void createColumns() {
		// TODO: Cache labelProviders on URI base
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		CodeViewerUtils.createCodeColumn(this.treeViewer, codeService);

		this.treeViewer.createColumn("", new AbsoluteWidth(16))
				.setLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI element)
									throws Exception {
								return new StyledString();
							}
						});

		this.treeViewer
				.createColumn("ID", new AbsoluteWidth(0)/* 150 */)
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										if (NoCodesNode.Uri.equals(uri)) {
											return new StyledString();
										}
										ILocatable element = CodeViewer.this.locatorService
												.resolve(uri, null).get();

										if (ICode.class.isInstance(element)) {
											ICode code = (ICode) element;
											return new StyledString(new Long(
													code.getId()).toString());
										}
										if (ICodeInstance.class
												.isInstance(element)) {
											ICodeInstance codeInstance = (ICodeInstance) element;
											return new StyledString(
													codeInstance.getId()
															.toString());

										}
										return new StyledString("ERROR",
												Stylers.ATTENTION_STYLER);
									}
								}));
		this.treeViewer
				.createColumn("Date Created", new AbsoluteWidth(0)/* 170 */)
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										if (NoCodesNode.Uri.equals(uri)) {
											return new StyledString();
										}
										ILocatable element = CodeViewer.this.locatorService
												.resolve(uri, null).get();

										if (ICode.class.isInstance(element)) {
											ICode code = (ICode) element;
											return new StyledString(
													CodeViewer.this.preferenceUtil
															.getDateFormat()
															.format(code
																	.getCreation()
																	.getDate()));
										}
										if (ICodeInstance.class
												.isInstance(element)) {
											ICodeInstance codeInstance = (ICodeInstance) element;
											return new StyledString(
													CodeViewer.this.preferenceUtil
															.getDateFormat()
															.format(codeInstance
																	.getCreation()
																	.getDate()));
										}
										return new StyledString("ERROR",
												Stylers.ATTENTION_STYLER);
									}
								}));

		TreeViewerColumn countColumn = this.treeViewer.createColumn("# ph",
				new AbsoluteWidth(60));
		countColumn.getColumn().setAlignment(SWT.RIGHT);
		countColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						ILocatable element = CodeViewer.this.locatorService
								.resolve(uri, null).get();

						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							int all = codeService.getAllInstances(code).size();
							int here = codeService.getInstances(code).size();
							return new StyledString((all != here) ? all + " ("
									+ here + ")" : all + "");
						}
						return new StyledString();
					}
				}));
	}

	public Control getControl() {
		if (this.treeViewer != null) {
			return this.treeViewer.getTree();
		}
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
	public static URI[] getURIs(ISelection selection) {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		List<ICodeInstance> codeInstances = SelectionUtils.getAdaptableObjects(
				selection, ICodeInstance.class);
		for (ICode code : SelectionUtils.getAdaptableObjects(selection,
				ICode.class)) {
			codeInstances.addAll(codeService.getAllInstances(code));
		}
		List<URI> uris = new ArrayList<URI>();
		for (ICodeInstance codeInstance : codeInstances) {
			uris.add(codeInstance.getId());
		}
		return uris.toArray(new URI[0]);
	}

	public AbstractTreeViewer getViewer() {
		return this.treeViewer;
	}

	public void refresh() {
		this.treeViewer.refresh();
	}

}
