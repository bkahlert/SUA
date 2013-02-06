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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.devel.nebula.utils.SelectionProviderDelegator;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer.MultiSourceTimelineViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer.TimelineLabelProvider;

/**
 * This widget display one or more {@link Timeline}s.
 * 
 * @author bkahlert
 * 
 *         TODO TimelineCompositeViewer implementieren und
 *         {@link ISelectionChangedListener} implementieren lassen
 * 
 */
public class TimelinesComposite extends Composite implements ISelectionProvider {

	private static final Logger LOGGER = Logger
			.getLogger(TimelinesComposite.class);

	private static final String VIEWER_DATA = "VIEWER";

	private List<ITimelineBandProvider> timelineBandProviders;
	private SelectionProviderDelegator selectionProviderDelegator = new SelectionProviderDelegator();

	/**
	 * TODO Find a more elegant solution to notice when one of the timelines got
	 * the focus.
	 * 
	 * @param parent
	 * @param style
	 * @param timelineBandProviders
	 */
	public TimelinesComposite(Composite parent, int style,
			List<ITimelineBandProvider> timelineBandProviders) {
		super(parent, style);
		super.setLayout(new FillLayout(SWT.VERTICAL));
		this.timelineBandProviders = timelineBandProviders;
		parent.getDisplay().addFilter(SWT.FocusIn, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Control))
					return;
				Control control = (Control) event.widget;
				while (control != null) {
					if (control instanceof Timeline) {
						selectionProviderDelegator
								.setSelectionProvider(getTimelineViewer((Timeline) control));
						return;
					}
					control = control.getParent();
				}
			}
		});
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
				2 + (keys.size() * timelineBandProviders.size()) + 1);

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

			Timeline timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<Timeline>() {
					@Override
					public Timeline call() throws Exception {
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
					timeline = ExecutorUtil.syncExec(new Callable<Timeline>() {
						@Override
						public Timeline call() throws Exception {
							Timeline timeline = new Timeline(
									TimelinesComposite.this, SWT.NONE);
							timeline.setData(key);
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

			// init timeline viewer
			final MultiSourceTimelineViewer timelineViewer = new MultiSourceTimelineViewer(
					timeline);
			ExecutorUtil.syncExec(new Runnable() {
				public void run() {
					setTimelineViewer(key, timelineViewer);
				}
			});
			timelineViewer.setTimelineLabelProvider(new TimelineLabelProvider(
					key));
			timelineViewer.setProviders(this.timelineBandProviders);
			timelineViewer.setInput(key);
			timelineViewer.refresh(monitor.newChild(this.timelineBandProviders
					.size()));

			if (monitor.isCanceled()) {
				disposeTimelines(key);
				throw new OperationCanceledException();
			}
		}

		Future<T> rs = ExecutorUtil.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				TimelinesComposite.this.layout();
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

			final Timeline timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<Timeline>() {
					@Override
					public Timeline call() throws Exception {
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
					timeline.highlight(dateRanges);
				}
			});

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
				for (Control control : TimelinesComposite.this.getChildren()) {
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
	public Timeline getTimeline(final Object key) {
		Assert.isNotNull(key);
		for (Control control : this.getChildren()) {
			if (!control.isDisposed() && control instanceof Timeline) {
				Timeline timeline = (Timeline) control;
				if (key.equals(timeline.getData())) {
					return timeline;
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
		Timeline timeline = getTimeline(key);
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
		Timeline timeline = getTimeline(key);
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
						Timeline timeline = getTimeline(freeTimelines.remove(0));
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
						Timeline selectionTimeline = getTimeline(timelineKey);
						if (selectionTimeline != null
								&& !selectionTimeline.isDisposed())
							selectionTimeline.dispose();
					} catch (Exception e) {
						LOGGER.error("Error disposing " + Timeline.class);
					}
				}
			}
		});
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TimelinesComposite.this.layout();
			}
		});
	}

	@Override
	public void setLayout(Layout layout) {
		return;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.selectionProviderDelegator.addSelectionChangedListener(listener);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.selectionProviderDelegator
				.removeSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.selectionProviderDelegator.getSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		this.selectionProviderDelegator.setSelection(selection);
	}

}
