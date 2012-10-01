package de.fu_berlin.imp.seqan.usability_analyzer.entity.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class DataSourceFilter extends ViewerFilter {
	private DataSource dataSource;
	private DoclogDataDirectory doclogDataDirectory;

	public DataSourceFilter(DataSource dataSource) {
		this.dataSource = dataSource;
		this.doclogDataDirectory = Activator.getDefault()
				.getDoclogDataDirectory();
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Entity entity = (Entity) element;
		switch (this.dataSource) {
		case DIFFS:
			return entity.getID() != null; // TODO: diffFiles prüfen
		case DOCLOG:
			ID id = entity.getID();
			if (doclogDataDirectory.getFile(id) != null)
				return true;
			for (Fingerprint fingerprint : entity.getFingerprints()) {
				if (doclogDataDirectory.getFile(fingerprint) != null)
					return true;
			}
			return false;
		case SURVEYRECORD:
			return entity.getSurveyRecord() != null;
		}
		return true;
	}
}
