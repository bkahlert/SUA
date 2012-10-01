package de.fu_berlin.imp.seqan.usability_analyzer.core.preferences;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
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

	public String getDataDirectory() {
		String dataDirectory = getPreferenceStore().getString(
				SUACorePreferenceConstants.DATA_DIRECTORY);
		return (dataDirectory != null && !dataDirectory.isEmpty()) ? Normalizer
				.normalize(dataDirectory, Form.NFC) : null;
	}

	public void setDataDirectory(String dataDirectory) {
		getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATA_DIRECTORY, dataDirectory);
	}

	public boolean dataDirectoryChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATA_DIRECTORY);
	}

	public List<String> getDataDirectories() {
		String[] dataDirectories = StringUtils.split(getPreferenceStore()
				.getString(SUACorePreferenceConstants.DATA_DIRECTORIES), ";");
		if (dataDirectories == null)
			return new ArrayList<String>();
		return new ArrayList<String>(Arrays.asList(dataDirectories));
	}

	public void setDataDirectories(List<String> dataDirectories) {
		Assert.isNotNull(dataDirectories);
		getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATA_DIRECTORIES,
				StringUtils.join(dataDirectories, ";"));
	}

	public boolean dataDirectoriesChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATA_DIRECTORIES);
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
		return (rangeStart.isEmpty()) ? null : new TimeZoneDate(rangeStart);
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
		return (rangeEnd.isEmpty()) ? null : new TimeZoneDate(rangeEnd);
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
