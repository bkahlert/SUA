package de.fu_berlin.imp.seqan.usability_analyzer.doclog.timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEventImageBased;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecordList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.DoclogLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

public class DoclogTimelineBandProvider implements ITimelineBandProvider {

	@Override
	public boolean isValid(Object key) {
		return Activator.getDefault().getDoclogContainer().getKeys()
				.contains(key);
	}

	@Override
	public List<ITimelineBand> getTimelineBands(Object key,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);

		final Doclog doclog = (key instanceof ID) ? Activator.getDefault()
				.getDoclogContainer()
				.getDoclogFile(key, subMonitor.newChild(1)) : null;

		if (doclog == null)
			return null;

		DoclogRecordList filteredDoclogRecords = new DoclogRecordList();
		for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
			if (doclogRecord.getAction() == DoclogAction.UNLOAD)
				continue;
			if (doclogRecord.getUrl().contains("dddoc/html_devel/INDEX_"))
				continue;
			filteredDoclogRecords.add(doclogRecord);
		}

		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();

		ITimelineBand doclogRecordBand = createDoclogRecordTimelineBand(filteredDoclogRecords);
		timelineBands.add(doclogRecordBand);
		monitor.worked(1);

		return timelineBands;
	}

	private ITimelineBand createDoclogRecordTimelineBand(
			final DoclogRecordList doclogRecords) {
		IOptions doclogRecordBandOptions = new Options();
		doclogRecordBandOptions.setTitle("Documentation");
		doclogRecordBandOptions.setShowInOverviewBands(true);
		doclogRecordBandOptions.setRatio(0.35f);
		List<ITimelineEvent> doclogRecordEvents = new ArrayList<ITimelineEvent>();
		for (DoclogRecord doclogRecord : doclogRecords) {
			ITimelineEvent doclogRecordEvent = createDoclogRecordEvent(doclogRecord);
			doclogRecordEvents.add(doclogRecordEvent);
		}
		ITimelineBand doclogRecordBand = new TimelineBand(
				doclogRecordBandOptions, doclogRecordEvents);
		return doclogRecordBand;
	}

	private ITimelineEvent createDoclogRecordEvent(DoclogRecord doclogRecord) {
		DoclogLabelProvider doclogLabelProvider = new DoclogLabelProvider();

		StringBuffer title = new StringBuffer();
		title.append(doclogRecord.getAction().toString() + " - ");
		title.append(doclogRecord.getShortUrl());
		switch (doclogRecord.getAction()) {
		case LINK:
			title.append("<br/>→ " + doclogRecord.getActionParameter());
			break;
		case TYPING:
			title.append("<br/>typing &quot;"
					+ doclogRecord.getActionParameter() + "&quot;");
			break;
		}

		/*
		 * IMPORTANT: Because Mac OS returns filenames in a decomposed form
		 * (http://loopkid.net/articles/2011/03/19/groking-hfs-character
		 * -encoding) we need to convert them to composed form (http://download
		 * .oracle.com/javase/6/docs/api/java/text/Normalizer.html). This is the
		 * only form where we can be sure it is compatible with the outer world
		 * (e.g. a browser).
		 */
		/*
		 * I don't know why but the NFC thing doesn't seem to be necessary any
		 * more... String filename = Normalizer.normalize(
		 * screenshotFile.getCanonicalPath(), Form.NFC);
		 */
		// File screenshotFile = doclogRecord.getScreenshot().getFile();
		// if (screenshotFile != null) {
		// String filename = screenshotFile.getCanonicalPath();
		// icon = "file://" + filename.replace("%", "%25");
		// }

		TimeZoneDateRange dateRange = doclogRecord.getDateRange();
		Calendar startDate = dateRange.getStartDate() != null ? dateRange
				.getStartDate().getCalendar() : null;
		Calendar endDate = dateRange.getEndDate() != null ? dateRange
				.getEndDate().getCalendar() : null;

		List<String> classNames = Arrays.asList(DoclogRecord.class
				.getSimpleName(), doclogRecord.getAction().toString());

		return new TimelineEventImageBased(title.toString(),
				doclogLabelProvider.getImage(doclogRecord), null, startDate,
				endDate, classNames, doclogRecord);
	}

}
