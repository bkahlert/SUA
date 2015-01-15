package de.fu_berlin.imp.apiua.survey.model.groupdiscussion;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.core.model.IHtmlDocument;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;

/**
 * Instances of this class describe the answers to a Cognitive Dimensions of
 * Notations Framework Questionnaire.
 *
 * @author bjornson
 *
 */
public class GroupDiscussionDocument implements ILocatable,
		Iterable<GroupDiscussionDocumentField> {

	private static final long serialVersionUID = 7435885316236930663L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(GroupDiscussionDocument.class);

	private IHtmlDocument htmlDocument;
	private List<GroupDiscussionDocumentField> fields;

	public GroupDiscussionDocument(IHtmlDocument htmlDocument) {
		this.htmlDocument = htmlDocument;
		this.fields = new ArrayList<>();
		for (Entry<URI, String> entry : htmlDocument.getFields().entrySet()) {
			this.fields.add(new GroupDiscussionDocumentField(this, entry
					.getKey(), entry.getValue()));
		}
	}

	public File getFile() {
		return this.htmlDocument.getStaticFile();
	}

	@Override
	public URI getUri() {
		return this.htmlDocument.getUri();
	}

	public String getCssQuery() {
		return this.htmlDocument.getCssQuery();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<GroupDiscussionDocumentField> iterator() {
		return new ArrayListIterator(
				this.fields.toArray(new GroupDiscussionDocumentField[0]));
	}

	public int getSize() {
		return this.fields.size();
	}

	public File getMarkedUpFile() {
		return this.htmlDocument.getMarkedUpFile();
	}

}
