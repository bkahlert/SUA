package de.fu_berlin.imp.apiua.survey.model;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Comparator;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.comparators.NullComparator;

import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.util.DateUtil;

@XmlJavaTypeAdapter(DateIdAdapter.class)
public class DateId implements IIdentifier {

	public static final boolean isValid(String dateId) {
		if (dateId == null) {
			return false;
		}
		try {
			DateUtil.fromISO8601(dateId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static final NullComparator COMPARATOR = new NullComparator(
			new Comparator<DateId>() {
				@Override
				public int compare(DateId id1, DateId id2) {
					return id1.calendar.compareTo(id2.calendar);
				}
			});

	private final Calendar calendar;

	public DateId(String dateId) {
		super();
		if (!isValid(dateId)) {
			throw new InvalidParameterException(DateId.class.getSimpleName()
					+ " must contain a valid date");
		}
		this.calendar = DateUtil.fromISO8601(dateId);
	}

	@Override
	public String getIdentifier() {
		return DateUtil.toISO8601(this.calendar);
	}

	public Calendar getCalendar() {
		return this.calendar;
	}

	@Override
	public int compareTo(Object obj) {
		return COMPARATOR.compare(this, obj instanceof DateId ? (DateId) obj
				: null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.calendar == null) ? 0 : this.calendar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this.compareTo(obj) == 0;
	}

	@Override
	public String toString() {
		return this.getIdentifier();
	}
}
