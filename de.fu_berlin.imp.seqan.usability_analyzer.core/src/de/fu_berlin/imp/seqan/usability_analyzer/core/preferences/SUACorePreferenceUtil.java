package de.fu_berlin.imp.seqan.usability_analyzer.core.preferences;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;

public class SUACorePreferenceUtil {

	private IPreferenceStore preferenceStore;

	public SUACorePreferenceUtil() {
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

	public File getLogfilePath() {
		String logfilePath = preferenceStore
				.getString(SUACorePreferenceConstants.LOGFILE_PATH);
		return (logfilePath != null && !logfilePath.isEmpty()) ? new File(
				logfilePath) : null;
	}

	public boolean logfilePathChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.LOGFILE_PATH);
	}

	public File getSurveyRecordPath() {
		String surveyRecordPath = preferenceStore
				.getString(SUACorePreferenceConstants.SURVEYFILE_PATH);
		return (surveyRecordPath != null && !surveyRecordPath.isEmpty()) ? new File(
				surveyRecordPath) : null;
	}

	public boolean surveyRecordPathChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.SURVEYFILE_PATH);
	}

	public Date getDateRangeStart() {
		long rangeStart = preferenceStore
				.getLong(SUACorePreferenceConstants.DATE_RANGE_START);
		return new Date(rangeStart);
	}

	public void setDateRangeStart(Date rangeStart) {
		preferenceStore.setValue(SUACorePreferenceConstants.DATE_RANGE_START,
				rangeStart.getTime());
	}

	public boolean dateRangeStartChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_START);
	}

	public Date getDateRangeEnd() {
		long rangeEnd = preferenceStore
				.getLong(SUACorePreferenceConstants.DATE_RANGE_END);
		return new Date(rangeEnd);
	}

	public void setDateRangeEnd(Date rangeEnd) {
		preferenceStore.setValue(SUACorePreferenceConstants.DATE_RANGE_END,
				rangeEnd.getTime());
	}

	public boolean dateRangeEndChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_END);
	}

	public DateRange getDateRange() {
		return new DateRange(this.getDateRangeStart(), this.getDateRangeEnd());
	}

	public DateFormat getDateFormat() {
		return new SimpleDateFormat(
				preferenceStore
						.getString(SUACorePreferenceConstants.DATEFORMAT));
	}

	public String getDateFormatString() {
		return preferenceStore.getString(SUACorePreferenceConstants.DATEFORMAT);
	}

	public String getTimeDifferenceFormat() {
		return preferenceStore
				.getString(SUACorePreferenceConstants.TIMEDIFFERENCEFORMAT);
	}

	public RGB getColorOk() {
		return PreferenceConverter.getColor(preferenceStore,
				SUACorePreferenceConstants.COLOR_OK);
	}

	public boolean colorOkChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(SUACorePreferenceConstants.COLOR_OK);
	}

	public RGB getColorDirty() {
		return PreferenceConverter.getColor(preferenceStore,
				SUACorePreferenceConstants.COLOR_DIRTY);
	}

	public boolean colorDirtyChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_DIRTY);
	}

	public RGB getColorError() {
		return PreferenceConverter.getColor(preferenceStore,
				SUACorePreferenceConstants.COLOR_ERROR);
	}

	public boolean colorErrorChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_ERROR);
	}

	public RGB getColorMissing() {
		return PreferenceConverter.getColor(preferenceStore,
				SUACorePreferenceConstants.COLOR_MISSING);
	}

	public boolean colorMissingChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_MISSING);
	}
}
