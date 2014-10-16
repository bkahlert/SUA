package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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

import com.bkahlert.nebula.NebulaPreferences;
import com.bkahlert.nebula.utils.CellLabelClient;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
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
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.Utils;

public class CodeViewer extends Composite implements ISelectionProvider {

	private static Logger LOGGER = Logger.getLogger(CodeViewer.class);
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

	public CodeViewer(Composite parent, int style,
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
						this.clp = CodeViewer.this.viewer.getLabelProvider(0);
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
							CodeViewer.this.getClass(),
							"Prefetching filterable elements") {
						@Override
						protected IStatus runNamed(IProgressMonitor monitor) {
							try {
								CodeViewer.this.preload(ViewerUtils
										.getTopLevelItems(treeViewer), monitor);
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
				URI[] codeInstanceIDs = getURIs(selection);
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
		viewer.setContentProvider(new CodeViewerContentProvider(
				initialShowInstances == ShowInstances.ON));
		viewer.setInput(PlatformUI.getWorkbench()
				.getService(ICodeService.class));
		loadExpandedElements(viewer, saveExpandedElementsKey);
		tree.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveExpandedElements(viewer, saveExpandedElementsKey);
			}
		});

		return viewer;
	}

	private static void createColumns(SortableTreeViewer viewer) {
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		Utils.createCodeColumn(viewer, codeService);

		viewer.createColumn("", new AbsoluteWidth(16)).setLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI element)
							throws Exception {
						return new StyledString();
					}
				});

		viewer.createColumn("ID", new AbsoluteWidth(0)/* 150 */)
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
									return new StyledString(new Long(code
											.getId()).toString());
								}
								if (ICodeInstance.class.isInstance(element)) {
									ICodeInstance codeInstance = (ICodeInstance) element;
									return new StyledString(codeInstance
											.getId().toString());

								}
								return new StyledString("ERROR",
										Stylers.ATTENTION_STYLER);
							}
						});
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
		return this.viewer;
	}

	@SuppressWarnings("unchecked")
	private static void saveExpandedElements(TreeViewer viewer,
			String saveExpandedElementsKey) {
		new NebulaPreferences().saveExpandedElements(saveExpandedElementsKey,
				viewer, new IConverter<Object, String>() {
					@Override
					public String convert(Object returnValue) {
						if (returnValue instanceof URI) {
							return ((URI) returnValue).toString();
						}
						return null;
					}
				});
	}

	@SuppressWarnings("unchecked")
	private static void loadExpandedElements(TreeViewer viewer,
			String saveExpandedElementsKey) {
		new NebulaPreferences().loadExpandedElements(saveExpandedElementsKey,
				viewer, new IConverter<String, Object>() {
					@Override
					public Object convert(String returnValue) {
						try {
							return new URI(returnValue);
						} catch (Exception e) {
							LOGGER.error("Error loading expanded element "
									+ returnValue, e);
						}
						return null;
					}
				});
	}

	/**
	 * Resolves (using {@link LocatorService}) all the given objects and their
	 * descendants. This becomes handy to preload items, so a later running UI
	 * thread code will not need to spend the time resolving.
	 * 
	 * @param parentObjects
	 * @param monitor
	 * @return
	 */
	/**
	 * Preloads the given of the
	 * 
	 * @param parentElements
	 * @param monitor
	 * 
	 * @throws Exception
	 */
	protected void preload(final List<?> parentElements,
			IProgressMonitor monitor) throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
		List<URI> uris = ExecUtils.syncExec(new Callable<List<URI>>() {
			@Override
			public List<URI> call() throws Exception {
				List<Object> elements = new ArrayList<Object>();
				for (Object parentObject : parentElements) {
					elements.addAll(ViewerUtils.getDescendants(
							CodeViewer.this.viewer, parentObject));
				}
				List<URI> uris = new ArrayList<URI>();
				for (Object object : elements) {
					if (object instanceof URI) {
						URI uri = (URI) object;
						if (!uris.contains(uri)) {
							uris.add(uri);
						}
					}
				}
				return uris;
			}
		});
		subMonitor.worked(1);
		LocatorService.preload(uris, subMonitor.newChild(9));
		subMonitor.done();
	}

	/**
	 * Shows or hides the {@link ICodeInstance}s.
	 * <p>
	 * The viewer is automatically refreshed if this method is called.
	 * 
	 * @param showInstances
	 */
	public void setShowInstances(boolean showInstances) {
		if (this.viewer.getContentProvider() instanceof CodeViewerContentProvider) {
			CodeViewerContentProvider contentProvider = (CodeViewerContentProvider) this.viewer
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
