package de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml.XMLSurveyField;

public class CDDocumentField extends XMLSurveyField implements ILocatable {

	private static final long serialVersionUID = 2816855074503661103L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CDDocumentField.class);

	private URI uri;
	private final CDDocument cdDocument;

	public CDDocumentField(CDDocument cdDocument, XMLSurveyField xmlField) {
		super(xmlField.getKey(), xmlField.getQuestion(), xmlField.getAnswer());
		this.cdDocument = cdDocument;
	}

	public CDDocument getCdDocument() {
		return this.cdDocument;
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			try {
				this.uri = new URI(this.cdDocument.getUri().toString() + "/"
						+ this.getKey());
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + CDDocumentField.class, e);
			}
		}
		return this.uri;
	}

}
