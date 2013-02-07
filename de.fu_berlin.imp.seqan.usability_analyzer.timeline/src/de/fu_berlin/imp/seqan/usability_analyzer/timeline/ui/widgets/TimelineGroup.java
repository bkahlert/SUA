package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.impl.MultiSourceTimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

/**
 * This widget display one or more {@link Timeline}s.
 * 
 * @author bkahlert
 * 
 *         TODO TimelineCompositeViewer implementieren und
 *         {@link ISelectionChangedListener} implementieren lassen
 * 
 */
public class TimelineGroup<TIMELINE extends ITimeline> extends Composite
		implements ITimelineGroup<TIMELINE> {

	private static final Logger LOGGER = Logger.getLogger(TimelineGroup.class);

	private static final String VIEWER_DATA = "VIEWER";

	private ITimelineFactory<TIMELINE> timelineFactory;
	private ITimelineProviderFactory<TIMELINE> timelineProviderFactory;

	private ListenerList timelineListeners = new ListenerList();

	private ITimelineListener timelineListenerDelegate = new ITimelineListener() {
		@Override
		public void clicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).clicked(event);
			}
		}

		@Override
		public void middleClicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).middleClicked(event);
			}
		}

		@Override
		public void rightClicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).rightClicked(event);
			}
		}

		@Override
		public void doubleClicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).doubleClicked(event);
			}
		}

		@Override
		public void hoveredIn(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).hoveredIn(event);
			}
		}

		@Override
		public void hoveredOut(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).hoveredOut(event);
			}
		}
	};

	/**
	 * TODO Find a more elegant solution to notice when one of the timelines got
	 * the focus.
	 * 
	 * @param parent
	 * @param style
	 * @param timelineBandProviders
	 */
	public TimelineGroup(Composite parent, int style,
			ITimelineFactory<TIMELINE> timelineFactory,
			ITimelineProviderFactory<TIMELINE> timelineProviderFactory) {
		super(parent, style);
		super.setLayout(new FillLayout(SWT.VERTICAL));

		Assert.isNotNull(timelineFactory);
		Assert.isNotNull(timelineProviderFactory);
		this.timelineFactory = timelineFactory;
		this.timelineProviderFactory = timelineProviderFactory;
	}

	/**
	 * Displays the contents associated with the given keys.
	 * <p>
	 * For this every key gets one {@link SelectionTimeline}. Each
	 * {@link SelectionTimeline} will be filled with the contents provided by
	 * the {@link ITimelineBandProvider} s passed in the constructor.
	 * 
	 * @param keys
	 * @param progressMonitor
	 * @param success
	 * @return
	 */
	public <T> Future<T> load(Set<Object> keys,
			IProgressMonitor progressMonitor, final Callable<T> success) {

		final SubMonitor monitor = SubMonitor.convert(progressMonitor,
				2 + (10 * keys.size()) + 1);

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		Object[] unpreparedKeys = prepareTimelines(keys);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		monitor.worked(2);

		for (final Object key : unpreparedKeys) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			TIMELINE timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<TIMELINE>() {
					@Override
					public TIMELINE call() throws Exception {
						return getTimeline(key);
					}
				}).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving "
						+ Timeline.class.getSimpleName() + " for " + key);
				continue;
			}

			if (timeline == null) {
				// timeline was not prepared -> create a new one
				try {
					timeline = ExecutorUtil.syncExec(new Callable<TIMELINE>() {
						@Override
						public TIMELINE call() throws Exception {
							final TIMELINE timeline = TimelineGroup.this.timelineFactory
									.createTimeline(TimelineGroup.this,
											SWT.NONE);
							timeline.setData(key);
							timeline.addTimelineListener(timelineListenerDelegate);
							timeline.addDisposeListener(new DisposeListener() {
								@Override
								public void widgetDisposed(DisposeEvent e) {
									timeline.addTimelineListener(timelineListenerDelegate);
								}
							});
							return timeline;
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error creating " + Timeline.class + " for "
							+ key);
					continue;
				}
			} else {
				// a no more needed timeline was associated with an unprepared
				// key
			}

			monitor.worked(2);

			// init timeline viewer
			final MultiSourceTimelineViewer<TIMELINE> timelineViewer = new MultiSourceTimelineViewer<TIMELINE>(
					timeline);
			ExecutorUtil.syncExec(new Runnable() {
				public void run() {
					setTimelineViewer(key, timelineViewer);
				}
			});
			timelineViewer.setTimelineProvider(timelineProviderFactory
					.createTimelineProvider());
			timelineViewer.setInput(key);
			timelineViewer.refresh(monitor.newChild(8));

			if (monitor.isCanceled()) {
				disposeTimelines(key);
				throw new OperationCanceledException();
			}
		}

		Future<T> rs = ExecutorUtil.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				TimelineGroup.this.layout();
				if (success != null)
					return success.call();
				return null;
			}
		});

		monitor.worked(1);
		monitor.done();

		return rs;
	}

	/**
	 * Highlights the given date ranges in the timelines and centers them
	 * correctly.
	 * 
	 * @param groupedDateRanges
	 * @param progressMonitor
	 */
	public void refresh(
			final Map<Object, List<TimeZoneDateRange>> groupedDateRanges,
			IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("Updading " + ITimeline.class.getSimpleName()
				+ "s", groupedDateRanges.keySet().size());
		for (final Object key : groupedDateRanges.keySet()) {
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();

			final TIMELINE timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<TIMELINE>() {
					@Override
					public TIMELINE call() throws Exception {
						return getTimeline(key);
					}
				}).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving "
						+ Timeline.class.getSimpleName() + " for " + key);
				continue;
			}
			if (timeline == null) {
				LOGGER.warn(Timeline.class.getSimpleName()
						+ " does not exist anymore for " + key);
				continue;
			}

			if (timeline instanceof Timeline) {
				final List<TimeZoneDateRange> dateRanges = groupedDateRanges
						.get(key);

				if (progressMonitor.isCanceled())
					throw new OperationCanceledException();

				ExecutorUtil.asyncExec(new Runnable() {
					@Override
					public void run() {
						TimeZoneDate center = dateRanges.get(0).getStartDate() != null ? dateRanges
								.get(0).getStartDate() : dateRanges.get(0)
								.getEndDate();
						if (center != null && center.getCalendar() != null)
							timeline.setCenterVisibleDate(center.getCalendar());
						((Timeline) timeline).highlight(dateRanges);
					}
				});
			}

			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
		}

		progressMonitor.done();
	}

	public Set<Object> getTimelineKeys() {
		final Set<Object> keys = new HashSet<Object>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (Control control : TimelineGroup.this.getChildren()) {
					if (!control.isDisposed() && control instanceof Timeline) {
						keys.add(control.getData());
					}
				}
			}
		});
		return keys;
	}

	/**
	 * Returns the {@link Timeline} that is associated with the given key.
	 * 
	 * @UI must be called from the UI thread
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TIMELINE getTimeline(final Object key) {
		Assert.isNotNull(key);
		for (Control control : this.getChildren()) {
			if (!control.isDisposed() && control instanceof IBaseTimeline) {
				IBaseTimeline timeline = (IBaseTimeline) control;
				if (key.equals(timeline.getData())) {
					try {
						return (TIMELINE) timeline;
					} catch (Exception e) {
						LOGGER.fatal("Could not cast to generic type. "
								+ "It should never have happened that an "
								+ "incompatible timeline type was used.", e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link ITimelineViewer} that belongs to the {@link Timeline}
	 * associated with the given key.
	 * 
	 * @UI must be called from the UI thread
	 * @param key
	 * @return
	 */
	public ITimelineViewer getTimelineViewer(final Object key) {
		Assert.isNotNull(key);
		TIMELINE timeline = getTimeline(key);
		return (ITimelineViewer) timeline.getData(VIEWER_DATA);
	}

	/**
	 * Returns the {@link ITimelineViewer} that belongs to the {@link Timeline}
	 * associated with the given key.
	 * 
	 * @UI must be called from the UI thread
	 * @param key
	 * @return
	 */
	public ITimelineViewer getTimelineViewer(final Timeline timeline) {
		Assert.isNotNull(timeline);
		return (ITimelineViewer) timeline.getData(VIEWER_DATA);
	}

	/**
	 * Sets the {@link ITimelineViewer} that is used with the {@link Timeline}
	 * associated with the given key.
	 * 
	 * @param key
	 * @param timelineViewer
	 */
	public void setTimelineViewer(final Object key,
			ITimelineViewer timelineViewer) {
		Assert.isNotNull(key);
		TIMELINE timeline = getTimeline(key);
		timeline.setData(VIEWER_DATA, timelineViewer);
	}

	/**
	 * Prepares already instantiated {@link Timeline}s in the following way:
	 * <ol>
	 * <li>{@link SelectionTimeline}s already identified with a given key stay
	 * untouched since they are already working
	 * <li>the other {@link SelectionTimeline}s are associated with a new key
	 * since the key they are still identified with if no longer requested
	 * <li>{@link SelectionTimeline}s that are not needed anymore become
	 * disposed (since all requested keys are treated already)
	 * <li>all newly assigned keys and unassigned keys are returned (so the
	 * caller can load the actual contents)
	 * </ol>
	 * 
	 * @param usedTimelineKeys
	 * @return keys that were not associated to an existing
	 *         {@link SelectionTimeline} or were associated with a
	 *         {@link SelectionTimeline} that before was responsible for another
	 *         key
	 */
	private Object[] prepareTimelines(Set<Object> keys) {
		List<Object> neededTimelines = new LinkedList<Object>(keys);
		List<Object> existingTimelines = new LinkedList<Object>(
				getTimelineKeys());
		List<?> preparedTimelines = ListUtils.intersection(existingTimelines,
				neededTimelines);
		final List<?> unpreparedTimelines = ListUtils.subtract(neededTimelines,
				preparedTimelines);
		final List<?> freeTimelines = ListUtils.subtract(existingTimelines,
				preparedTimelines);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (freeTimelines.size() > 0
						&& unpreparedTimelines.size() > i) {
					try {
						TIMELINE timeline = getTimeline(freeTimelines.remove(0));
						timeline.setData(unpreparedTimelines.get(i));
					} catch (Exception e) {
						LOGGER.error("Error assigning new key "
								+ unpreparedTimelines.get(i) + " to "
								+ Timeline.class);
					}
				}
			}
		});
		disposeTimelines(freeTimelines.toArray());
		return unpreparedTimelines.toArray();
	}

	/**
	 * Disposes all {@link SelectionTimeline}s that are identified by at least
	 * one of the provided keys.
	 * 
	 * @param timelineKeys
	 */
	private void disposeTimelines(final Object... timelineKeys) {
		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				for (Object timelineKey : timelineKeys) {
					try {
						TIMELINE timeline = getTimeline(timelineKey);
						if (timeline != null && !timeline.isDisposed())
							timeline.dispose();
					} catch (Exception e) {
						LOGGER.error("Error disposing " + Timeline.class);
					}
				}
			}
		});
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TimelineGroup.this.layout();
			}
		});
	}

	@Override
	public void setLayout(Layout layout) {
		return;
	}

	@Override
	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.add(timelineListener);
	}

	@Override
	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.remove(timelineListener);
	}

	@Override
	public <T> Future<T> show(Set<ITimelineInput> inputs,
			IProgressMonitor monitor, Callable<T> success) {
		// TODO Auto-generated method stub
		return null;
	}

}
