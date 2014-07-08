package de.fu_berlin.imp.apiua.core.model.identifier;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.comparators.NullComparator;

import de.fu_berlin.imp.apiua.core.model.adapters.IDAdapter;

@XmlJavaTypeAdapter(IDAdapter.class)
public class ID implements IIdentifier {

	public static final Pattern PATTERN = Pattern.compile("^[A-Za-z\\d]+$");

	public static final boolean isValid(String id) {
		if (id == null) {
			return false;
		}
		return PATTERN.matcher(id).find();
	}

	private static final NullComparator COMPARATOR = new NullComparator(
			new Comparator<ID>() {
				@Override
				public int compare(ID id1, ID id2) {
					return id1.getIdentifier().compareTo(id2.getIdentifier());
				}
			});

	private String id;

	public ID(String id) {
		super();
		if (!isValid(id)) {
			throw new InvalidParameterException(ID.class.getSimpleName()
					+ " must only contain alphanumeric characters");
		}
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return this.id;
	}

	@Override
	public int compareTo(Object obj) {
		return COMPARATOR.compare(this, obj instanceof ID ? (ID) obj : null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
