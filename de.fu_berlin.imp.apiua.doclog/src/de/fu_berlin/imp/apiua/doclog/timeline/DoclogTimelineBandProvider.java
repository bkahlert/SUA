package de.fu_berlin.imp.apiua.doclog.timeline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.widgets.timeline.IBaseTimeline;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.doclog.Activator;
import de.fu_berlin.imp.apiua.doclog.model.Doclog;
import de.fu_berlin.imp.apiua.doclog.model.DoclogAction;
import de.fu_berlin.imp.apiua.doclog.model.DoclogRecord;
import de.fu_berlin.imp.apiua.doclog.model.DoclogRecordList;
import de.fu_berlin.imp.apiua.doclog.ui.DoclogLabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.timeline.extensionProviders.ITimelineBandProvider;

public class DoclogTimelineBandProvider implements
		ITimelineBandProvider<IIdentifier> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(DoclogTimelineBandProvider.class);

	private enum BANDS {
		DOCLOGRECORD_BAND
	}

	@Override
	public ITimelineContentProvider<IIdentifier> getContentProvider() {
		return new ITimelineContentProvider<IIdentifier>() {

			// private DoclogContentProvider doclogContentProvider = new
			// DoclogContentProvider();
			private IIdentifier input = null;

			@Override
			public <TIMELINE extends IBaseTimeline> void inputChanged(
					ITimelineGroupViewer<TIMELINE, IIdentifier> timelineGroupViewer,
					IIdentifier oldInput, IIdentifier newInput) {
				this.input = newInput;
				// this.doclogContentProvider.inputChanged(viewer, oldInput,
				// newInput);
			}

			@Override
			public boolean isValid(IIdentifier key) {
				return ArrayUtils.contains(Activator.getDefault()
						.getDoclogContainer().getIdentifiers(), key);
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

				final Doclog doclog = (this.input instanceof IIdentifier && Activator
						.getDefault().getDoclogContainer() != null) ? Activator
						.getDefault().getDoclogContainer()
						.getDoclogFile(this.input, subMonitor.newChild(1))
						: null;

				if (doclog == null) {
					return new Object[0];
				}

				switch ((BANDS) band) {
				case DOCLOGRECORD_BAND:
					DoclogRecordList filteredDoclogRecords = new DoclogRecordList();
					for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
						if (doclogRecord.getAction() == DoclogAction.UNLOAD) {
							continue;
						}
						if (doclogRecord.getUrl().contains(
								"dddoc/html_devel/INDEX_")) {
							continue;
						}
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

			private final ICodeService codeService = (ICodeService) PlatformUI
					.getWorkbench().getService(ICodeService.class);
			private final DoclogLabelProvider doclogLabelProvider = new DoclogLabelProvider();

			@Override
			public String getTitle(Object event) {
				if (event instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) event;

					StringBuffer title = new StringBuffer();
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
					default:
					}

					return title.toString();
				}
				return "";
			}

			@Override
			public String getTooltip(Object event) {
				return null;
			}

			@Override
			public java.net.URI getIcon(Object event) {
				Image image = this.doclogLabelProvider.getImage(event);
				if (image != null) {
					return ImageUtils.createUriFromImage(image);
				}
				return null;
			}

			@Override
			public java.net.URI getImage(Object event) {
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
			public RGB[] getColors(Object event) {
				List<RGB> colors = new ArrayList<RGB>();
				if (event instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) event;
					for (ICode code : this.codeService.getAllCodes(doclogRecord
							.getUri())) {
						colors.add(code.getColor());
					}
				}
				return colors.toArray(new RGB[0]);
			}

			@Override
			public boolean isResizable(Object event) {
				return false;
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
