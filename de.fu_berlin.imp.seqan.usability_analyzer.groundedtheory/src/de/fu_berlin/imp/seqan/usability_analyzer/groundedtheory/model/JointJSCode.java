package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.util.HashMap;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public class JointJSCode implements ILocatable {

	private static final long serialVersionUID = 1L;

	private final HashMap<String, Object> cell;

	private URI uri;

	public JointJSCode(HashMap<String, Object> cell) {
		this.cell = cell;
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			this.uri = new URI(this.cell.get("id").toString());
		}
		return this.uri;
	}

}
