package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.CellLabelClient;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.viewer.FilteredTree;
import com.bkahlert.nebula.viewer.FilteredTree.TreeViewerFactory;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.Utils;

public class RelationViewer extends Composite implements ISelectionProvider {

	public static Logger LOGGER = Logger.getLogger(RelationViewer.class);
	private final static SUACorePreferenceUtil PREFERENCE_UTIL = new SUACorePreferenceUtil();

	public static enum ShowInstances {
		ON, OFF;
	}

	public static enum Filterable {
		ON, OFF;
	}

	public static enum QuickSelectionMode {
		ON, OFF;
	}

	private TreeViewer viewer = null;
	private Control focusControl;

	public RelationViewer(Composite parent, int style,
			final ShowInstances initialShowInstances,
			final String saveExpandedElementsKey, Filterable filterable,
			QuickSelectionMode quickSelectionMode) {
		super(parent, style);
		this.setLayout(new FillLayout());

		if (filterable == Filterable.ON) {
			FilteredTree filteredTree = new FilteredTree(this, SWT.BORDER
					| SWT.MULTI | SWT.FULL_SELECTION, new TreeViewerFactory() {
				@Override
				public TreeViewer create(Composite parent, int style) {
					return createViewer(parent, style, initialShowInstances,
							saveExpandedElementsKey);
				}
			}, new IConverter<URI, String>() {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				CellLabelProvider clp = null;
				CellLabelClient clc = null;

				@Override
				public String convert(URI uri) {
					if (this.clp == null) {
						this.clp = RelationViewer.this.viewer
								.getLabelProvider(0);
					}
					if (this.clc == null) {
						this.clc = new CellLabelClient(this.clp);
					}
					this.clc.setElement(uri);
					StringBuilder sb = new StringBuilder(this.clc.getText());
					sb.append(" ");
					sb.append(this.codeService.loadMemoPlain(uri));
					return sb.toString();
				}
			}) {
				@Override
				protected Job prefetch(final TreeViewer treeViewer) {
					NamedJob prefetcher = new NamedJob(
							RelationViewer.this.getClass(),
							"Prefetching filterable elements") {
						@Override
						protected IStatus runNamed(IProgressMonitor monitor) {
							try {
								LocatorService.preload(RelationViewer.class,
										treeViewer, ViewerUtils
												.getTopLevelItems(treeViewer),
										monitor);
							} catch (Exception e) {
								LOGGER.error(
										"Error prefetching elements before filtering",
										e);
							}
							return Status.OK_STATUS;
						}
					};
					return prefetcher;
				}
			};
			filteredTree
					.setQuickSelectionMode(quickSelectionMode == QuickSelectionMode.ON);
			this.viewer = filteredTree.getViewer();
			this.focusControl = filteredTree.getFilterControl();
		} else {
			this.viewer = createViewer(this, style, initialShowInstances,
					saveExpandedElementsKey);
			this.focusControl = this.viewer.getControl();
		}
	}

	private static SortableTreeViewer createViewer(Composite parent, int style,
			ShowInstances initialShowInstances,
			final String saveExpandedElementsKey) {
		Tree tree = new Tree(parent, style);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		Utils.addCodeColorRenderSupport(tree, 1);

		final SortableTreeViewer viewer = new SortableTreeViewer(tree);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final ISelection selection = event.getSelection();
				URI[] codeInstanceIDs = Utils.getURIs(selection);
				if (LocatorService.INSTANCE != null) {
					LocatorService.INSTANCE.showInWorkspace(codeInstanceIDs,
							false, null);
				} else {
					LOGGER.error("Could not retrieve "
							+ ILocatorService.class.getSimpleName());
				}
			}
		});
		createColumns(viewer);
		viewer.setContentProvider(new RelationViewerContentProvider(
				initialShowInstances == ShowInstances.ON));
		viewer.setInput(PlatformUI.getWorkbench()
				.getService(ICodeService.class));
		SUAGTPreferenceUtil.loadExpandedElementsASync(viewer,
				saveExpandedElementsKey);
		tree.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				SUAGTPreferenceUtil.saveExpandedElements(viewer,
						saveExpandedElementsKey);
			}
		});

		return viewer;
	}

	private static void createColumns(SortableTreeViewer viewer) {
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		Utils.createPimpedColumn(viewer, codeService);

		// viewer.createColumn("", new AbsoluteWidth(16)).setLabelProvider(
		// new ILabelProviderService.StyledLabelProvider() {
		// @Override
		// public StyledString getStyledText(URI element)
		// throws Exception {
		// return new StyledString();
		// }
		// });

		viewer.createColumn("Date Created", new AbsoluteWidth(0)/* 170 */)
				.setLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (uri == ViewerURI.NO_PHENOMENONS_URI) {
									return new StyledString("");
								}
								ILocatable element = LocatorService.INSTANCE
										.resolve(uri, null).get();

								if (ICode.class.isInstance(element)) {
									ICode code = (ICode) element;
									return new StyledString(PREFERENCE_UTIL
											.getDateFormat().format(
													code.getCreation()
															.getDate()));
								}
								if (ICodeInstance.class.isInstance(element)) {
									ICodeInstance codeInstance = (ICodeInstance) element;
									return new StyledString(PREFERENCE_UTIL
											.getDateFormat().format(
													codeInstance.getCreation()
															.getDate()));
								}
								if (IRelation.class.isInstance(element)) {
									IRelation relation = (IRelation) element;
									return new StyledString(PREFERENCE_UTIL
											.getDateFormat().format(
													relation.getCreation()
															.getDate()));

								}
								return new StyledString("ERROR",
										Stylers.ATTENTION_STYLER);
							}
						});

		Utils.createNumPhaenomenonsColumn(viewer, codeService);
	}

	public Control getControl() {
		if (this.viewer != null) {
			return this.viewer.getTree();
		}
		return null;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.viewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.viewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.viewer.setSelection(selection);
	}

	public AbstractTreeViewer getViewer() {
		return this.viewer;
	}

	/**
	 * Shows or hides the {@link ICodeInstance}s.
	 * <p>
	 * The viewer is automatically refreshed if this method is called.
	 *
	 * @param showInstances
	 */
	public void setShowInstances(boolean showInstances) {
		if (this.viewer.getContentProvider() instanceof RelationViewerContentProvider) {
			RelationViewerContentProvider contentProvider = (RelationViewerContentProvider) this.viewer
					.getContentProvider();
			contentProvider.setShowInstances(showInstances);
		} else {
			LOGGER.error("Unexpected content provider; check implementation");
		}
	}

	public void refresh() {
		this.viewer.refresh();
	}

	@Override
	public boolean setFocus() {
		return this.focusControl.setFocus();
	}

}
