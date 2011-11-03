package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

public class Token implements Comparable<Token> {
	private String token;

	public Token(String token) {
		super();
		this.token = token;
	}

	@Override
	public String toString() {
		return this.token;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	@Override
	public int compareTo(Token token) {
		return this.toString().compareTo(token.toString());
	}

}
