package de.fu_berlin.imp.seqan.usability_analyzer.entity.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class DataSourceFilter extends ViewerFilter {
	private DataSource dataSource;

	public DataSourceFilter(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Entity person = (Entity) element;
		switch (this.dataSource) {
		case DIFFS:
			return person.getDiffFiles() != null
					&& person.getDiffFiles().size() > 0;
		case DOCLOG:
			return person.getDoclogFile() != null;

		case SURVEYRECORD:
			return person.getSurveyRecord() != null;
		}
		return true;
	}
}
