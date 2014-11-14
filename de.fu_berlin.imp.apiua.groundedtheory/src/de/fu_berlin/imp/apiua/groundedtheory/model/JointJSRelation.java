package de.fu_berlin.imp.apiua.groundedtheory.model;

import java.util.HashMap;
import java.util.List;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;

public class JointJSRelation implements ILocatable, IRelation {
	private static final long serialVersionUID = 1L;

	private final HashMap<String, Object> cell;

	private URI uri;
	private String title;
	private IEndpoint source;
	private IEndpoint target;

	public JointJSRelation(HashMap<String, Object> cell) {
		this.cell = cell;
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			this.uri = new URI(this.cell.get("uri").toString());
		}
		return this.uri;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getTitle() {
		if (this.title == null) {
			List<HashMap<String, Object>> labels = (List<HashMap<String, Object>>) this.cell
					.get("labels");
			if (labels != null) {
				for (HashMap<String, Object> label : labels) {
					this.title = (String) ((HashMap<String, Object>) ((HashMap<String, Object>) label
							.get("attrs")).get("text")).get("text");
				}
			}
		}
		return this.title;
	}

	private IEndpoint createEndpoint(HashMap<String, Object> endpoint) {
		if (endpoint.get("id") != null) {
			final URI id = new URI(endpoint.get("id").toString());
			return new IRelation.NodeEndpoint(id.toString());
		} else {
			final int x = Integer.valueOf(endpoint.get("x").toString());
			final int y = Integer.valueOf(endpoint.get("y").toString());
			return new IRelation.CoordinateEndpoint(x, y);
		}
	}

	@Override
	public IEndpoint getSource() {
		if (this.source == null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> source = (HashMap<String, Object>) this.cell
					.get("source");
			this.source = this.createEndpoint(source);
		}
		return this.source;
	}

	@Override
	public IEndpoint getTarget() {
		if (this.target == null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> source = (HashMap<String, Object>) this.cell
					.get("target");
			this.target = this.createEndpoint(source);
		}
		return this.target;
	}

	@Override
	public String toString() {
		return "Link \"" + this.getTitle() + "\": " + this.getSource() + " -> "
				+ this.getTarget();
	}

}
