package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

public class AnalysisTimelineBandProvider implements ITimelineBandProvider {

	private static final Logger LOGGER = Logger
			.getLogger(AnalysisTimelineBandProvider.class);

	public AnalysisTimelineBandProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isValid(Object key) {
		if (key instanceof ID) {
			return true;
		} else if (key instanceof Fingerprint) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<ITimelineBand> getTimelineBands(Object key,
			IProgressMonitor monitor) {
		List<ITimelineBand> bands = new ArrayList<ITimelineBand>();

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		if (codeService == null) {
			LOGGER.error("Could not get " + ICodeService.class.getSimpleName());
			return bands;
		}

		IOptions options = new Options();
		options.setTitle("Analysis");
		options.setRatio(0.20f);
		options.setShowInOverviewBands(true);

		List<ITimelineEvent> events = new ArrayList<ITimelineEvent>();
		for (ICodeInstance instance : codeService.getInstances(key)) {
			ICodeable codeable = codeService.getCodedObject(instance.getId());
			if (codeable == null) {
				LOGGER.error("Retrieved null "
						+ ICodeable.class.getSimpleName());
				continue;
			}
			ICode code = instance.getCode();
			if (code == null) {
				LOGGER.fatal("Detected that " + codeable
						+ " is attached no a null "
						+ ICode.class.getSimpleName());
				continue;
			}
			if (codeable instanceof HasDateRange) {
				TimeZoneDateRange dateRange = ((HasDateRange) codeable)
						.getDateRange();
				ITimelineEvent event = new TimelineEvent(code.getCaption(),
						null, null,
						dateRange.getStartDate() != null ? dateRange
								.getStartDate().getCalendar() : null,
						dateRange.getEndDate() != null ? dateRange.getEndDate()
								.getCalendar() : null, Arrays.asList("CODE"),
						code);
				events.add(event);
			} else {
				LOGGER.warn("Could not render "
						+ ICodeInstance.class.getSimpleName()
						+ " since the corresponding codeable " + codeable
						+ " has no start or end.");
			}
		}

		ITimelineBand band = new TimelineBand(options, events);
		bands.add(band);
		return bands;
	}
}
