package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

public class ID implements Comparable<ID> {
	private String id;

	public ID(String id) {
		super();
		this.id = id;
	}

	@Override
	public String toString() {
		return this.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ID other = (ID) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(ID id) {
		return this.toString().compareTo(id.toString());
	}

}
