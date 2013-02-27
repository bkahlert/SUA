package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.rendering.TrackCalculator;
import com.bkahlert.devel.nebula.rendering.TrackCalculator.Converter;
import com.bkahlert.devel.nebula.rendering.TrackCalculator.ITrackCalculation;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.PaintUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.GeometryUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class EpisodeRenderer implements IDisposable {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeRenderer.class);

	/**
	 * This constant is used to tell users that an {@link EpisodeRenderer} event
	 * occurs.
	 * <p>
	 * Use <code>{@link Control#getData(String))} != null</code> to check for an
	 * event.
	 */
	public static final String CONTROL_DATA_STRING = EpisodeRenderer.class
			.getSimpleName() + "_INTERACTIVITY";

	public static final int MAX_DISTANCE_TO_RESIZE = 16;

	public static Color DEFAULT_BACKGROUND_COLOR = new Color(
			Display.getCurrent(), 141, 206, 231);

	public static class CodeColors {
		private Color backgroundColor = null;
		private Color borderColor = null;

		public CodeColors(RGB backgroundRGB) {
			if (backgroundRGB == null)
				this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
			else
				this.backgroundColor = new Color(Display.getCurrent(),
						backgroundRGB);

			this.borderColor = new Color(Display.getDefault(), ColorUtils
					.scaleLightnessBy(
							new com.bkahlert.devel.nebula.colors.RGB(
									backgroundColor.getRGB()), 0.85f)
					.toClassicRGB());
		}

		public Color getBackgroundColor() {
			return backgroundColor;
		}

		public Color getBorderColor() {
			return borderColor;
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

	private static class ResizeInfo {
		public static ResizeInfo getInfoIfApplicable(Event event,
				ViewerColumn column, Map<IEpisode, Rectangle> episodeBounds) {
			Rectangle bounds = com.bkahlert.devel.nebula.utils.ViewerUtils
					.getBounds(column);
			if (event.x >= bounds.x && event.x <= bounds.x + bounds.width) {
				ResizeInfo info = null;
				double distance = MAX_DISTANCE_TO_RESIZE;
				for (IEpisode episode : episodeBounds.keySet()) {
					Rectangle episodeBounds_ = episodeBounds.get(episode);
					double currDistance = GeometryUtils.shortestDistance(
							episodeBounds_, new Point(event.x, event.y));
					if (currDistance < distance) {
						distance = currDistance;

						if (event.y < episodeBounds_.y + MAX_DISTANCE_TO_RESIZE) {
							info = new ResizeInfo(-1, episode);
						} else if (event.y > episodeBounds_.y
								+ episodeBounds_.height
								- MAX_DISTANCE_TO_RESIZE) {
							info = new ResizeInfo(+1, episode);
						} else {
							info = new ResizeInfo(0, episode);
						}
					}
				}
				return info;
			}
			return null;
		}

		private int direction;
		private IEpisode episode;
		private IEpisode newEpisode;
		private HasDateRange hoveredItem;

		public ResizeInfo(int direction, IEpisode episode) {
			super();
			this.direction = direction;
			this.episode = episode;
			this.newEpisode = null;
			this.hoveredItem = null;
		}

		/**
		 * @return 1 for upwards direction and -1 for downwards
		 */
		public int getDirection() {
			return direction;
		}

		/**
		 * @return the episode
		 */
		public IEpisode getEpisode() {
			return episode;
		}

		public IEpisode getNewEpisode() {
			return this.newEpisode;
		}

		public HasDateRange getHoveredItem() {
			return hoveredItem;
		}

		public void setHoveredItem(HasDateRange hoveredItem) {
			TimeZoneDateRange newRange;
			try {
				TimeZoneDate start;
				TimeZoneDate end;
				if (this.direction < 0) {
					start = hoveredItem.getDateRange().getStartDate();
					end = episode.getEnd();
				} else {
					start = episode.getStart();
					end = hoveredItem.getDateRange().getEndDate();
				}
				newRange = new TimeZoneDateRange(start, end);
			} catch (InvalidParameterException e) {
				return;
			}
			if (episode.getDateRange().equals(newRange))
				return;

			this.newEpisode = episode.changeRange(newRange);

			this.hoveredItem = hoveredItem;
		}
	}

	private static Converter<IEpisode> CONVERTER = new Converter<IEpisode>() {
		@Override
		public Long getStart(IEpisode episode) {
			return episode.getStart().getTime();
		}

		@Override
		public Long getEnd(IEpisode episode) {
			return episode.getEnd().getTime();
		}
	};

	private static class Renderer implements PaintListener, Listener,
			IDisposable {

		private Map<IEpisode, CodeColors> renderingColors = new HashMap<IEpisode, CodeColors>();

		private ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		/**
		 * Used to display information to the currently hovered item.
		 */
		private ToolTip hoveredItemTooltip;

		/**
		 * Area in which all {@link IEpisode}s must be painted.
		 */
		private final ViewerColumn column;

		/**
		 * Contains the column for the recently painted {@link IEpisode}s.
		 */
		private Map<IEpisode, Rectangle> renderingBounds = null;

		/**
		 * Space between two {@link IEpisode}s.
		 */
		private final int trackSpace;

		/**
		 * Information concerning the active resize action.
		 */
		private ResizeInfo resizeInfo = null;

		private final Cursor resizeTopCursor = new Cursor(Display.getCurrent(),
				SWT.CURSOR_SIZES);
		private final Cursor resizeBottomCursor = new Cursor(
				Display.getCurrent(), SWT.CURSOR_SIZEN);
		private final Cursor handCursor = new Cursor(Display.getCurrent(),
				SWT.CURSOR_HAND);

		@SuppressWarnings("unused")
		private int hShift = 100;

		public Renderer(ViewerColumn column, int trackSpace) {
			this.hoveredItemTooltip = new ToolTip(column.getViewer()
					.getControl().getShell(), SWT.ICON_INFORMATION);
			this.column = column;
			this.trackSpace = trackSpace;
		}

		public void handleEvent(Event event) {

			if (this.renderingBounds == null)
				return;

			final ResizeInfo info = ResizeInfo.getInfoIfApplicable(event,
					column, renderingBounds);
			switch (event.type) {
			case SWT.MouseDown:
				if (info != null && info.direction != 0) {
					this.resizeInfo = info;
					event.widget.setData(CONTROL_DATA_STRING, new Object());
				}
				break;
			case SWT.MouseUp:
				if (this.resizeInfo == null) {
					if (info != null && info.getDirection() == 0) {
						ExecutorUtil.asyncRun(new Runnable() {
							@Override
							public void run() {
								codeService.showCodedObjectInWorkspace(info
										.getEpisode().getCodeInstanceID());
							}
						});
					}
				} else {
					final IEpisode oldEpisode = this.resizeInfo.getEpisode();
					final IEpisode newEpisode = this.resizeInfo.getNewEpisode();
					if (oldEpisode != null && newEpisode != null) {
						ExecutorUtil.asyncRun(new Runnable() {
							@Override
							public void run() {
								try {
									codeService.replaceEpisodeAndSave(
											oldEpisode, newEpisode);
								} catch (CodeServiceException e) {
									LOGGER.error("Error resizing "
											+ IEpisode.class.getSimpleName(), e);
								}
							}
						});
					}
					this.resizeInfo = null;
					event.widget.setData(CONTROL_DATA_STRING, null);
					hoveredItemTooltip.setVisible(false);
					((Control) event.widget).redraw();
					((Control) event.widget).setCursor(null);
				}
				break;
			case SWT.MouseMove:
				if (this.resizeInfo == null) {
					hoveredItemTooltip.setVisible(false);

					Control control = (Control) event.widget;
					if (info != null) {
						if (info.direction > 0) {
							control.setCursor(resizeTopCursor);
						} else if (info.getDirection() < 0) {
							control.setCursor(resizeBottomCursor);
						} else {
							control.setCursor(handCursor);
						}
					} else {
						control.setCursor(null);
					}
				} else {
					Item item = null;

					if (event.widget instanceof Table)
						item = ((Table) event.widget).getItem(new Point(
								event.x, event.y));
					if (event.widget instanceof Tree)
						item = ((Tree) event.widget).getItem(new Point(event.x,
								event.y));

					if (item != null && item.getData() instanceof HasDateRange) {
						this.resizeInfo.setHoveredItem((HasDateRange) item
								.getData());

						Point pt = ((Tree) event.widget).toDisplay(
								event.x + 10, event.y);
						hoveredItemTooltip
								.setText("New episode "
										+ (this.resizeInfo.getDirection() < 0 ? "starts at "
												+ this.resizeInfo
														.getNewEpisode()
														.getStart().toISO8601()
												: "ends at "
														+ this.resizeInfo
																.getNewEpisode()
																.getEnd()
																.toISO8601()));
						hoveredItemTooltip.setLocation(pt);
						hoveredItemTooltip.setVisible(true);

						((Control) event.widget).redraw();
					}
				}
				break;
			}
		}

		@Override
		public void paintControl(PaintEvent e) {
			List<Item> items = com.bkahlert.devel.nebula.utils.ViewerUtils
					.getAllItems((Control) e.widget);
			if (items.size() == 0)
				return;

			Object key = getKey(items);
			if (key == null) {
				LOGGER.error(IEpisode.class.getSimpleName()
						+ "s can currently only be rendered in "
						+ Viewer.class.getSimpleName()
						+ "s that display data from a single "
						+ ID.class.getSimpleName() + " or "
						+ Fingerprint.class.getSimpleName());
				return;
			}

			// Highlight hovered item
			if (this.resizeInfo != null
					&& this.resizeInfo.getHoveredItem() != null) {
				for (Item item : items) {
					if (item.getData() == this.resizeInfo.getHoveredItem()) {
						PaintUtils.drawRoundedBorder(
								e.gc,
								getBounds(item),
								Display.getCurrent().getSystemColor(
										SWT.COLOR_BLACK));
						break;
					}
				}
			}

			// Draw episodes
			this.renderingBounds = getEpisodeBounds(getEpisodes(key), items);
			for (IEpisode episode : renderingBounds.keySet()) {
				if (!renderingColors.containsKey(episode)) {
					try {
						List<ICode> codes = codeService.getCodes(episode);
						if (codes.size() > 0)
							renderingColors.put(episode, new CodeColors(codes
									.get(0).getColor().toClassicRGB()));
					} catch (CodeServiceException e1) {
						LOGGER.error("Could not find the episode's "
								+ ICode.class.getSimpleName() + "s.");
					}

				}

				Rectangle bounds = this.renderingBounds.get(episode);
				e.gc.setAlpha(128);
				CodeColors codeColors = renderingColors.get(episode);
				if (codeColors == null)
					return;
				PaintUtils.drawRoundedRectangle(e.gc, bounds,
						codeColors.getBackgroundColor());

				// draw overlay icons
				try {
					if (codeService.getCodes(episode).size() > 0) {
						Image overlay = ImageManager.OVERLAY_CODED_IMG;
						e.gc.setAlpha(255);
						e.gc.drawImage(
								overlay,
								bounds.x
										+ bounds.width
										- overlay.getBounds().width
										- ((bounds.width >= overlay.getBounds().width + 6) ? 3
												: 0),
								bounds.y
										+ bounds.height
										- overlay.getBounds().height
										+ ((bounds.height >= overlay
												.getBounds().height + 6) ? -3
												: 0)
										- ((bounds.width >= overlay.getBounds().width + 6) ? 0
												: -3));
					}
					if (codeService.isMemo(episode)) {
						Image overlay = ImageManager.OVERLAY_MEMO_IMG;
						e.gc.setAlpha(255);
						e.gc.drawImage(
								overlay,
								bounds.x
										+ bounds.width
										- overlay.getBounds().width
										- ((bounds.width >= overlay.getBounds().width + 6) ? 3
												: 0),
								bounds.y
										+ ((bounds.height >= overlay
												.getBounds().height + 6) ? 3
												: 0)
										- ((bounds.width >= overlay.getBounds().width + 6) ? 0
												: 3));
					}
				} catch (CodeServiceException e1) {
					LOGGER.warn("Error drawing overlays for " + episode, e1);
				}
			}
		}

		/**
		 * Returns the key ({@link ID} or {@link Fingerprint} contained in the
		 * given {@link Item}.
		 * 
		 * @param items
		 * @return null if no or more than one keys are contained
		 */
		public static Object getKey(List<Item> items) {
			Object key = null;
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
						return null;
					}
				} else {
					return null;
				}
			}
			return key;
		}

		private Set<IEpisode> getEpisodes(Object key) {
			if (key instanceof ID)
				return codeService.getEpisodes((ID) key);
			else
				return codeService.getEpisodes((Fingerprint) key);
		}

		/**
		 * Returns the column in which the corresponding {@link IEpisode} should
		 * be rendered.
		 * 
		 * @param set
		 * @param items
		 * @return
		 */
		private Map<IEpisode, Rectangle> getEpisodeBounds(Set<IEpisode> set,
				List<Item> items) {
			if (this.resizeInfo != null) {
				set.remove(this.resizeInfo.getEpisode());
				if (this.resizeInfo.getNewEpisode() != null)
					set.add(this.resizeInfo.getNewEpisode());
			}

			// we need to shift the episodes horizontally if we don't want them
			// to overlap; tracks contains the numbers from 0 to n whereas the
			// number is the track/lane on which the episode needs to be
			// rendered
			ITrackCalculation<IEpisode> tracks = TrackCalculator
					.calculateTracks(new LinkedList<IEpisode>(set), CONVERTER);

			Rectangle columnBounds = com.bkahlert.devel.nebula.utils.ViewerUtils
					.getBounds(column);

			Map<IEpisode, Rectangle> episodeBounds = new HashMap<IEpisode, Rectangle>();
			for (Item item : items) {
				// we are only interested in top level items
				// FIXME: DiffFileRecords end (= file save moments) before their
				// corresponding DiffFileRecord end (= build moment).
				// When calculating the intersections DiffFileRecords always
				// intersect the previous DiffFile making the Episode look one
				// DiffFile longer.
				if (item instanceof TreeItem
						&& ((TreeItem) item).getParentItem() != null)
					continue;

				if (item.getData() instanceof HasDateRange) {
					TimeZoneDateRange range = ((HasDateRange) item.getData())
							.getDateRange();
					for (IEpisode episode : set) {
						if (range.isIntersected2(episode.getDateRange())) {
							Rectangle currentBounds = getBounds(item);

							currentBounds.width = (columnBounds.width - this.trackSpace
									* (tracks.getMaxTracks() - 1))
									/ (tracks.getNumTracks(episode));
							currentBounds.width = (columnBounds.width - this.trackSpace
									* (tracks.getMaxTracks() - 1))
									/ (tracks.getMaxTracks());
							currentBounds.x = columnBounds.x
									+ tracks.getTrack(episode)
									* (currentBounds.width + this.trackSpace);

							// alter currentBounds so it reflects the height of
							// the item itself plus all its children
							Rectangle deepestChildBounds = getBounds(getDeepestChildItem(item));
							currentBounds.height = deepestChildBounds.y
									+ deepestChildBounds.height
									- currentBounds.y;

							if (!episodeBounds.containsKey(episode))
								episodeBounds.put(episode, currentBounds);
							else {
								Rectangle bounds = episodeBounds.get(episode);
								bounds.y = Math.min(bounds.y, currentBounds.y);
								bounds.height = Math.max(bounds.height,
										currentBounds.height + currentBounds.y
												- bounds.y);
							}
						}
					}
				}
			}
			return episodeBounds;
		}

		private static Rectangle getBounds(Item item) {
			return item instanceof TreeItem ? ((TreeItem) item).getBounds()
					: ((TableItem) item).getBounds();
		}

		private static Item getDeepestChildItem(Item item) {
			if (!(item instanceof TreeItem))
				return item;
			TreeItem[] treeItems = ((TreeItem) item).getItems();
			if (treeItems == null || treeItems.length == 0)
				return item;
			return getDeepestChildItem(treeItems[treeItems.length - 1]);
		}

		@Override
		public void dispose() {
			if (this.resizeTopCursor != null
					&& !this.resizeTopCursor.isDisposed())
				this.resizeTopCursor.dispose();
			if (this.resizeBottomCursor != null
					&& !this.resizeBottomCursor.isDisposed())
				this.resizeBottomCursor.dispose();
		}
	}

	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		private void redraw() {
			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (viewer.getControl() != null
							&& !viewer.getControl().isDisposed())
						viewer.getControl().redraw();
				}
			});
		}

		public void memoModified(
				de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable codeable) {
			redraw();
		};

		public void episodeAdded(IEpisode episode) {
			redraw();
		};

		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			redraw();
		};

		public void episodesDeleted(Set<IEpisode> episodes) {
			redraw();
		};
	};

	private Renderer renderer;
	private ColumnViewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	private EpisodeRenderer(ColumnViewer viewer, ViewerColumn column,
			int trackSpace) {
		this.viewer = viewer;
		this.renderer = new Renderer(column, trackSpace);
	}

	public EpisodeRenderer(AbstractTableViewer viewer, ViewerColumn column,
			int trackSpace) {
		this((ColumnViewer) viewer, column, trackSpace);
	}

	public EpisodeRenderer(AbstractTreeViewer viewer, ViewerColumn column,
			int trackSpace) {
		this((ColumnViewer) viewer, column, trackSpace);
	}

	public void activateRendering() {
		this.viewer.getControl().addListener(SWT.MeasureItem, renderer);
		this.viewer.getControl().addListener(SWT.PaintItem, renderer);
		this.viewer.getControl().addListener(SWT.MouseDown, renderer);
		this.viewer.getControl().addListener(SWT.MouseUp, renderer);
		this.viewer.getControl().addListener(SWT.MouseMove, renderer);
		this.viewer.getControl().addPaintListener(renderer);
		this.codeService.addCodeServiceListener(codeServiceListener);
	}

	public void deactivateRendering() {
		this.codeService.removeCodeServiceListener(codeServiceListener);
		this.viewer.getControl().removePaintListener(renderer);
		this.viewer.getControl().removeListener(SWT.MouseMove, renderer);
		this.viewer.getControl().removeListener(SWT.MouseUp, renderer);
		this.viewer.getControl().removeListener(SWT.MouseDown, renderer);
		this.viewer.getControl().removeListener(SWT.PaintItem, renderer);
		this.viewer.getControl().removeListener(SWT.MeasureItem, renderer);
	}

	@Override
	public void dispose() {
		if (this.renderer != null)
			this.renderer.dispose();
	}
}
