package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
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

import com.bkahlert.nebula.rendering.TrackCalculator;
import com.bkahlert.nebula.rendering.TrackCalculator.Converter;
import com.bkahlert.nebula.rendering.TrackCalculator.ITrackCalculation;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.PaintUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.core.util.GeometryUtils;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;

public class EpisodeRenderer implements IDisposable {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeRenderer.class);

	private static Image CODED_OVERLAY = ImageManager.OVERLAY_CODED
			.getImageDescriptor().createImage();
	private static Image MEMO_OVERLAY = ImageManager.OVERLAY_MEMO
			.getImageDescriptor().createImage();

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
			Display.getCurrent(), new org.eclipse.swt.graphics.RGB(85, 85, 85));

	private static class ResizeInfo {
		public static ResizeInfo getInfoIfApplicable(Event event,
				ViewerColumn column, Map<IEpisode, Rectangle> episodeBounds) {
			Rectangle bounds = com.bkahlert.nebula.utils.ViewerUtils
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

		private final int direction;
		private final IEpisode episode;
		private IEpisode newEpisode;
		private URI hoveredUri;

		public ResizeInfo(int direction, IEpisode episode) {
			super();
			this.direction = direction;
			this.episode = episode;
			this.newEpisode = null;
			this.hoveredUri = null;
		}

		/**
		 * @return 1 for upwards direction and -1 for downwards
		 */
		public int getDirection() {
			return this.direction;
		}

		/**
		 * @return the episode
		 */
		public IEpisode getEpisode() {
			return this.episode;
		}

		public IEpisode getNewEpisode() {
			return this.newEpisode;
		}

		public URI getHoveredUri() {
			return this.hoveredUri;
		}

		public void setHoveredItem(URI uri) {
			try {
				ILocatable hoveredItem = LocatorService.INSTANCE.resolve(uri,
						null).get();
				if (hoveredItem instanceof HasDateRange) {
					TimeZoneDateRange newRange;
					TimeZoneDate start;
					TimeZoneDate end;
					if (this.direction < 0) {
						start = ((HasDateRange) hoveredItem).getDateRange()
								.getStartDate();
						end = this.episode.getEnd();
					} else {
						start = this.episode.getStart();
						end = ((HasDateRange) hoveredItem).getDateRange()
								.getEndDate();
					}

					try {
						newRange = new TimeZoneDateRange(start, end);
					} catch (InvalidParameterException e) {
						return;
					}

					if (this.episode.getDateRange().equals(newRange)) {
						return;
					}

					this.newEpisode = this.episode.changeRange(newRange);
				}
				this.hoveredUri = uri;
			} catch (Exception e) {
				LOGGER.error("Error setting hovered item to " + uri, e);
			}
		}
	}

	private static Converter<IEpisode> CONVERTER = new Converter<IEpisode>() {
		@Override
		public Long getStart(IEpisode episode) {
			if (episode == null || episode.getStart() == null) {
				return Long.MIN_VALUE;
			}
			return episode.getStart().getTime();
		}

		@Override
		public Long getEnd(IEpisode episode) {
			if (episode == null || episode.getEnd() == null) {
				return Long.MAX_VALUE;
			}
			return episode.getEnd().getTime();
		}
	};

	private static class Renderer implements PaintListener, Listener,
			IDisposable {

		private final Map<IEpisode, GTLabelProvider.CodeColors> renderingColors = new HashMap<IEpisode, GTLabelProvider.CodeColors>();

		private final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		/**
		 * Used to display information to the currently hovered item.
		 */
		private final ToolTip hoveredItemTooltip;

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
		private final int hShift = 100;

		public Renderer(ViewerColumn column, int trackSpace) {
			this.hoveredItemTooltip = new ToolTip(column.getViewer()
					.getControl().getShell(), SWT.ICON_INFORMATION);
			this.column = column;
			this.trackSpace = trackSpace;
		}

		@Override
		public void handleEvent(final Event event) {

			if (this.renderingBounds == null) {
				return;
			}

			final ResizeInfo info = ResizeInfo.getInfoIfApplicable(event,
					this.column, this.renderingBounds);
			switch (event.type) {
			case SWT.MouseDown:
				if (info != null && info.direction != 0) {
					this.resizeInfo = info;
				}
				break;
			case SWT.MouseUp:
				if (this.resizeInfo == null) {
					if (info != null && info.getDirection() == 0) {
						LocatorService.INSTANCE.showInWorkspace(info
								.getEpisode().getUri(), false, null);
					}
				} else {
					final IEpisode oldEpisode = this.resizeInfo.getEpisode();
					final IEpisode newEpisode = this.resizeInfo.getNewEpisode();
					if (oldEpisode != null && newEpisode != null) {
						ExecUtils.nonUISyncExec(EpisodeRenderer.class,
								"Setting Episode", new Runnable() {
									@Override
									public void run() {
										try {
											Renderer.this.codeService
													.replaceEpisodeAndSave(
															oldEpisode,
															newEpisode);
										} catch (CodeServiceException e) {
											LOGGER.error(
													"Error resizing "
															+ IEpisode.class
																	.getSimpleName(),
													e);
										}

										((Control) event.widget).redraw();
										((Control) event.widget)
												.setCursor(null);
									}
								});
					}
					this.resizeInfo = null;
					this.hoveredItemTooltip.setVisible(false);
				}
				break;
			case SWT.MouseMove:
				if (info != null) {
					event.widget.setData(CONTROL_DATA_STRING, new Object());
				} else {
					event.widget.setData(CONTROL_DATA_STRING, null);
				}

				if (this.resizeInfo == null) {
					this.hoveredItemTooltip.setVisible(false);

					Control control = (Control) event.widget;
					if (info != null) {
						if (info.direction > 0) {
							control.setCursor(this.resizeTopCursor);
						} else if (info.getDirection() < 0) {
							control.setCursor(this.resizeBottomCursor);
						} else {
							control.setCursor(this.handCursor);
						}
					} else {
						control.setCursor(null);
					}
				} else {
					Item item = null;

					if (event.widget instanceof Table) {
						item = ((Table) event.widget).getItem(new Point(
								event.x, event.y));
					}
					if (event.widget instanceof Tree) {
						item = ((Tree) event.widget).getItem(new Point(event.x,
								event.y));
					}

					if (item != null && item.getData() instanceof URI) {
						this.resizeInfo.setHoveredItem((URI) item.getData());

						Point pt = ((Tree) event.widget).toDisplay(
								event.x + 10, event.y);
						String tooltip;
						if (this.resizeInfo.getNewEpisode() == null) {
							tooltip = "Invalid";
						} else if (this.resizeInfo.getDirection() < 0) {
							tooltip = "New episode starts at "
									+ (this.resizeInfo.getNewEpisode()
											.getStart() != null ? this.resizeInfo
											.getNewEpisode().getStart()
											.toISO8601()
											: "-∞");
						} else {
							tooltip = "New episode ends at "
									+ (this.resizeInfo.getNewEpisode().getEnd() != null ? this.resizeInfo
											.getNewEpisode().getEnd()
											.toISO8601()
											: "+∞");
						}
						this.hoveredItemTooltip.setText(tooltip);
						this.hoveredItemTooltip.setLocation(pt);
						this.hoveredItemTooltip.setVisible(true);

						((Control) event.widget).redraw();
					}
				}
				break;
			}
		}

		@Override
		public void paintControl(PaintEvent e) {
			List<Item> items = com.bkahlert.nebula.utils.TreeTableUtils
					.getAllItems((Control) e.widget);
			if (items.size() == 0) {
				return;
			}

			Set<IIdentifier> identifiers = getIdentifiers(items);
			if (identifiers.size() != 1) {
				LOGGER.error(IEpisode.class.getSimpleName()
						+ "s can currently only be rendered in "
						+ Viewer.class.getSimpleName()
						+ "s that display data from a single "
						+ IIdentifier.class.getSimpleName());
				return;
			}

			// Highlight hovered item
			if (this.resizeInfo != null
					&& this.resizeInfo.getHoveredUri() != null) {
				for (Item item : items) {
					if (item.getData() == this.resizeInfo.getHoveredUri()) {
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
			this.renderingBounds = this.getEpisodeBounds(
					this.getEpisodes(identifiers.iterator().next()), items);
			for (IEpisode episode : this.renderingBounds.keySet()) {
				// remove all outdated colors (e.g. because episode got a new
				// color with different color)
				if (this.renderingColors.containsKey(episode)) {
					List<ICode> codes = this.codeService.getCodes(episode
							.getUri());
					GTLabelProvider.CodeColors renderingColor = this.renderingColors
							.get(episode);
					if (codes.size() == 0
							|| !codes.get(0).getColor()
									.equals(renderingColor.getBackgroundRGB())) {
						renderingColor.dispose();
						this.renderingColors.remove(episode);
					}
				}

				// create all missing colors
				if (!this.renderingColors.containsKey(episode)) {
					List<ICode> codes = this.codeService.getCodes(episode
							.getUri());
					if (codes.size() > 0) {
						this.renderingColors.put(episode,
								new GTLabelProvider.CodeColors(codes.get(0)
										.getColor()));
					} else {
						this.renderingColors.put(episode,
								new GTLabelProvider.CodeColors(null));
					}

				}

				Rectangle bounds = this.renderingBounds.get(episode);
				e.gc.setAlpha(128);
				GTLabelProvider.CodeColors codeColors = this.renderingColors
						.get(episode);
				if (codeColors == null) {
					LOGGER.warn("Could not paint episode because it has no color; "
							+ episode);
					return;
				}
				PaintUtils.drawRoundedRectangle(e.gc, bounds,
						codeColors.getBackgroundColor());

				if (this.codeService.getCodes(episode.getUri()).size() > 0) {
					e.gc.setAlpha(255);
					e.gc.drawImage(
							CODED_OVERLAY,
							bounds.x
									+ bounds.width
									- CODED_OVERLAY.getBounds().width
									- ((bounds.width >= CODED_OVERLAY
											.getBounds().width + 6) ? 3 : 0),
							bounds.y
									+ bounds.height
									- CODED_OVERLAY.getBounds().height
									+ ((bounds.height >= CODED_OVERLAY
											.getBounds().height + 6) ? -3 : 0)
									- ((bounds.width >= CODED_OVERLAY
											.getBounds().width + 6) ? 0 : -3));
				}
				if (this.codeService.isMemo(episode.getUri())) {
					e.gc.setAlpha(255);
					e.gc.drawImage(
							MEMO_OVERLAY,
							bounds.x
									+ bounds.width
									- MEMO_OVERLAY.getBounds().width
									- ((bounds.width >= MEMO_OVERLAY
											.getBounds().width + 6) ? 3 : 0),
							bounds.y
									+ ((bounds.height >= MEMO_OVERLAY
											.getBounds().height + 6) ? 3 : 0)
									- ((bounds.width >= MEMO_OVERLAY
											.getBounds().width + 6) ? 0 : 3));
				}
			}
		}

		/**
		 * Returns the key ({@link IIdentifier} contained in the given
		 * {@link Item}s.
		 * 
		 * @param items
		 * @return null if no or more than one keys are contained
		 */
		public static Set<IIdentifier> getIdentifiers(List<Item> items) {
			Set<IIdentifier> identifiers = new HashSet<IIdentifier>();
			for (Item item : items) {
				if (item.getData() instanceof URI) {
					IIdentifier currentIdentifier = URIUtils
							.getIdentifier((URI) item.getData());
					if (currentIdentifier != null
							&& !identifiers.contains(currentIdentifier)) {
						identifiers.add(currentIdentifier);
					}
				}
			}
			return identifiers;
		}

		private Set<IEpisode> getEpisodes(IIdentifier key) {
			return this.codeService.getEpisodes(key);
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
				if (this.resizeInfo.getNewEpisode() != null) {
					set.add(this.resizeInfo.getNewEpisode());
				}
			}

			// we need to shift the episodes horizontally if we don't want them
			// to overlap; tracks contains the numbers from 0 to n whereas the
			// number is the track/lane on which the episode needs to be
			// rendered
			ITrackCalculation<IEpisode> tracks = TrackCalculator
					.calculateTracks(new LinkedList<IEpisode>(set), CONVERTER);

			Rectangle columnBounds = com.bkahlert.nebula.utils.ViewerUtils
					.getBounds(this.column);

			Map<IEpisode, Rectangle> episodeBounds = new HashMap<IEpisode, Rectangle>();
			for (Item item : items) {
				// we are only interested in top level items
				// FIXME: DiffFileRecords end (= file save moments) before their
				// corresponding DiffFileRecord end (= build moment).
				// When calculating the intersections DiffFileRecords always
				// intersect the previous DiffFile making the Episode look one
				// DiffFile longer.
				if (item instanceof TreeItem
						&& ((TreeItem) item).getParentItem() != null) {
					continue;
				}

				ILocatable locatable = null;
				if (item.getData() instanceof ILocatable) {
					locatable = (ILocatable) item.getData();
				} else if (item.getData() instanceof URI) {
					try {
						locatable = LocatorService.INSTANCE.resolve(
								(URI) item.getData(), null).get();
					} catch (Exception e) {
						LOGGER.error("Can't resolve " + item.getData()
								+ " for rendering", e);
					}
				}

				if (locatable instanceof HasDateRange) {
					TimeZoneDateRange range = ((HasDateRange) locatable)
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

							if (!episodeBounds.containsKey(episode)) {
								episodeBounds.put(episode, currentBounds);
							} else {
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
			if (!(item instanceof TreeItem)) {
				return item;
			}
			TreeItem[] treeItems = ((TreeItem) item).getItems();
			if (treeItems == null || treeItems.length == 0) {
				return item;
			}
			return getDeepestChildItem(treeItems[treeItems.length - 1]);
		}

		@Override
		public void dispose() {
			if (this.resizeTopCursor != null
					&& !this.resizeTopCursor.isDisposed()) {
				this.resizeTopCursor.dispose();
			}
			if (this.resizeBottomCursor != null
					&& !this.resizeBottomCursor.isDisposed()) {
				this.resizeBottomCursor.dispose();
			}
		}
	}

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		private void redraw() {
			ExecUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (EpisodeRenderer.this.viewer.getControl() != null
							&& !EpisodeRenderer.this.viewer.getControl()
									.isDisposed()) {
						EpisodeRenderer.this.viewer.getControl().redraw();
					}
				}
			});
		}

		@Override
		public void memoModified(URI uri) {
			this.redraw();
		};

		@Override
		public void episodeAdded(IEpisode episode) {
			this.redraw();
		};

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			this.redraw();
		};

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			this.redraw();
		};
	};

	private final Renderer renderer;
	private final ColumnViewer viewer;
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

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
		this.viewer.getControl().addListener(SWT.MeasureItem, this.renderer);
		this.viewer.getControl().addListener(SWT.PaintItem, this.renderer);
		this.viewer.getControl().addListener(SWT.MouseDown, this.renderer);
		this.viewer.getControl().addListener(SWT.MouseUp, this.renderer);
		this.viewer.getControl().addListener(SWT.MouseMove, this.renderer);
		this.viewer.getControl().addPaintListener(this.renderer);
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	public void deactivateRendering() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		this.viewer.getControl().removePaintListener(this.renderer);
		this.viewer.getControl().removeListener(SWT.MouseMove, this.renderer);
		this.viewer.getControl().removeListener(SWT.MouseUp, this.renderer);
		this.viewer.getControl().removeListener(SWT.MouseDown, this.renderer);
		this.viewer.getControl().removeListener(SWT.PaintItem, this.renderer);
		this.viewer.getControl().removeListener(SWT.MeasureItem, this.renderer);
	}

	@Override
	public void dispose() {
		if (this.renderer != null) {
			this.renderer.dispose();
		}
	}
}
