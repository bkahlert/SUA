package de.fu_berlin.imp.apiua.survey.model.xml;

/**
 * Instances of this class represent a question in a cognitive dimensions
 * questionnaire.
 * 
 * @author bjornson
 * 
 */
public class XMLSurveyField {
	private String key;
	private String question;
	private String answer;

	public XMLSurveyField(String key, String question, String answer) {
		super();
		this.key = key;
		this.question = question;
		this.answer = answer;
	}

	public XMLSurveyField(String key, String answer) {
		super();
		this.key = key;
		this.question = null;
		this.answer = answer;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return this.question;
	}

	/**
	 * @return the answer
	 */
	public String getAnswer() {
		return this.answer;
	}

}
