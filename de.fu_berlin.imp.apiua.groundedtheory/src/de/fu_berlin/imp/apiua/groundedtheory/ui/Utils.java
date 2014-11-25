package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.HLS;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.util.NoNullSet;
import de.fu_berlin.imp.apiua.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.CodeLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.RelationInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.RelationLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.EditingSupport;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.ViewerURI;

public class Utils {

	public static final Logger LOGGER = Logger.getLogger(Utils.class);

	public static String chooseGTFileLocation() {
		DirectoryDialog directoryDialog = new DirectoryDialog(new Shell()); // TODO
		directoryDialog.setText("Grounded Theory Directory");
		directoryDialog
				.setMessage("Please choose where you want to store your grounded theory progress.");
		String filename = directoryDialog.open();
		return filename;
	}

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	/**
	 * Returns a color that - given the colors of all existing codes - is as
	 * different as possible.
	 *
	 * @return
	 */
	public static RGB getFancyCodeColor() {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		Set<RGB> rgbs = new NoNullSet<RGB>();
		for (ICode code : codeService.getCodeStore().getCodes()) {
			RGB rgb = code.getColor();
			HLS hls = ColorSpaceConverter.RGBtoHLS(rgb);

			double lightness = hls.getLightness();
			double saturation = hls.getSaturation();
			if (lightness > 0.4 && lightness < 0.6 && saturation > 0.4
					&& saturation < 0.6) {
				rgbs.add(code.getColor());
			}
		}
		return ColorUtils.getBestComplementColor(rgbs);
	}

	public static void addCodeColorRenderSupport(Tree tree,
			final int columnNumber) {
		tree.addListener(
				SWT.PaintItem,
				event -> {
					if (!(event.item instanceof TreeItem)
							|| !(event.item.getData() instanceof URI))
						return;
					TreeItem item = (TreeItem) event.item;
					Rectangle bounds = item.getImageBounds(columnNumber);
					bounds.width = 14;
					bounds.height = 14;
					bounds.y += 2;
					bounds.x -= 2;

					ICode code = null;
					try {
						code = LocatorService.INSTANCE.resolve(
								(URI) item.getData(), ICode.class, null).get();
					} catch (Exception e) {
						LOGGER.error("Error painting color of "
								+ item.getData());
					}
					if (code != null) {
						GTLabelProvider.drawCodeImage(code, event.gc, bounds);
					}
				});
		tree.addListener(SWT.MouseMove, new Listener() {
			private final Cursor hand = new Cursor(Display.getCurrent(),
					SWT.CURSOR_HAND);

			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree))
					return;
				Tree tree = ((Tree) event.widget);
				TreeItem item = tree.getItem(new Point(event.getBounds().x,
						event.getBounds().y));
				if (item != null && item.getData() instanceof URI) {
					if (ICode.class.equals(LocatorService.INSTANCE
							.getType((URI) item.getData()))) {
						Rectangle bounds = item.getImageBounds(columnNumber);
						bounds.width = 14;
						bounds.height = 14;
						bounds.y += 2;
						bounds.x -= 2;

						if (event.getBounds().x >= bounds.x
								&& event.getBounds().x <= bounds.x
										+ bounds.width) {
							tree.setCursor(this.hand);
						} else {
							tree.setCursor(null);
						}
					}
				}
			}
		});
		tree.addListener(
				SWT.MouseUp,
				event -> {
					if (!(event.widget instanceof Tree))
						return;
					Tree tree1 = ((Tree) event.widget);
					if (tree1.getCursor() != null) {
						ICommandService cmdService = (ICommandService) PlatformUI
								.getWorkbench().getService(
										ICommandService.class);
						Command cmd = cmdService
								.getCommand("de.fu_berlin.imp.apiua.groundedtheory.commands.recolorCode");
						try {
							cmd.executeWithChecks(new ExecutionEvent());
						} catch (NotHandledException e1) {
						} catch (Exception e2) {
							LOGGER.error("Error recoloring "
									+ event.item.getData());
						}
					}
				});
	}

	/**
	 * Creates a column that not only shows the pure object but also interesting
	 * meta data (like assigned dimension values).
	 *
	 * @param treeViewer
	 * @param codeService
	 */
	public static void createPimpedColumn(SortableTreeViewer treeViewer,
			final ICodeService codeService) {
		TreeViewerColumn codeColumn = treeViewer.createColumn("Code",
				new RelativeWidth(1.0, 150));

		final GTLabelProvider labelProvider = new GTLabelProvider();

		codeColumn
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						if (uri == ViewerURI.NO_CODES_URI)
							return new StyledString("no codes",
									Stylers.MINOR_STYLER);
						if (uri == ViewerURI.NO_RELATIONS_URI)
							return new StyledString("no relations",
									Stylers.MINOR_STYLER);
						if (uri == ViewerURI.NO_PHENOMENONS_URI)
							return new StyledString("no phenomenons",
									Stylers.MINOR_STYLER);
						StyledString text = labelProvider.getStyledText(uri);

						if (CodeLocatorProvider.CODE_NAMESPACE.equals(URIUtils
								.getResource(uri))) {
							ICode code = LocatorService.INSTANCE.resolve(uri,
									ICode.class, null).get();
							for (ICodeInstance codeInstance : CODE_SERVICE
									.getInstances(code.getUri())) {
								Pair<StyledString, StyledString> dimensionValues = GTLabelProvider
										.getDimensionValues(codeInstance);
								if (dimensionValues != null) {
									if (dimensionValues.getFirst() != null) {
										text.append(" = ");
										text.append(dimensionValues.getFirst());
									}
									if (dimensionValues.getSecond() != null) {
										text.append(" ")
												.append("(",
														Stylers.MINOR_STYLER)
												.append(Stylers.rebase(
														dimensionValues
																.getSecond(),
														Stylers.MINOR_STYLER))
												.append(")",
														Stylers.MINOR_STYLER);
									}
								}
							}
						}

						if (CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE
								.equals(URIUtils.getResource(uri))) {
							ICodeInstance codeInstance = LocatorService.INSTANCE
									.resolve(uri, ICodeInstance.class, null)
									.get();
							Stylers.rebase(text, Stylers.SMALL_STYLER);
							if (CodeLocatorProvider.CODE_NAMESPACE
									.equals(URIUtils.getResource(codeInstance
											.getId()))) {
								text.append("  phenomenon",
										Stylers.MINOR_STYLER);
							}
						}

						if (RelationLocatorProvider.RELATION_NAMESPACE
								.equals(URIUtils.getResource(uri))) {
						}

						if (RelationInstanceLocatorProvider.RELATION_INSTANCE_NAMESPACE
								.equals(URIUtils.getResource(uri))) {
						}

						return text;
					}

					@Override
					public Image getImage(URI uri) throws Exception {
						if (uri == ViewerURI.NO_CODES_URI)
							return null;
						if (uri == ViewerURI.NO_RELATIONS_URI)
							return null;
						if (uri == ViewerURI.NO_PHENOMENONS_URI)
							return null;
						return labelProvider.getImage(uri);
					}
				});

		codeColumn.setEditingSupport(new EditingSupport(treeViewer));
		TreeViewerEditor.create(treeViewer,
				new ColumnViewerEditorActivationStrategy(treeViewer) {
					@Override
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, ColumnViewerEditor.DEFAULT);
	}

	public static void createNumPhaenomenonsColumn(
			SortableTreeViewer treeViewer, final ICodeService codeService) {
		TreeViewerColumn countColumn = treeViewer.createColumn("# ph",
				new AbsoluteWidth(60));
		countColumn.getColumn().setAlignment(SWT.RIGHT);
		countColumn
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						ILocatable element = LocatorService.INSTANCE.resolve(
								uri, null).get();

						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							int all = codeService.getAllInstances(code).size();
							int here = codeService.getInstances(code).size();
							StyledString text = new StyledString(all + "",
									Stylers.DEFAULT_STYLER);
							text.append("   " + here, Stylers.COUNTER_STYLER);
							return text;
						}

						if (IRelation.class.isInstance(element)) {
							IRelation relation = (IRelation) element;
							int all = codeService
									.getRelationInstances(relation).size();
							// int here = codeService.getInstances(code).size();
							StyledString text = new StyledString(all + "",
									Stylers.DEFAULT_STYLER);
							// text.append("   " + here,
							// Stylers.COUNTER_STYLER);
							return text;
						}

						return new StyledString();
					}
				});
	}

	/**
	 * Returns all phenomenon {@link URI}s that can be retrieved from {@ICode
	 * 
	 *
	 *
	 *
	 *
	 *
	 * }s, {@link ICodeInstance}s, {@link IRelation}s and
	 * {@link IRelationInstance}s contained in the {@link ISelection} .
	 * <p>
	 * {@link ICode}s and {@link IRelation}s are treated differently. They are
	 * not only included but also their instances's phenomenon.
	 *
	 * @param selection
	 * @return
	 */
	public static URI[] getURIs(ISelection selection) {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		List<ICodeInstance> codeInstances = SelectionUtils.getAdaptableObjects(
				selection, ICodeInstance.class);
		SelectionUtils.getAdaptableObjects(selection, ICode.class).stream()
				.map(code -> codeService.getAllInstances(code))
				.forEach(codeInstances::addAll);

		List<IRelationInstance> relationInstances = SelectionUtils
				.getAdaptableObjects(selection, IRelationInstance.class);
		SelectionUtils.getAdaptableObjects(selection, IRelation.class).stream()
				.map(relation -> codeService.getRelationInstances(relation))
				.forEach(relationInstances::addAll);

		List<URI> uris = new ArrayList<URI>();
		codeInstances.stream().map(ci -> ci.getId()).forEach(uris::add);
		relationInstances.stream().map(ri -> ri.getPhenomenon())
				.forEach(uris::add);
		return uris.toArray(new URI[0]);
	}
}
