package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.DateTimeCellEditor;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

public class EpisodeEditingSupport extends EditingSupport {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeEditingSupport.class);

	public static enum Field {
		NAME, STARTDATE, ENDDATE
	}

	private Field field;

	public EpisodeEditingSupport(ColumnViewer viewer, Field field) {
		super(viewer);
		this.field = field;
	}

	@Override
	protected boolean canEdit(Object element) {
		return getCellEditor(element) != null;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		Composite composite = null;
		if (getViewer() instanceof TableViewer)
			composite = ((TableViewer) getViewer()).getTable();
		if (getViewer() instanceof TreeViewer)
			composite = ((TreeViewer) getViewer()).getTree();
		if (composite == null)
			return null;

		if (element instanceof IEpisode) {
			if (field == Field.NAME)
				return new TextCellEditor(composite);
			else
				return new DateTimeCellEditor(composite);
		}

		return null;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof IEpisode) {
			IEpisode episode = (IEpisode) element;
			if (field == Field.NAME)
				return episode.getCaption();
			else if (field == Field.STARTDATE)
				return episode.getRange().getStartDate();
			else if (field == Field.STARTDATE)
				return episode.getRange().getEndDate();

		}
		return "ERROR";
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof IEpisode) {
			try {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				IEpisode oldEpisode = (IEpisode) element;
				IEpisode newEpisode = null;
				if (field == Field.NAME) {
					String newCaption = (String) value;
					newEpisode = oldEpisode.changeCaption(newCaption);
				} else if (field == Field.STARTDATE) {
					TimeZoneDate newStartDate = (TimeZoneDate) value;
					TimeZoneDateRange range = new TimeZoneDateRange(
							newStartDate, oldEpisode.getRange().getEndDate());
					newEpisode = oldEpisode.changeRange(range);
				} else if (field == Field.ENDDATE) {
					TimeZoneDate newEndDate = (TimeZoneDate) value;
					TimeZoneDateRange range = new TimeZoneDateRange(oldEpisode
							.getRange().getStartDate(), newEndDate);
					newEpisode = oldEpisode.changeRange(range);
				}
				if (newEpisode == null)
					throw new NullPointerException("Invalid field");
				codeService.replaceEpisodeAndSave(oldEpisode, newEpisode);
				ViewerUtils.refresh(getViewer(), true);
			} catch (Exception e) {
				LOGGER.error(
						"Could not save changed "
								+ IEpisode.class.getSimpleName() + " " + value,
						e);
			}
		}
	}

}
