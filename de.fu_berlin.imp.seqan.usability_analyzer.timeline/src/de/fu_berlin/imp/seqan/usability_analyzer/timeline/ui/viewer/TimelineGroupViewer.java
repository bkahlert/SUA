package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.viewer.timelineGroup.ITimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

public abstract class TimelineGroupViewer<TIMELINEGROUP extends ITimelineGroup<? extends ITimeline>>
		extends Viewer implements ITimelineGroupViewer {

	private TIMELINEGROUP timelineGroup;

	private ISelection selection = null;
	private ITimelineListener timelineListener = new ITimelineListener() {
		@Override
		public void clicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void middleClicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void rightClicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void doubleClicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void hoveredIn(TimelineEvent event) {
		}

		@Override
		public void hoveredOut(TimelineEvent event) {
		}
	};

	public TimelineGroupViewer(TIMELINEGROUP timelineGroup) {
		Assert.isNotNull(timelineGroup);
		this.timelineGroup = timelineGroup;
		this.timelineGroup.addTimelineListener(this.timelineListener);
		Runnable addDisposeListener = new Runnable() {
			@Override
			public void run() {
				TimelineGroupViewer.this.timelineGroup
						.addDisposeListener(new DisposeListener() {
							@Override
							public void widgetDisposed(DisposeEvent e) {
								if (TimelineGroupViewer.this.timelineGroup != null
										&& !TimelineGroupViewer.this.timelineGroup
												.isDisposed()) {
									TimelineGroupViewer.this.timelineGroup
											.dispose();
								}
							}
						});
			}
		};
		if (Display.getCurrent() == Display.getDefault())
			addDisposeListener.run();
		else
			Display.getDefault().syncExec(addDisposeListener);
	}

	@Override
	public Control getControl() {
		return (Control) this.timelineGroup;
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		this.selection = selection;
		fireSelectionChanged(new SelectionChangedEvent(this, selection));
	}

	public abstract void refresh(IProgressMonitor monitor);

	@Override
	public void refresh() {
		this.refresh(null);
	}

}
