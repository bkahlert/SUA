package de.fu_berlin.imp.seqan.usability_analyzer.doclog.timeline;

import org.eclipse.core.runtime.IAdapterFactory;

import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class TimelineEventAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ICodeable.class, DoclogRecord.class };
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
			if (adapterType == DoclogRecord.class) {
				if (timelineEvent.getPayload() instanceof DoclogRecord)
					return timelineEvent.getPayload();
				else
					return null;
			}
			return null;
		}
		return null;
	}

}
