package de.fu_berlin.imp.seqan.usability_analyzer.diff.timeline;

import org.eclipse.core.runtime.IAdapterFactory;

import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class TimelineEventAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ICodeable.class, Diff.class, DiffRecord.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ITimelineEvent) {
			ITimelineEvent timelineEvent = (ITimelineEvent) adaptableObject;
			if (adapterType == ICodeable.class) {
				if (timelineEvent.getPayload() instanceof ICodeable)
					return timelineEvent.getPayload();
				else
					return null;
			}
			if (adapterType == IDiff.class) {
				if (timelineEvent.getPayload() instanceof IDiff)
					return timelineEvent.getPayload();
				else
					return null;
			}
			if (adapterType == DiffRecord.class) {
				if (timelineEvent.getPayload() instanceof DiffRecord)
					return timelineEvent.getPayload();
				else
					return null;
			}
			return null;
		}
		return null;
	}

}
