package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

/**
 * Instances of this interface symbolize links between two {@link IEndpoint}s.
 * 
 * @author bkahlert
 * 
 */
public interface ILink extends ILocatable {

	public static interface IEndpoint {
	}

	public static interface ICoordinateEndpoint extends IEndpoint {
		public long getX();

		public long getY();
	}

	public static interface ICodeEndpoint extends IEndpoint {
		public URI getCode();
	}

	public static class CoordinateEndpoint implements ICoordinateEndpoint {
		private final int x;
		private final int y;

		public CoordinateEndpoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public long getX() {
			return x;
		}

		@Override
		public long getY() {
			return y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CoordinateEndpoint other = (CoordinateEndpoint) obj;
			if (x != other.x) {
				return false;
			}
			if (y != other.y) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "{ " + x + ", " + y + " }";
		}
	}

	public static class CodeEndpoint implements ICodeEndpoint {
		private final URI id;

		public CodeEndpoint(URI id) {
			this.id = id;
		}

		@Override
		public URI getCode() {
			return id;
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
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CodeEndpoint other = (CodeEndpoint) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "{ " + id + " }";
		}
	}

	public String getTitle();

	public IEndpoint getSource();

	public IEndpoint getTarget();

}
