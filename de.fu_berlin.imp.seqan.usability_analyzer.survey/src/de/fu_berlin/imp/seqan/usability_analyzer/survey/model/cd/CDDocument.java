package de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.bkahlert.nebula.utils.HashUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.SurveyLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.DateId;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml.XMLSurveyDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml.XMLSurveyField;

/**
 * Instances of this class describe the answers to a Cognitive Dimensions of
 * Notations Framework Questionnaire.
 * 
 * @author bjornson
 * 
 */
public class CDDocument implements ILocatable, HasIdentifier,
		Iterable<CDDocumentField> {

	private static final long serialVersionUID = 7435885316236930663L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(CDDocument.class);

	private URI uri;
	private final List<CDDocumentField> fields;
	private DateId identifier;

	public CDDocument(IData data, String lang) throws IOException {
		try {
			this.identifier = new DateId(FilenameUtils.getBaseName(data
					.getName()));
		} catch (IllegalArgumentException e) {
			this.identifier = null;
		}

		XMLSurveyDocument xml = new XMLSurveyDocument(data, lang);
		this.fields = new ArrayList<CDDocumentField>();
		for (XMLSurveyField xmlField : xml) {
			this.fields.add(new CDDocumentField(this, xmlField));
		}
	}

	@Override
	public URI getUri() {
		if (this.uri == null) {
			try {
				this.uri = new URI("sua://"
						+ SurveyLocatorProvider.SURVEY_NAMESPACE + "/cd/"
						+ this.identifier);
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + CDDocument.class, e);
			}
		}
		return this.uri;
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.identifier;
	}

	/**
	 * Returns a hash of length 4 of the {@link CDDocument}'s
	 * {@link IIdentifier} for visual purposes only. The uniqueness is not
	 * guaranteed but very likely and should thus never be used to unambiguously
	 * identify a resource.
	 * 
	 * @return
	 */
	public static String getIdentifierHash(IIdentifier identifier) {
		return HashUtils.md5(identifier.toString()).substring(0, 4);
	}

	/**
	 * Returns a hash of length 4 of the {@link CDDocument}'s
	 * {@link IIdentifier} for visual purposes only. The uniqueness is not
	 * guaranteed but very likely and should thus never be used to unambiguously
	 * identify a resource.
	 * 
	 * @return
	 */
	public String getIdentifierHash() {
		return getIdentifierHash(this.identifier);
	}

	public Calendar getCompleted() {
		return this.identifier.getCalendar();
	}

	public String getQuestionKey(int index) {
		return this.fields.get(index).getKey();
	}

	public String getQuestionTitle(int index) {
		return this.fields.get(index).getQuestion();
	}

	public String getQuestionAnswer(int index) {
		return this.fields.get(index).getAnswer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<CDDocumentField> iterator() {
		return new ArrayListIterator(
				this.fields.toArray(new CDDocumentField[0]));
	}

	public int getSize() {
		return this.fields.size();
	}

}
