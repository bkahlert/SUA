package de.fu_berlin.imp.apiua.survey.model.groupdiscussion;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;

public class GroupDiscussionDocumentField implements ILocatable {

	private static final long serialVersionUID = 2816855074503661103L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(GroupDiscussionDocumentField.class);

	private URI uri;
	private final GroupDiscussionDocument groupDiscussionDocument;
	private String value;

	public GroupDiscussionDocumentField(
			GroupDiscussionDocument groupDiscussionDocument, URI uri,
			String value) {
		this.groupDiscussionDocument = groupDiscussionDocument;
		this.uri = uri;
		this.value = value;
	}

	public GroupDiscussionDocument getGroupDiscussionDocument() {
		return this.groupDiscussionDocument;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	public String getValue() {
		return this.value;
	}

}
