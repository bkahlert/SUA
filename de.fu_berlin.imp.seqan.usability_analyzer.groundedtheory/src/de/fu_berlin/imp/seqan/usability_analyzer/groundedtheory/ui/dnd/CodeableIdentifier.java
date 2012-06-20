package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.dnd;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class CodeableIdentifier implements Serializable {

	private static final Logger LOGGER = Logger
			.getLogger(CodeableIdentifier.class);
	private static final long serialVersionUID = 1L;
	private final String id;

	public CodeableIdentifier(ICodeable codeable) {
		this.id = codeable.getCodeInstanceID().toString();
	}

	public URI getId() {
		try {
			return new URI(id);
		} catch (URISyntaxException e) {
			LOGGER.error("Could not convert string to uri", e);
			return null;
		}
	}
}
