package de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.comparators.NullComparator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.adapters.FingerprintAdapter;

@XmlJavaTypeAdapter(FingerprintAdapter.class)
public class Fingerprint implements IIdentifier {
	public static final Pattern PATTERN = Pattern.compile("^![A-Za-z\\d]+$");

	public static final boolean isValid(String id) {
		if (id == null) {
			return false;
		}
		return PATTERN.matcher(id).find();
	}

	private static final NullComparator COMPARATOR = new NullComparator(
			new Comparator<Fingerprint>() {
				@Override
				public int compare(Fingerprint fingerprint1,
						Fingerprint fingerprint2) {
					return fingerprint1.getIdentifier().compareTo(
							fingerprint2.getIdentifier());
				}
			});

	private String fingerprint;

	public Fingerprint(String fingerprint) {
		super();
		if (!isValid(fingerprint)) {
			throw new InvalidParameterException(
					Fingerprint.class.getSimpleName() + " " + fingerprint
							+ " must only contain alphanumeric characters");
		}
		this.fingerprint = fingerprint;
	}

	@Override
	public String getIdentifier() {
		return this.fingerprint;
	}

	@Override
	public int compareTo(Object obj) {
		return COMPARATOR.compare(this,
				obj instanceof Fingerprint ? (Fingerprint) obj : null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.fingerprint == null) ? 0 : this.fingerprint.hashCode());
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
