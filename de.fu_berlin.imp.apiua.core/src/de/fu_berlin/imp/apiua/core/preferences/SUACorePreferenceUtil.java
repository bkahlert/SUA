package de.fu_berlin.imp.apiua.core.preferences;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.EclipsePreferenceUtil;
import com.bkahlert.nebula.utils.SerializationUtils;

import de.fu_berlin.imp.apiua.core.Activator;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IWorkSession;
import de.fu_berlin.imp.apiua.core.services.IWorkSessionEntity;
import de.fu_berlin.imp.apiua.core.services.impl.WorkSession;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;

public class SUACorePreferenceUtil extends EclipsePreferenceUtil {

	private static final Logger LOGGER = Logger
			.getLogger(SUACorePreferenceUtil.class);

	public SUACorePreferenceUtil() {
		super(Activator.getDefault());
	}

	public String getDataDirectory() {
		String dataDirectory = this.getPreferenceStore().getString(
				SUACorePreferenceConstants.DATA_DIRECTORY);
		return (dataDirectory != null && !dataDirectory.isEmpty()) ? Normalizer
				.normalize(dataDirectory, Form.NFC) : null;
	}

	public void setDataDirectory(String dataDirectory) {
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATA_DIRECTORY, dataDirectory);
	}

	public boolean dataDirectoryChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATA_DIRECTORY);
	}

	public List<String> getDataDirectories() {
		String[] dataDirectories = StringUtils.split(this.getPreferenceStore()
				.getString(SUACorePreferenceConstants.DATA_DIRECTORIES), ";");
		if (dataDirectories == null) {
			return new ArrayList<String>();
		}
		return new ArrayList<String>(Arrays.asList(dataDirectories));
	}

	public void setDataDirectories(List<String> dataDirectories) {
		Assert.isNotNull(dataDirectories);
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATA_DIRECTORIES,
				StringUtils.join(dataDirectories, ";"));
	}

	public boolean dataDirectoriesChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATA_DIRECTORIES);
	}

	public TimeZone getDefaultTimeZone() {
		return TimeZone.getTimeZone(this.getPreferenceStore().getString(
				SUACorePreferenceConstants.DEFAULT_TIME_ZONE));
	}

	public void setDefaultTimeZone(TimeZone timeZone) {
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.DEFAULT_TIME_ZONE, timeZone.getID());
	}

	public boolean defaultTimeZoneChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DEFAULT_TIME_ZONE);
	}

	public TimeZoneDate getDateRangeStart() {
		String rangeStart = this.getPreferenceStore().getString(
				SUACorePreferenceConstants.DATE_RANGE_START);
		return (rangeStart.isEmpty()) ? null : new TimeZoneDate(rangeStart);
	}

	public void setDateRangeStart(TimeZoneDate rangeStart) {
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATE_RANGE_START,
				rangeStart.toISO8601());
	}

	public boolean dateRangeStartChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_START);
	}

	public TimeZoneDate getDateRangeEnd() {
		String rangeEnd = this.getPreferenceStore().getString(
				SUACorePreferenceConstants.DATE_RANGE_END);
		return (rangeEnd.isEmpty()) ? null : new TimeZoneDate(rangeEnd);
	}

	public void setDateRangeEnd(TimeZoneDate rangeEnd) {
		this.getPreferenceStore()
				.setValue(SUACorePreferenceConstants.DATE_RANGE_END,
						rangeEnd.toISO8601());
	}

	public boolean dateRangeEndChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_END);
	}

	public boolean getDateRangeStartEnabled() {
		return this.getPreferenceStore().getBoolean(
				SUACorePreferenceConstants.DATE_RANGE_START_ENABLED);
	}

	public void setDateRangeStartEnabled(boolean rangeStartEnabled) {
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATE_RANGE_START_ENABLED,
				rangeStartEnabled);
	}

	public boolean dateRangeStartEnabledChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_START_ENABLED);
	}

	public boolean getDateRangeEndEnabled() {
		return this.getPreferenceStore().getBoolean(
				SUACorePreferenceConstants.DATE_RANGE_END_ENABLED);
	}

	public void setDateRangeEndEnabled(boolean rangeEndEnabled) {
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.DATE_RANGE_END_ENABLED,
				rangeEndEnabled);
	}

	public boolean dateRangeEndEnabledChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.DATE_RANGE_END_ENABLED);
	}

	public TimeZoneDateRange getDateRange() {
		return new TimeZoneDateRange(
				this.getDateRangeStartEnabled() ? this.getDateRangeStart()
						: null,
				this.getDateRangeEndEnabled() ? this.getDateRangeEnd() : null);
	}

	public DateFormat getDateFormat() {
		return new SimpleDateFormat(this.getPreferenceStore().getString(
				SUACorePreferenceConstants.DATEFORMAT));
	}

	public String getDateFormatString() {
		return this.getPreferenceStore().getString(
				SUACorePreferenceConstants.DATEFORMAT);
	}

	public String getTimeDifferenceFormat() {
		return this.getPreferenceStore() != null ? this.getPreferenceStore()
				.getString(SUACorePreferenceConstants.TIMEDIFFERENCEFORMAT)
				: SUACorePreferenceInitializer.DEFAULT_TIMEDIFFERENCEFORMAT;
	}

	public RGB getColorOk() {
		return PreferenceConverter.getColor(this.getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_OK);
	}

	public boolean colorOkChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(SUACorePreferenceConstants.COLOR_OK);
	}

	public RGB getColorDirty() {
		return PreferenceConverter.getColor(this.getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_DIRTY);
	}

	public boolean colorDirtyChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_DIRTY);
	}

	public RGB getColorError() {
		return PreferenceConverter.getColor(this.getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_ERROR);
	}

	public boolean colorErrorChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_ERROR);
	}

	public RGB getColorMissing() {
		return PreferenceConverter.getColor(this.getPreferenceStore(),
				SUACorePreferenceConstants.COLOR_MISSING);
	}

	public boolean colorMissingChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUACorePreferenceConstants.COLOR_MISSING);
	}

	public void setLastWorkSession(IWorkSession workSession) {
		List<URI> uris = new ArrayList<URI>();
		if (workSession != null) {
			for (IWorkSessionEntity entity : workSession.getEntities()) {
				uris.add(entity.getUri());
			}
		}

		String config = StringUtils.join(uris, "%%");
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.LAST_WORKSESSION, config);
	}

	public IWorkSession getLastWorkSession() {
		ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);
		String[] uriStrings = this.getPreferenceStore()
				.getString(SUACorePreferenceConstants.LAST_WORKSESSION)
				.split("%%");
		List<IWorkSessionEntity> entities = new ArrayList<IWorkSessionEntity>();
		for (String uriString : uriStrings) {
			if (uriString == null || uriString.isEmpty()) {
				continue;
			}
			try {
				final URI uri = new URI(uriString);
				ILocatable locatable = locatorService.resolve(uri, null).get();
				if (locatable instanceof IWorkSessionEntity) {
					entities.add((IWorkSessionEntity) locatable);
				}
			} catch (Exception e) {
				LOGGER.error("Could not resolve the corresponding object of "
						+ URI.class.getSimpleName() + " " + uriStrings, e);
			}
		}
		return new WorkSession(entities.toArray(new IWorkSessionEntity[0]));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Point> loadLastScrollPositions() {
		Map<String, Point> scrollPositions = new HashMap<String, Point>();

		String ser = this.getPreferenceStore().getString(
				SUACorePreferenceConstants.LAST_SCROLL_POSITION);
		if (ser == null || ser.isEmpty()) {
			return scrollPositions;
		}

		try {
			Map<String, String> map = new HashMap<String, String>();
			map = (Map<String, String>) SerializationUtils.deserialize(ser,
					Serializable.class);
			for (Entry<String, String> entry : map.entrySet()) {
				if (entry.getValue().contains(",")) {
					String[] split = entry.getValue().split(",");
					try {
						scrollPositions.put(
								entry.getKey(),
								new Point(Integer.parseInt(split[0]), Integer
										.parseInt(split[1])));
					} catch (NumberFormatException e) {
					}
					;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error loading last scroll positions", e);
		}
		return scrollPositions;
	}

	private void saveLastScrollPositions(Map<String, Point> scrollPositions) {
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, Point> entry : scrollPositions.entrySet()) {
			if (entry.getValue() != null) {
				map.put(entry.getKey(),
						entry.getValue().x + "," + entry.getValue().y);
			}
		}
		try {
			String ser = SerializationUtils.serialize((Serializable) map);
			this.getPreferenceStore().setValue(
					SUACorePreferenceConstants.LAST_SCROLL_POSITION, ser);
		} catch (Exception e) {
			LOGGER.error("Error saving last scroll positions", e);
		}
	}

	public Point getLastScrollPosition(String owner) {
		Assert.isNotNull(owner);
		Map<String, Point> scrollPositions = this.loadLastScrollPositions();
		if (scrollPositions.containsKey(owner)) {
			return scrollPositions.get(owner);
		} else {
			return new Point(0, 0);
		}
	}

	public Point getLastScrollPosition(Class<?> clazz) {
		Assert.isNotNull(clazz);
		return this.getLastScrollPosition(clazz.getName());
	}

	public void setLastScrollPosition(String owner, Point scrollPosition) {
		Assert.isNotNull(owner);
		Map<String, Point> scrollPositions = this.loadLastScrollPositions();
		scrollPositions.put(owner, scrollPosition);
		this.saveLastScrollPositions(scrollPositions);
	}

	public void setLastScrollPosition(Class<?> clazz, Point scrollPosition) {
		Assert.isNotNull(clazz);
		this.setLastScrollPosition(clazz.getName(), scrollPosition);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map getCurrentState() {
		Map currentState = super.getCurrentState();
		currentState.put(SUACorePreferenceConstants.FOCUSED_ELEMENTS,
				this.getFocusedElements());
		return currentState;
	}

	public void setFocusedElements(List<URI> elements) {
		String pref = de.fu_berlin.imp.apiua.core.util.SerializationUtils
				.serialize(elements);
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.FOCUSED_ELEMENTS, pref);
		this.fireSourceChanged(ISources.WORKBENCH,
				SUACorePreferenceConstants.FOCUSED_ELEMENTS, elements);
	}

	public List<URI> getFocusedElements() {
		String pref = this.getPreferenceStore().getString(
				SUACorePreferenceConstants.FOCUSED_ELEMENTS);
		if (pref != null && !pref.isEmpty()) {
			try {
				return new ArrayList<URI>(
						de.fu_berlin.imp.apiua.core.util.SerializationUtils
								.deserialize(pref));
			} catch (Exception e) {
				// LOGGER.error("Could not load last focused elements", e);
			}
		}
		return new LinkedList<URI>();
	}

	public void setSelectionHistory(List<URI> elements) {
		String pref = de.fu_berlin.imp.apiua.core.util.SerializationUtils
				.serialize(elements);
		this.getPreferenceStore().setValue(
				SUACorePreferenceConstants.SELECTION_HISTORY, pref);
		this.fireSourceChanged(ISources.WORKBENCH,
				SUACorePreferenceConstants.SELECTION_HISTORY, elements);
	}

	public List<URI> getSelectionHistory() {
		String pref = this.getPreferenceStore().getString(
				SUACorePreferenceConstants.SELECTION_HISTORY);
		if (pref != null && !pref.isEmpty()) {
			try {
				return new ArrayList<URI>(
						de.fu_berlin.imp.apiua.core.util.SerializationUtils
								.deserialize(pref));
			} catch (Exception e) {
				// LOGGER.error("Could not load last focused elements", e);
			}
		}
		return new LinkedList<URI>();
	}
}
