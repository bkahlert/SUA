package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteAbandonedCodeInstancesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;

class CodeStore implements ICodeStore {

	private static final Logger logger = Logger.getLogger(CodeStore.class);

	private File codeStoreFile;
	private LinkedList<Long> createdIds = new LinkedList<Long>();
	private ICode[] codes = null;
	private ICodeInstance[] codeInstances = null;

	public CodeStore(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
		this.codes = null;
	}

	public ICode createCode(String caption) throws CodeStoreFullException {
		Long id = Long.MAX_VALUE;
		try {
			ICode[] codes = this.loadCodes();
			ArrayList<Long> ids = new ArrayList<Long>(codes.length);
			for (ICode code : codes) {
				if (code.getId() == Long.MAX_VALUE)
					throw new CodeStoreFullException();
				ids.add(code.getId());
			}
			ids.addAll(createdIds);
			id = Code.calculateId(ids);
		} catch (CodeStoreReadException e) {
			logger.fatal(
					"Could not calculate a new " + ICode.class.getSimpleName()
							+ " ID", e);
		}
		createdIds.add(id);
		return new Code(id, caption);
	}

	@Override
	public ICodeInstance createCodeInstance(ICode code, ICodeable codeable)
			throws InvalidParameterException, CodeStoreReadException,
			DuplicateCodeInstanceException {
		for (ICode currentCode : loadCodes()) {
			if (currentCode.equals(code)) {
				ICodeInstance codeInstance = new CodeInstance(code,
						codeable.getCodeInstanceId(), new TimeZoneDate(
								new Date(), TimeZone.getDefault()));
				if (ArrayUtils.contains(loadCodeInstances(), codeInstance)) {
					throw new DuplicateCodeInstanceException();
				} else {
					return codeInstance;
				}
			}
		}
		throw new InvalidParameterException("Could not find a matching "
				+ ICode.class.getSimpleName() + " for " + code);
	}

	@Override
	public ICode[] loadCodes() throws CodeStoreReadException {
		if (!this.codeStoreFile.exists())
			throw new CodeStoreReadException(new FileNotFoundException(
					codeStoreFile.getAbsolutePath()));

		if (this.codes != null)
			return this.codes;

		NodeList nodes = null;
		if (codeStoreFile.length() > 0) {
			try {
				nodes = (NodeList) CodeStoreUtils.runXPathExpression(
						codeStoreFile, new CodeStoreUtils.XPathRunnable() {
							@Override
							public Object run(Document doc, XPath xpath)
									throws XPathExpressionException {
								return xpath.compile("//code").evaluate(doc,
										XPathConstants.NODESET);
							}
						});
			} catch (Exception e) {
				logger.error("Could not read " + ICode.class.getSimpleName()
						+ "s from " + codeStoreFile.getName(), e);
				throw new CodeStoreReadException(e);
			}
		}

		if (nodes == null)
			return (ICode[]) new ICode[0];

		ICode[] codes = new ICode[nodes.getLength()];
		for (int i = 0; i < nodes.getLength(); i++) {
			Node codeItem = nodes.item(i);
			NamedNodeMap codeItemAttributes = codeItem.getAttributes();
			long id = Long.parseLong(codeItemAttributes.getNamedItem("id")
					.getNodeValue());
			String caption = codeItemAttributes.getNamedItem("caption")
					.getNodeValue();
			codes[i] = new Code(id, caption);
		}

		this.codes = codes;

		return codes;
	}

	@Override
	public ICodeInstance[] loadCodeInstances() throws CodeStoreReadException {
		if (!this.codeStoreFile.exists())
			throw new CodeStoreReadException(new FileNotFoundException(
					codeStoreFile.getAbsolutePath()));

		if (this.codeInstances != null)
			return this.codeInstances;

		NodeList nodes = null;
		if (codeStoreFile.length() > 0) {
			try {
				nodes = (NodeList) CodeStoreUtils.runXPathExpression(
						codeStoreFile, new CodeStoreUtils.XPathRunnable() {
							@Override
							public Object run(Document doc, XPath xpath)
									throws XPathExpressionException {
								return xpath.compile("//codeInstance")
										.evaluate(doc, XPathConstants.NODESET);
							}
						});
			} catch (Exception e) {
				logger.error(
						"Could not read " + ICodeInstance.class.getSimpleName()
								+ "s from " + codeStoreFile.getName(), e);
				throw new CodeStoreReadException(e);
			}
		}

		if (nodes == null)
			return (ICodeInstance[]) new ICodeInstance[0];

		CodeMapper codeMapper = new CodeMapper(loadCodes());

		ICodeInstance[] codeInstances = (ICodeInstance[]) new ICodeInstance[nodes
				.getLength()];
		for (int i = 0; i < nodes.getLength(); i++) {
			Node codeItem = nodes.item(i);
			NamedNodeMap codeItemAttributes = codeItem.getAttributes();
			long codeId = Long.parseLong(codeItemAttributes.getNamedItem(
					"codeId").getNodeValue());
			String id = codeItemAttributes.getNamedItem("id").getNodeValue();
			TimeZoneDate creation = new TimeZoneDate(codeItemAttributes
					.getNamedItem("creation").getNodeValue());
			codeInstances[i] = new CodeInstance(codeMapper.getCode(codeId), id,
					creation);
		}

		return codeInstances;
	}

	@Override
	public void addAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeStoreReadException {
		saveCodes((ICode[]) ArrayUtils.add(loadCodes(), code));
	}

	@Override
	public void addAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException, CodeStoreReadException {
		saveCodeInstances((ICodeInstance[]) ArrayUtils.add(loadCodeInstances(),
				codeInstance));
	}

	@Override
	public void saveCodes(ICode[] codes) throws CodeStoreWriteException,
			CodeStoreReadException {
		save(codes, loadCodeInstances());
	}

	@Override
	public void saveCodeInstances(ICodeInstance[] codeInstances)
			throws CodeStoreWriteException, CodeStoreReadException {
		save(loadCodes(), codeInstances);
	}

	@Override
	public void save(ICode[] codes, ICodeInstance[] codeInstances)
			throws CodeStoreWriteException {
		this.codes = codes;
		this.codeInstances = codeInstances;

		// sanity check
		LinkedList<ICodeInstance> abandonedCodeInstances = new LinkedList<ICodeInstance>();
		for (ICodeInstance codeInstance : codeInstances) {
			if (!ArrayUtils.contains(codes, codeInstance.getCode()))
				abandonedCodeInstances.add(codeInstance);
		}
		if (abandonedCodeInstances.size() > 0) {
			throw new CodeStoreWriteAbandonedCodeInstancesException(
					abandonedCodeInstances
							.toArray(new ICodeInstance[abandonedCodeInstances
									.size()]));
		}

		Document doc = null;
		try {
			doc = CodeStoreUtils.loadDocument(codeStoreFile);
		} catch (SAXParseException e) {
			try {
				doc = CodeStoreUtils.newDocument();
			} catch (Exception f) {
				throw new CodeStoreWriteException(f);
			}
		} catch (Exception e) {
			throw new CodeStoreWriteException(e);
		}

		Node rootNode;
		if (doc.getChildNodes().getLength() == 0) {
			rootNode = doc.appendChild(doc.createElement("SUACodeStore"));
		} else {
			rootNode = doc.getChildNodes().item(0);
		}

		Node codesNode = null;
		Node codeInstancesNode = null;
		for (int i = 0, m = rootNode.getChildNodes().getLength(); i < m; i++) {
			Node childNode = rootNode.getChildNodes().item(i);
			if (childNode.getNodeName().equals("codes"))
				codesNode = childNode;
			else if (childNode.getNodeName().equals("codeInstances"))
				codeInstancesNode = childNode;
		}

		if (codesNode == null) {
			codesNode = rootNode.appendChild(doc.createElement("codes"));
		}

		if (codeInstancesNode == null) {
			codeInstancesNode = rootNode.appendChild(doc
					.createElement("codeInstances"));
		}

		CodeStoreUtils.clearChildren(codesNode);
		CodeStoreUtils.clearChildren(codeInstancesNode);

		for (ICode code : codes) {
			Element codeElement = doc.createElement("code");
			codeElement.setAttribute("id", Long.valueOf(code.getId())
					.toString());
			codeElement.setAttribute("caption", code.getCaption());
			codesNode.appendChild(codeElement);
		}

		for (ICodeInstance codeInstance : codeInstances) {
			Element codeInstanceElement = doc.createElement("codeInstance");
			codeInstanceElement.setAttribute("codeId",
					Long.valueOf(codeInstance.getCode().getId()).toString());
			codeInstanceElement.setAttribute("id", codeInstance.getId());
			codeInstanceElement.setAttribute("creation", codeInstance
					.getCreation().toISO8601());
			codeInstancesNode.appendChild(codeInstanceElement);
		}

		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			FileOutputStream writer = new FileOutputStream(codeStoreFile);
			trans.transform(new DOMSource(doc), new StreamResult(writer));
			writer.close();
		} catch (TransformerConfigurationException e) {
			throw new CodeStoreWriteException(e);
		} catch (FileNotFoundException e) {
			throw new CodeStoreWriteException(e);
		} catch (TransformerException e) {
			throw new CodeStoreWriteException(e);
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
	}

	@Override
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeInstanceDoesNotExistException, CodeStoreWriteException,
			CodeStoreReadException {
		List<ICodeInstance> codesInstances = new ArrayList<ICodeInstance>(
				Arrays.asList(loadCodeInstances()));
		if (!codesInstances.contains(codeInstance))
			throw new CodeInstanceDoesNotExistException();
		codesInstances.remove(codeInstance);
		saveCodeInstances(codesInstances.toArray(new ICodeInstance[0]));
	}

	public void deleteCodeInstances(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException {
		List<ICodeInstance> codeInstancesToKeep = new LinkedList<ICodeInstance>();
		for (ICodeInstance codeInstance : this.loadCodeInstances()) {
			if (!codeInstance.getCode().equals(code)) {
				codeInstancesToKeep.add(codeInstance);
			}
		}
		saveCodeInstances(codeInstancesToKeep.toArray(new ICodeInstance[0]));
	}

	@Override
	public void deleteCode(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException, CodeDoesNotExistException {
		List<ICode> codes = new ArrayList<ICode>(Arrays.asList(loadCodes()));
		if (!codes.contains(code))
			throw new CodeDoesNotExistException();
		deleteCodeInstances(code);
		codes.remove(code);
		saveCodes(codes.toArray(new ICode[0]));
	}
}
