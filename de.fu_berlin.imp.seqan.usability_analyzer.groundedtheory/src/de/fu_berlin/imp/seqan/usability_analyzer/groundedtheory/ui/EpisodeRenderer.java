package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.inf.nebula.utils.ColorUtils;
import de.fu_berlin.inf.nebula.utils.PaintUtils;

public class EpisodeRenderer {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeRenderer.class);

	public static Color DEFAULT_BACKGROUND_COLOR = new Color(
			Display.getCurrent(), 141, 206, 231);

	public static class EpisodeRenderingInfo {
		private Color backgroundColor = null;
		private Color borderColor = null;
		private int offset = 0;

		public EpisodeRenderingInfo(RGB backgroundRGB, int offset) {
			if (backgroundRGB == null)
				this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
			else
				this.backgroundColor = new Color(Display.getCurrent(),
						backgroundRGB);

			this.borderColor = ColorUtils.addLightness(backgroundColor, -0.15f);

			this.offset = offset;
		}

		public Color getBackgroundColor() {
			return backgroundColor;
		}

		public Color getBorderColor() {
			return borderColor;
		}

		public int getOffset() {
			return offset;
		}

		public void dispose() {
			if (DEFAULT_BACKGROUND_COLOR != this.backgroundColor
					&& this.backgroundColor != null
					&& !this.backgroundColor.isDisposed())
				this.backgroundColor.dispose();
			if (this.borderColor != null && !this.borderColor.isDisposed())
				this.borderColor.dispose();
		}
	}

	private static class Renderer implements PaintListener {

		private Map<IEpisode, EpisodeRenderingInfo> renderingInfos = new HashMap<IEpisode, EpisodeRenderingInfo>();

		private ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		public Renderer() {
		}

		@Override
		public void paintControl(PaintEvent e) {
			Item[] items = (e.widget instanceof Tree) ? ((Tree) e.widget)
					.getItems() : ((Table) e.widget).getItems();
			if (items.length == 0)
				return;

			Object key = null;

			// sanity check
			boolean clean = true;
			for (Item item : items) {
				if (item.getData() instanceof HasID
						|| item.getData() instanceof HasFingerprint) {
					Object currentKey = item.getData() instanceof HasID
							&& ((HasID) item.getData()).getID() != null ? ((HasID) item
							.getData()).getID() : ((HasFingerprint) item
							.getData()).getFingerprint();
					if (key == null)
						key = currentKey;
					else if (!key.equals(currentKey)) {
						clean = false;
						break;
					}
				} else {
					clean = false;
					break;
				}
			}
			if (!clean) {
				LOGGER.error(IEpisode.class.getSimpleName()
						+ "s can currently only be rendered in "
						+ Viewer.class.getSimpleName()
						+ "s that display data from a single "
						+ ID.class.getSimpleName() + " or "
						+ Fingerprint.class.getSimpleName());
				return;
			}

			List<IEpisode> episodes;
			if (key instanceof ID)
				episodes = codeService.getEpisodes((ID) key);
			else
				episodes = codeService.getEpisodes((Fingerprint) key);

			Map<IEpisode, Rectangle> renderings = new HashMap<IEpisode, Rectangle>();

			for (Item item : items) {
				if (item.getData() instanceof HasDateRange) {
					TimeZoneDateRange range = ((HasDateRange) item.getData())
							.getDateRange();

					for (IEpisode episode : episodes) {
						if (range.isIntersected2(episode.getRange())) {
							Rectangle currentBounds = getBounds(item);

							// alter currentBounds so it reflects the height of
							// the item itself plus all its children
							Rectangle deepestChildBounds = getBounds(getDeepestChildItem(item));
							currentBounds.height = deepestChildBounds.y
									+ deepestChildBounds.height
									- currentBounds.y;

							if (!renderings.containsKey(episode))
								renderings.put(episode, currentBounds);
							else {
								Rectangle bounds = renderings.get(episode);
								bounds.y = Math.min(bounds.y, currentBounds.y);
								bounds.height = Math.max(bounds.height,
										currentBounds.height + currentBounds.y
												- bounds.y);
							}
						}
					}
				}
			}

			for (IEpisode episode : renderings.keySet()) {
				if (!renderingInfos.containsKey(episode)) {
					int offset = renderingInfos.keySet().size();
					renderingInfos.put(episode, new EpisodeRenderingInfo(
							episode.getColor(), offset));
				}

				EpisodeRenderingInfo renderingInfo = renderingInfos
						.get(episode);
				Rectangle bounds = renderings.get(episode);
				bounds.x = 1 + renderingInfo.getOffset();
				bounds.width = 4;

				e.gc.setAlpha(128);
				PaintUtils.drawRoundedRectangle(e.gc, bounds,
						renderingInfo.getBackgroundColor(),
						renderingInfo.getBorderColor());
			}
		}

		private Rectangle getBounds(Item item) {
			return item instanceof TreeItem ? ((TreeItem) item).getBounds()
					: ((TableItem) item).getBounds();
		}

		private Item getDeepestChildItem(Item item) {
			if (!(item instanceof TreeItem))
				return item;
			TreeItem[] treeItems = ((TreeItem) item).getItems();
			if (treeItems == null || treeItems.length == 0)
				return item;
			return getDeepestChildItem(treeItems[treeItems.length - 1]);
		}
	}

	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		private void redraw() {
			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.getControl().redraw();
				}
			});
		}

		public void episodeAdded(IEpisode episode) {
			redraw();
		};

		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			redraw();
		};

		public void episodesDeleted(java.util.List<IEpisode> episodes) {
			redraw();
		};
	};

	private Renderer renderer = new Renderer();
	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	private EpisodeRenderer(Viewer viewer) {
		this.viewer = viewer;
	}

	public EpisodeRenderer(AbstractTableViewer viewer) {
		this((Viewer) viewer);
	}

	public EpisodeRenderer(AbstractTreeViewer viewer) {
		this((Viewer) viewer);
	}

	public void activateRendering() {
		this.viewer.getControl().addPaintListener(renderer);
		this.codeService.addCodeServiceListener(codeServiceListener);
	}

	public void deactivateRendering() {
		this.codeService.removeCodeServiceListener(codeServiceListener);
		this.viewer.getControl().removePaintListener(renderer);
	}
}
