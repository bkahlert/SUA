package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.output.NullOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class CodeStoreUtils {

	static interface XPathRunnable {
		Object run(Document doc, XPath xpath) throws XPathExpressionException;
	}

	static void clearChildren(Node node) {
		while (node.hasChildNodes())
			node.removeChild(node.getFirstChild());
	}

	static Document newDocument() throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().newDocument();
	}

	static Document loadDocument(File file) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		/*
		 * parse generates annoying output if the file is empty
		 */
		PrintStream oldErr = System.err;
		System.setErr(new PrintStream(new NullOutputStream()));
		Document document = factory.newDocumentBuilder().parse(file);
		System.setErr(oldErr);
		return document;
	}

	static Object runXPathExpression(File file, XPathRunnable runnable)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		Document doc = loadDocument(file);

		XPathFactory xFactory = XPathFactory.newInstance();
		XPath xpath = xFactory.newXPath();

		return runnable.run(doc, xpath);
	}

}
