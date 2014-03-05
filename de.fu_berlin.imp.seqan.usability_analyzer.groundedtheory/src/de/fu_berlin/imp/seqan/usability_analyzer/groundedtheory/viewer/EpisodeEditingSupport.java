package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.DateTimeCellEditor;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIEditingSupport;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class EpisodeEditingSupport extends URIEditingSupport {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeEditingSupport.class);

	public static enum Field {
		NAME, STARTDATE, ENDDATE
	}

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final Field field;

	public EpisodeEditingSupport(ColumnViewer viewer, Field field) {
		super(viewer);
		this.field = field;
	}

	@Override
	protected CellEditor getCellEditor(URI element, Composite composite)
			throws Exception {
		IEpisode episode = this.locatorService.resolve(element, IEpisode.class,
				null).get();
		if (episode != null) {
			if (this.field == Field.NAME) {
				return new TextCellEditor(composite);
			} else {
				return new DateTimeCellEditor(composite);
			}
		}
		return null;
	}

	@Override
	protected Object getInitValue(URI element) throws Exception {
		IEpisode episode = this.locatorService.resolve(element, IEpisode.class,
				null).get();
		if (episode != null) {
			if (this.field == Field.NAME) {
				return episode.getCaption();
			} else if (this.field == Field.STARTDATE) {
				return episode.getDateRange().getStartDate();
			} else if (this.field == Field.ENDDATE) {
				return episode.getDateRange().getEndDate();
			}

		}
		return null;
	}

	@Override
	protected void setEditedValue(URI element, Object value) throws Exception {
		IEpisode oldEpisode = this.locatorService.resolve(element,
				IEpisode.class, null).get();
		if (oldEpisode != null) {
			try {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				IEpisode newEpisode = null;
				if (this.field == Field.NAME) {
					String newCaption = (String) value;
					newEpisode = oldEpisode.changeCaption(newCaption);
				} else if (this.field == Field.STARTDATE) {
					TimeZoneDate newStartDate = (TimeZoneDate) value;
					TimeZoneDateRange range = new TimeZoneDateRange(
							newStartDate, oldEpisode.getDateRange()
									.getEndDate());
					newEpisode = oldEpisode.changeRange(range);
				} else if (this.field == Field.ENDDATE) {
					TimeZoneDate newEndDate = (TimeZoneDate) value;
					TimeZoneDateRange range = new TimeZoneDateRange(oldEpisode
							.getDateRange().getStartDate(), newEndDate);
					newEpisode = oldEpisode.changeRange(range);
				}
				if (newEpisode == null) {
					throw new NullPointerException("Invalid field");
				}
				codeService.replaceEpisodeAndSave(oldEpisode, newEpisode);
				ViewerUtils.refresh(this.getViewer(), true);
			} catch (Exception e) {
				LOGGER.error(
						"Could not save changed "
								+ IEpisode.class.getSimpleName() + " " + value,
						e);
			}
		}
	}

}
