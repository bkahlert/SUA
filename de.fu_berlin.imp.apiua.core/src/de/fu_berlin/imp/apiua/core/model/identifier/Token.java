package de.fu_berlin.imp.apiua.core.model.identifier;

import java.util.Comparator;

import org.apache.commons.collections.comparators.NullComparator;


public class Token implements IIdentifier {

	private static final NullComparator COMPARATOR = new NullComparator(
			new Comparator<Token>() {
				@Override
				public int compare(Token token1, Token token2) {
					return token1.getIdentifier().compareTo(
							token2.getIdentifier());
				}
			});

	private String token;

	public Token(String token) {
		super();
		this.token = token;
	}

	@Override
	public String getIdentifier() {
		return this.token;
	}

	@Override
	public int compareTo(Object obj) {
		return COMPARATOR.compare(this, obj instanceof Token ? (Token) obj
				: null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.token == null) ? 0 : this.token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this.compareTo(obj) == 0;
	}

	@Override
	public String toString() {
		return this.token;
	}

}
