package de.fu_berlin.imp.seqan.usability_analyzer.entity.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class DataSourceFilter extends ViewerFilter {
	private DataSource dataSource;
	private DoclogDirectory doclogDirectory;

	public DataSourceFilter(DataSource dataSource) {
		this.dataSource = dataSource;
		this.doclogDirectory = Activator.getDefault().getDoclogDirectory();
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Entity person = (Entity) element;
		switch (this.dataSource) {
		case DIFFS:
			return person.getID() != null; // TODO: diffFiles prüfen
		case DOCLOG:
			ID id = person.getID();
			if (doclogDirectory.getFile(id) != null)
				return true;
			for (Fingerprint fingerprint : person.getFingerprints()) {
				if (doclogDirectory.getFile(fingerprint) != null)
					return true;
			}
			return false;
		case SURVEYRECORD:
			return person.getSurveyRecord() != null;
		}
		return true;
	}
}
