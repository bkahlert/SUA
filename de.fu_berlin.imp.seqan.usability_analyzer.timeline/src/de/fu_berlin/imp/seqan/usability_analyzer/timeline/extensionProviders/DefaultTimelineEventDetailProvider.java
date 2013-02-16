package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

/**
 * This class provides a default implementation of
 * {@link ITimelineEventDetailProvider}.
 * 
 * @author bkahlert
 * 
 * @param <DataType>
 */
public abstract class DefaultTimelineEventDetailProvider<DataType> implements
		ITimelineEventDetailProvider<DataType> {

	public List<IllustratedText> getMetaInformation(DataType data) {
		return new ArrayList<IllustratedText>();
	}

	public List<Map.Entry<String, String>> getDetailInformation(DataType data) {
		return new ArrayList<Map.Entry<String, String>>();
	}

	public void fillCustomComposite(org.eclipse.swt.widgets.Composite parent,
			DataType doclogRecord, ITimeline timeline) {
	}

	public Color getBackground(DataType data, ITimeline timeline) {
		boolean isIntersected = false;

		if (data instanceof HasDateRange) {
			TimeZoneDateRange dateRange = ((HasDateRange) data).getDateRange();

			if (timeline.getDecorators() != null)
				for (IDecorator t : timeline.getDecorators()) {
					if (new TimeZoneDateRange(
							t.getStartDate() != null ? new TimeZoneDate(
									t.getStartDate()) : null,
							t.getEndDate() != null ? new TimeZoneDate(t
									.getEndDate()) : null)
							.isIntersected(dateRange)) {
						isIntersected = true;
						break;
					}
				}

			return isIntersected ? Activator.COLOR_HIGHLIGHT
					: Activator.COLOR_STANDARD;
		}

		return Activator.COLOR_STANDARD;
	}
}
