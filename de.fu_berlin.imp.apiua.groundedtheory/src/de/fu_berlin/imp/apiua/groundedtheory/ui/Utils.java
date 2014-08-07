package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
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
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.HLS;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.util.NoNullSet;
import de.fu_berlin.imp.apiua.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.CodeLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeEditingSupport;
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
		tree.addListener(SWT.PaintItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.item instanceof TreeItem)
						|| !(event.item.getData() instanceof URI)) {
					return;
				}
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
					LOGGER.error("Error painting color of " + item.getData());
				}
				if (code != null) {
					GTLabelProvider.drawCodeImage(code, event.gc, bounds);
				}
			}
		});
		tree.addListener(SWT.MouseMove, new Listener() {
			private final Cursor hand = new Cursor(Display.getCurrent(),
					SWT.CURSOR_HAND);

			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree)) {
					return;
				}
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
		tree.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree)) {
					return;
				}
				Tree tree = ((Tree) event.widget);
				if (tree.getCursor() != null) {
					ICommandService cmdService = (ICommandService) PlatformUI
							.getWorkbench().getService(ICommandService.class);
					Command cmd = cmdService
							.getCommand("de.fu_berlin.imp.apiua.groundedtheory.commands.recolorCode");
					try {
						cmd.executeWithChecks(new ExecutionEvent());
					} catch (NotHandledException e) {
					} catch (Exception e) {
						LOGGER.error("Error recoloring " + event.item.getData());
					}
				}
			}
		});
	}

	public static void createCodeColumn(SortableTreeViewer treeViewer,
			final ICodeService codeService) {
		TreeViewerColumn codeColumn = treeViewer.createColumn("Code",
				new RelativeWidth(1.0, 150));

		final GTLabelProvider labelProvider = new GTLabelProvider();

		codeColumn
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI element)
							throws Exception {
						if (element == ViewerURI.NO_PHENOMENONS_URI) {
							return new StyledString("no phenomenons",
									Stylers.MINOR_STYLER);
						}
						StyledString text = labelProvider
								.getStyledText(element);
						if (CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE
								.equals(URIUtils.getResource(element))) {
							ICodeInstance codeInstance = LocatorService.INSTANCE
									.resolve(element, ICodeInstance.class, null)
									.get();
							text.setStyle(0, text.length(),
									Stylers.SMALL_STYLER);
							if (CodeLocatorProvider.CODE_NAMESPACE
									.equals(URIUtils.getResource(codeInstance
											.getId()))) {
								text.append("  phenomenon",
										Stylers.MINOR_STYLER);
							}
						}
						return text;
					}

					@Override
					public Image getImage(URI element) throws Exception {
						if (element == ViewerURI.NO_PHENOMENONS_URI) {
							return null;
						}
						return labelProvider.getImage(element);
					}
				});

		codeColumn.setEditingSupport(new CodeEditingSupport(treeViewer));
		TreeViewerEditor.create(treeViewer,
				new ColumnViewerEditorActivationStrategy(treeViewer) {
					@Override
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, TreeViewerEditor.DEFAULT);
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

						return new StyledString();
					}
				});
	}
}