package de.fu_berlin.imp.apiua.survey.model.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

public class XMLSurveyDocument implements Iterable<XMLSurveyField> {

	private static final Logger LOGGER = Logger
			.getLogger(XMLSurveyDocument.class);

	private List<XMLSurveyField> questions;

	// TODO provide Properties instead of language to avoid loading translation
	// n times
	public XMLSurveyDocument(IData data, String lang) throws IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Document doc;
		try {
			doc = dBuilder.parse(data.getStaticFile());
		} catch (SAXException e) {
			throw new IOException(e);
		}

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		Element currentNode = doc.getDocumentElement();
		while (currentNode.getChildNodes().getLength() == 1) {
			currentNode = (Element) ((NodeList) currentNode.getChildNodes())
					.item(0);
		}

		Properties translation = loadTranslation(data.getParentDataContainer(),
				lang);
		this.questions = readQuestions(currentNode, translation);
	}

	public XMLSurveyDocument(IData data) throws IOException {
		this(data, null);
	}

	public XMLSurveyField getQuestion(int index) {
		if (this.questions == null || index < 0
				|| index >= this.questions.size()) {
			throw new IllegalArgumentException();
		}
		return this.questions.get(index);
	}

	public int getSize() {
		return this.questions.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<XMLSurveyField> iterator() {
		return new ArrayListIterator(
				this.questions.toArray(new XMLSurveyField[0]));
	}

	/**
	 * Loads a translation file in the ini format from a {@link IDataContainer}.
	 * The {@link IDataContainer} must contain a resource with the name
	 * {LANG}.properties.
	 * 
	 * @param fromContainer
	 * @param lang
	 * @return
	 */
	public static Properties loadTranslation(IDataContainer fromContainer,
			String lang) {
		IData file = fromContainer.getResource(lang + ".properties");
		Properties translation = new Properties();
		try {
			translation.load(IOUtils.toInputStream(file.read()));
		} catch (IOException e) {
			LOGGER.error("Error reading translation " + lang
					+ ".properties from " + fromContainer);
		}
		return translation;
	}

	/**
	 * Given a XML node the method returns a list of extracted questions formed
	 * by the node's child nodes.
	 * 
	 * @param parentNode
	 * @param translation
	 * @return
	 */
	public static List<XMLSurveyField> readQuestions(Element parentNode,
			Properties translation) {
		List<XMLSurveyField> questions = new ArrayList<XMLSurveyField>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				String key = element.getNodeName();
				String answer = element.getTextContent();
				String question = translation != null ? translation
						.getProperty(key, null) : null;

				XMLSurveyField record = new XMLSurveyField(key, question,
						answer);
				questions.add(record);
			}
		}
		return questions;
	}

}
