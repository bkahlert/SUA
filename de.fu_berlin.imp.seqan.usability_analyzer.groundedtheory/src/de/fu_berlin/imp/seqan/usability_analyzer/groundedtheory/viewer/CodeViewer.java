package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTreeViewer;
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

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.NebulaPreferences;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.viewer.FilteredTree;
import com.bkahlert.nebula.viewer.FilteredTree.TreeViewerFactory;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;

public class CodeViewer extends Composite implements ISelectionProvider {

	private static Logger LOGGER = Logger.getLogger(CodeViewer.class);
	private final static SUACorePreferenceUtil PREFERENCE_UTIL = new SUACorePreferenceUtil();
	private final static ILocatorService LOCATOR_SERVICE = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

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
			final ShowInstances showInstances,
			final String saveExpandedElementsKey, Filterable filterable,
			QuickSelectionMode quickSelectionMode) {
		super(parent, style);
		this.setLayout(new FillLayout());

		if (filterable == Filterable.ON) {
			FilteredTree filteredTree = new FilteredTree(this, SWT.BORDER
					| SWT.MULTI | SWT.FULL_SELECTION, new TreeViewerFactory() {
				@Override
				public TreeViewer create(Composite parent, int style) {
					return createViewer(parent, style, showInstances,
							saveExpandedElementsKey);
				}
			});
			filteredTree
					.setQuickSelectionMode(quickSelectionMode == QuickSelectionMode.ON);
			this.viewer = filteredTree.getViewer();
			this.focusControl = filteredTree.getFilterControl();
		} else {
			this.viewer = createViewer(this, style, showInstances,
					saveExpandedElementsKey);
			this.focusControl = this.viewer.getControl();
		}
	}

	private static SortableTreeViewer createViewer(Composite parent, int style,
			ShowInstances showInstances, final String saveExpandedElementsKey) {
		Tree tree = new Tree(parent, style);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		Utils.addCodeColorRenderSupport(tree, 1);

		final SortableTreeViewer viewer = new SortableTreeViewer(tree);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
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
		createColumns(viewer);
		viewer.setContentProvider(new CodeViewerContentProvider(
				showInstances == ShowInstances.ON));
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
								ILocatable element = LOCATOR_SERVICE.resolve(
										uri, null).get();

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
								ILocatable element = LOCATOR_SERVICE.resolve(
										uri, null).get();

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

	public void refresh() {
		this.viewer.refresh();
	}

	@Override
	public boolean setFocus() {
		return this.focusControl.setFocus();
	}

}
