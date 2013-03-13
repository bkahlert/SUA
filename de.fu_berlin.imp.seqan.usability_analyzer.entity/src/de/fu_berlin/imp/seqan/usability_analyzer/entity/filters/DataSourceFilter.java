package de.fu_berlin.imp.seqan.usability_analyzer.entity.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class DataSourceFilter extends ViewerFilter {
	private DataSource dataSource;
	private DoclogDataContainer doclogDataContainer;

	public DataSourceFilter(DataSource dataSource) {
		this.dataSource = dataSource;
		this.doclogDataContainer = Activator.getDefault().getDoclogContainer();
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Entity entity = (Entity) element;
		switch (this.dataSource) {
		case DIFFS:
			return entity.getId() != null; // TODO: diffFiles prüfen
		case DOCLOG:
			ID id = entity.getId();
			if (doclogDataContainer.getFile(id) != null)
				return true;
			for (Fingerprint fingerprint : entity.getFingerprints()) {
				if (doclogDataContainer.getFile(fingerprint) != null)
					return true;
			}
			return false;
		case SURVEYRECORD:
			return entity.getSurveyRecord() != null;
		}
		return true;
	}
}
