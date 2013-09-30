package de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd;

import java.net.URI;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml.XMLSurveyDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml.XMLSurveyField;

public class CDDocumentField extends XMLSurveyField implements ILocatable {

	private static final long serialVersionUID = 2816855074503661103L;

	private static final Logger LOGGER = Logger
			.getLogger(CDDocumentField.class);

	private CDDocument cdDocument;

	public CDDocumentField(CDDocument cdDocument, XMLSurveyField xmlField) {
		super(xmlField.getKey(), xmlField.getQuestion(), xmlField.getAnswer());
		this.cdDocument = cdDocument;
	}

	public CDDocument getCdDocument() {
		return this.cdDocument;
	}

	@Override
	public URI getUri() {
		try {
			return new URI(this.cdDocument.getUri().toString() + "/"
					+ this.getKey());
		} catch (Exception e) {
			LOGGER.error("Could not create URI for a "
					+ XMLSurveyDocument.class.getSimpleName(), e);
		}
		return null;
	}

}
