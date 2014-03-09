package de.fu_berlin.imp.seqan.usability_analyzer.entity.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.PropertyChangeEvent;

import com.bkahlert.nebula.utils.EclipsePreferenceUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;

public class SUAEntityPreferenceUtil extends EclipsePreferenceUtil {

	public SUAEntityPreferenceUtil() {
		super(Activator.getDefault());
	}

	public boolean getFilterDiffs() {
		return getPreferenceStore().getBoolean(
				SUAEntityPreferenceConstants.FILTER_DIFFS);
	}

	public boolean filterDiffsChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAEntityPreferenceConstants.FILTER_DIFFS);
	}

	public boolean getFilterDoclogs() {
		return getPreferenceStore().getBoolean(
				SUAEntityPreferenceConstants.FILTER_DOCLOGS);
	}

	public boolean filterDoclogsChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAEntityPreferenceConstants.FILTER_DOCLOGS);
	}

	public boolean getFilterSurveys() {
		return getPreferenceStore().getBoolean(
				SUAEntityPreferenceConstants.FILTER_SURVEYS);
	}

	public boolean filterSurveysChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAEntityPreferenceConstants.FILTER_SURVEYS);
	}

	public List<DataSource> getFilterdDataSources() {
		List<DataSource> filteredDataSources = new ArrayList<DataSource>();
		if (this.getFilterDiffs())
			filteredDataSources.add(DataSource.DIFFS);
		if (this.getFilterDoclogs())
			filteredDataSources.add(DataSource.DOCLOG);
		if (this.getFilterSurveys())
			filteredDataSources.add(DataSource.SURVEYRECORD);
		return filteredDataSources;
	}
}
