package de.fu_berlin.imp.seqan.usability_analyzer.doclog.timeline;

import java.net.URI;
import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.graphics.Image;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineHelper;

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

	private enum BANDS {
		DOCLOGRECORD_BAND
	}

	@Override
	public ITimelineContentProvider getContentProvider() {
		return new ITimelineContentProvider() {

			private ITimelineViewer timelineViewer = null;
			private Object input = null;

			@Override
			public void inputChanged(ITimelineViewer timelineViewer,
					Object oldInput, Object newInput) {
				this.timelineViewer = timelineViewer;
				this.input = newInput;
			}

			@Override
			public boolean isValid(Object key) {
				return Activator.getDefault().getDoclogContainer().getKeys()
						.contains(key);
			}

			@Override
			public Object[] getBands(IProgressMonitor monitor) {
				return new Object[] { BANDS.DOCLOGRECORD_BAND };
			}

			@Override
			public Object[] getEvents(Object band, IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
				if (!(band instanceof BANDS)) {
					subMonitor.done();
					return new Object[0];
				}

				final Doclog doclog = (this.input instanceof ID) ? Activator
						.getDefault().getDoclogContainer()
						.getDoclogFile(this.input, subMonitor.newChild(1))
						: null;

				if (doclog == null)
					return null;

				switch ((BANDS) band) {
				case DOCLOGRECORD_BAND:
					DoclogRecordList filteredDoclogRecords = new DoclogRecordList();
					for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
						if (doclogRecord.getAction() == DoclogAction.UNLOAD)
							continue;
						if (doclogRecord.getUrl().contains(
								"dddoc/html_devel/INDEX_"))
							continue;
						filteredDoclogRecords.add(doclogRecord);
					}
					monitor.worked(1);
					return filteredDoclogRecords.toArray();
				}

				return new Object[0];
			}
		};
	}

	@Override
	public ITimelineBandLabelProvider getBandLabelProvider() {
		return new ITimelineBandLabelProvider() {

			@Override
			public String getTitle(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case DOCLOGRECORD_BAND:
						return "Documentation";
					}
				}
				return "";
			}

			@Override
			public Boolean isShowInOverviewBands(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case DOCLOGRECORD_BAND:
						return true;
					}
				}
				return null;
			}

			@Override
			public Float getRatio(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case DOCLOGRECORD_BAND:
						return 0.35f;
					}
				}
				return null;
			}
		};
	}

	@Override
	public ITimelineEventLabelProvider getEventLabelProvider() {
		return new ITimelineEventLabelProvider() {

			private DoclogLabelProvider doclogLabelProvider = new DoclogLabelProvider();

			@Override
			public String getTitle(Object event) {
				if (event instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) event;

					StringBuffer title = new StringBuffer();
					title.append(doclogRecord.getAction().toString() + " - ");
					title.append(doclogRecord.getShortUrl());
					switch (doclogRecord.getAction()) {
					case LINK:
						title.append("<br/>→ "
								+ doclogRecord.getActionParameter());
						break;
					case TYPING:
						title.append("<br/>typing &quot;"
								+ doclogRecord.getActionParameter() + "&quot;");
						break;
					}

					return title.toString();
				}
				return "";
			}

			@Override
			public URI getIcon(Object event) {
				/*
				 * IMPORTANT: Because Mac OS returns filenames in a decomposed
				 * form
				 * (http://loopkid.net/articles/2011/03/19/groking-hfs-character
				 * -encoding) we need to convert them to composed form
				 * (http://download
				 * .oracle.com/javase/6/docs/api/java/text/Normalizer.html).
				 * This is the only form where we can be sure it is compatible
				 * with the outer world (e.g. a browser).
				 */
				/*
				 * I don't know why but the NFC thing doesn't seem to be
				 * necessary any more... String filename = Normalizer.normalize(
				 * screenshotFile.getCanonicalPath(), Form.NFC);
				 */
				// File screenshotFile = doclogRecord.getScreenshot().getFile();
				// if (screenshotFile != null) {
				// String filename = screenshotFile.getCanonicalPath();
				// icon = "file://" + filename.replace("%", "%25");
				// }

				Image image = doclogLabelProvider.getImage(event);
				if (image != null)
					return TimelineHelper.createUriFromImage(image);
				return null;
			}

			@Override
			public URI getImage(Object event) {
				return null;
			}

			@Override
			public Calendar getStart(Object event) {
				if (event instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) event;
					TimeZoneDateRange dateRange = doclogRecord.getDateRange();
					return dateRange.getStartDate() != null ? dateRange
							.getStartDate().getCalendar() : null;
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				if (event instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) event;
					TimeZoneDateRange dateRange = doclogRecord.getDateRange();
					return dateRange.getEndDate() != null ? dateRange
							.getEndDate().getCalendar() : null;
				}
				return null;
			}

			@Override
			public String[] getClassNames(Object event) {
				if (event instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) event;
					return new String[] { DoclogRecord.class.getSimpleName(),
							doclogRecord.getAction().toString() };
				}
				return new String[0];
			}
		};
	}
}
