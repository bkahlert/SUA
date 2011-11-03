package de.fu_berlin.imp.seqan.usability_analyzer.person.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.person.Activator;

public class SUAEntityPreferenceUtil {

	private IPreferenceStore preferenceStore;

	public SUAEntityPreferenceUtil() {
		super();
		preferenceStore = Activator.getDefault().getPreferenceStore();
	}

	public void addPropertyChangeListener(
			IPropertyChangeListener propertyChangeListener) {
		this.preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}

	public void removePropertyChangeListener(
			IPropertyChangeListener propertyChangeListener) {
		this.preferenceStore
				.removePropertyChangeListener(propertyChangeListener);
	}

	public boolean getFilterDiffs() {
		return preferenceStore
				.getBoolean(SUAEntityPreferenceConstants.FILTER_DIFFS);
	}

	public boolean filterDiffsChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAEntityPreferenceConstants.FILTER_DIFFS);
	}

	public boolean getFilterDoclogs() {
		return preferenceStore
				.getBoolean(SUAEntityPreferenceConstants.FILTER_DOCLOGS);
	}

	public boolean filterDoclogsChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAEntityPreferenceConstants.FILTER_DOCLOGS);
	}

	public boolean getFilterSurveys() {
		return preferenceStore
				.getBoolean(SUAEntityPreferenceConstants.FILTER_SURVEYS);
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
