package de.fu_berlin.imp.seqan.usability_analyzer.core.preferences;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.PreferenceUtil;

public class SUACorePreferenceUtil extends PreferenceUtil {

	public SUACorePreferenceUtil() {
		super(Activator.getDefault());
	}

	public File getLogfilePath() {
		String logfilePath = getPreferenceStore().getString(
				SUACorePreferenceConstants.LOGFILE_PATH);
		return (logfilePath != null && !logfilePath.isEmpty()) ? new File(
				logfilePath) : null;
	}

	public boolean logfilePathChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.LOGFILE_PATH);
	}

	public File getSurveyRecordPath() {
		String surveyRecordPath = getPreferenceStore().getString(
				SUACorePreferenceConstants.SURVEYFILE_PATH);
		return (surveyRecordPath != null && !surveyRecordPath.isEmpty()) ? new File(
				surveyRecordPath) : null;
	}

	public boolean surveyRecordPathChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.SURVEYFILE_PATH);
	}

	public TimeZone getDefaultTimeZone() {
		return TimeZone.getTimeZone(getPreferenceStore().getString(
				SUACorePreferenceConstants.DEFAULT_TIME_ZONE));
	}

	public void setDefaultTimeZone(TimeZone timeZone) {
		getPreferenceStore().setValue(
				SUACorePreferenceConstants.DEFAULT_TIME_ZONE, timeZone.getID());
	}

	public boolean defaultTimeZoneChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DEFAULT_TIME_ZONE);
	}

	public TimeZoneDate getDateRangeStart() {
		String rangeStart = getPreferenceStore().getString(
				SUACorePreferenceConstants.DATE_RANGE_START);
		return new TimeZoneDate(rangeStart);
	}

	public void setDateRangeStart(TimeZoneDate rangeStart) {
		getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATE_RANGE_START,
				rangeStart.toISO8601());
	}

	public boolean dateRangeStartChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_START);
	}

	public TimeZoneDate getDateRangeEnd() {
		String rangeEnd = getPreferenceStore().getString(
				SUACorePreferenceConstants.DATE_RANGE_END);
		return new TimeZoneDate(rangeEnd);
	}

	public void setDateRangeEnd(TimeZoneDate rangeEnd) {
		getPreferenceStore()
				.setValue(SUACorePreferenceConstants.DATE_RANGE_END,
						rangeEnd.toISO8601());
	}

	public boolean dateRangeEndChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_END);
	}

	public boolean getDateRangeStartEnabled() {
		return getPreferenceStore().getBoolean(
				SUACorePreferenceConstants.DATE_RANGE_START_ENABLED);
	}

	public void setDateRangeStartEnabled(boolean rangeStartEnabled) {
		getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATE_RANGE_START_ENABLED,
				rangeStartEnabled);
	}

	public boolean dateRangeStartEnabledChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_START_ENABLED);
	}

	public boolean getDateRangeEndEnabled() {
		return getPreferenceStore().getBoolean(
				SUACorePreferenceConstants.DATE_RANGE_END_ENABLED);
	}

	public void setDateRangeEndEnabled(boolean rangeEndEnabled) {
		getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATE_RANGE_END_ENABLED,
				rangeEndEnabled);
	}

	public boolean dateRangeEndEnabledChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_END_ENABLED);
	}

	public TimeZoneDateRange getDateRange() {
		return new TimeZoneDateRange(
				this.getDateRangeStartEnabled() ? getDateRangeStart() : null,
				this.getDateRangeEndEnabled() ? this.getDateRangeEnd() : null);
	}

	public DateFormat getDateFormat() {
		return new SimpleDateFormat(getPreferenceStore().getString(
				SUACorePreferenceConstants.DATEFORMAT));
	}

	public String getDateFormatString() {
		return getPreferenceStore().getString(
				SUACorePreferenceConstants.DATEFORMAT);
	}

	public String getTimeDifferenceFormat() {
		return getPreferenceStore().getString(
				SUACorePreferenceConstants.TIMEDIFFERENCEFORMAT);
	}

	public RGB getColorOk() {
		return PreferenceConverter.getColor(getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_OK);
	}

	public boolean colorOkChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(SUACorePreferenceConstants.COLOR_OK);
	}

	public RGB getColorDirty() {
		return PreferenceConverter.getColor(getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_DIRTY);
	}

	public boolean colorDirtyChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_DIRTY);
	}

	public RGB getColorError() {
		return PreferenceConverter.getColor(getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_ERROR);
	}

	public boolean colorErrorChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_ERROR);
	}

	public RGB getColorMissing() {
		return PreferenceConverter.getColor(getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_MISSING);
	}

	public boolean colorMissingChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_MISSING);
	}
}
