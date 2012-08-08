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

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

public class EpisodeEditingSupport extends EditingSupport {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeEditingSupport.class);

	public EpisodeEditingSupport(ColumnViewer viewer) {
		super(viewer);
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

		if (element instanceof IEpisode)
			return new TextCellEditor(composite);

		return null;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof IEpisode) {
			IEpisode episode = (IEpisode) element;
			return episode.getCaption();
		}
		return "ERROR";
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof IEpisode && value instanceof String) {
			IEpisode oldEpisode = (IEpisode) element;
			String newCaption = (String) value;
			try {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				IEpisode newEpisode = oldEpisode.changeCaption(newCaption);
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
