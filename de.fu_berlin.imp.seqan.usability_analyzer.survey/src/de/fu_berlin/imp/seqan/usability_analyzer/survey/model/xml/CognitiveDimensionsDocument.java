package de.fu_berlin.imp.seqan.usability_analyzer.survey.model.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;

public class CognitiveDimensionsDocument implements
		Iterable<CognitiveDimensionsRecord> {
	private List<CognitiveDimensionsRecord> questions;

	public CognitiveDimensionsDocument(IData data) throws IOException {
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

		this.questions = readQuestions(currentNode);
	}

	public CognitiveDimensionsRecord getQuestion(int index) {
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
	public Iterator<CognitiveDimensionsRecord> iterator() {
		return new ArrayListIterator(this.questions);
	}

	public static List<CognitiveDimensionsRecord> readQuestions(
			Element parentNode) {
		List<CognitiveDimensionsRecord> questions = new ArrayList<CognitiveDimensionsRecord>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				String key = element.getNodeName();
				String answer = element.getTextContent();

				CognitiveDimensionsRecord question = new CognitiveDimensionsRecord(
						key, answer);
				questions.add(question);
			}
		}
		return questions;
	}
}
