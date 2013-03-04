package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
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

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.PaintUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.NoNullSet;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer.CodeColors;

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
			if (!code.getColor().equals(new RGB(0, 0, 0))) {
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
						|| !(event.item.getData() instanceof ICode))
					return;
				TreeItem item = (TreeItem) event.item;
				Rectangle bounds = item.getImageBounds(columnNumber);
				bounds.width = 14;
				bounds.height = 14;
				bounds.y += 2;
				bounds.x -= 2;

				ICode code = (ICode) item.getData();
				CodeColors info = new CodeColors(code.getColor());
				event.gc.setAlpha(128);
				PaintUtils.drawRoundedRectangle(event.gc, bounds,
						info.getBackgroundColor(), info.getBorderColor());
			}
		});
		tree.addListener(SWT.MouseMove, new Listener() {
			private Cursor hand = new Cursor(Display.getCurrent(),
					SWT.CURSOR_HAND);

			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree))
					return;
				Tree tree = ((Tree) event.widget);
				TreeItem item = tree.getItem(new Point(event.getBounds().x,
						event.getBounds().y));
				if (item != null) {
					Rectangle bounds = item.getImageBounds(columnNumber);
					bounds.width = 14;
					bounds.height = 14;
					bounds.y += 2;
					bounds.x -= 2;

					if (event.getBounds().x >= bounds.x
							&& event.getBounds().x <= bounds.x + bounds.width) {
						tree.setCursor(hand);
					} else {
						tree.setCursor(null);
					}
				}
			}
		});
		tree.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree))
					return;
				Tree tree = ((Tree) event.widget);
				if (tree.getCursor() != null) {
					ICommandService cmdService = (ICommandService) PlatformUI
							.getWorkbench().getService(ICommandService.class);
					Command cmd = cmdService
							.getCommand("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.commands.recolorCode");
					try {
						cmd.executeWithChecks(new ExecutionEvent());
					} catch (Exception e) {
						LOGGER.error("Error recoloring " + event.item.getData());
					}
				}
			}
		});
	}

}
